package controlador;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import modelo.dao.FormularioDAO;
import modelo.entidades.ResultadoFormulario;

@WebServlet(name = "InsertarDatosFormulario", urlPatterns = {"/InsertarDatosFormulario"})
public class DatosFormulario extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        // ── 1. OBTENER USUARIO DE SESIÓN ──────────────────────────────────
        String nomU = (String) request.getSession().getAttribute("usuario");
        if (nomU == null) {
            response.sendRedirect("index.html");
            return;
        }

        // ── 2. RECOGER DATOS DEL FORMULARIO ───────────────────────────────
        String[] favoritas = request.getParameterValues("favoritas");
        String[] odiadas   = request.getParameterValues("odiadas");
        String[] superadas = request.getParameterValues("superadas");
        String[] suspensas = request.getParameterValues("suspensas");

        String python    = request.getParameter("python");
        String mips      = request.getParameter("mips");
        String nivelJava = request.getParameter("nivelJava");
        String c         = request.getParameter("c");

        try {
            // ── 3. PERSISTIR EN BD (modelo.dao) ───────────────────────────
            FormularioDAO dao = new FormularioDAO();
            dao.guardarFormulario(nomU, favoritas, odiadas, superadas, suspensas,
                                  python, mips, nivelJava, c);

            // ── 4. CALCULAR PUNTUACIONES Y RECOMENDACIÓN ──────────────────
            ResultadoFormulario resultado = calcularResultado(
                nomU, favoritas, odiadas, superadas, suspensas,
                python, mips, nivelJava, c
            );

            // ── 5. ACTUALIZAR MENCIÓN EN BD ────────────────────────────────
            actualizarMencion(nomU, resultado.getMencionRecomendada());

            // ── 6. GUARDAR RESULTADO EN SESIÓN Y REDIRIGIR ─────────────────
            request.getSession().setAttribute("resultadoFormulario", resultado);
            response.sendRedirect("MostrarProgreso");

        } catch (Exception e) {
            response.getWriter().println("<p>Error: " + e.getMessage() + "</p>");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // LÓGICA DE CÁLCULO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Calcula las puntuaciones de cada asignatura de 3º y 4º,
     * determina la mención recomendada y selecciona las asignaturas
     * recomendadas para cada cuatrimestre de 3º.
     */
    private ResultadoFormulario calcularResultado(String nomU,
                                                   String[] favoritas,
                                                   String[] odiadas,
                                                   String[] superadas,
                                                   String[] suspensas,
                                                   String python,
                                                   String mips,
                                                   String nivelJava,
                                                   String c) throws Exception {

        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
        Connection conn = ds.getConnection();

        // Puntuación por mención
        Map<String, Double> puntuacionMencion = new HashMap<>();
        puntuacionMencion.put("IS", 0.0);
        puntuacionMencion.put("CO", 0.0);
        puntuacionMencion.put("TI", 0.0);

        // Puntuación por asignatura (codA -> puntuación)
        Map<Integer, Double> puntuacionAsignatura = new HashMap<>();

        // Cargar todas las asignaturas de 3º y 4º con su mención
        PreparedStatement psAsig = conn.prepareStatement(
            "SELECT a.cod, a.nombre, a.cuatri, am.mencion, am.cursoAcademico, am.obligatoriedad " +
            "FROM Asignatura a " +
            "JOIN AsignaturasMencion am ON a.cod = am.codA " +
            "WHERE am.mencion != 'PreMención' AND am.cursoAcademico IN (3, 4)"
        );
        ResultSet rsAsig = psAsig.executeQuery();

        // Mapa: codA -> {nombre, cuatri, cursoAcademico, menciones}
        Map<Integer, AsignaturaInfo> infoAsignaturas = new HashMap<>();
        while (rsAsig.next()) {
            int cod              = rsAsig.getInt("cod");
            String nombre        = rsAsig.getString("nombre");
            int cuatri           = rsAsig.getInt("cuatri");
            String mencion       = rsAsig.getString("mencion");
            int cursoAcademico   = rsAsig.getInt("cursoAcademico");
            boolean obligatoria  = rsAsig.getBoolean("obligatoriedad");

            infoAsignaturas.computeIfAbsent(cod, k ->
                new AsignaturaInfo(cod, nombre, cuatri, cursoAcademico)
            ).addMencion(mencion, obligatoria);
        }
        rsAsig.close();
        psAsig.close();

        // Cargar lenguajes de cada asignatura
        PreparedStatement psLeng = conn.prepareStatement(
            "SELECT codA, nomL FROM LenguajesAsignatura"
        );
        ResultSet rsLeng = psLeng.executeQuery();
        Map<Integer, List<String>> lenguajesPorAsig = new HashMap<>();
        while (rsLeng.next()) {
            int cod   = rsLeng.getInt("codA");
            String lng = rsLeng.getString("nomL");
            lenguajesPorAsig.computeIfAbsent(cod, k -> new ArrayList<>()).add(lng);
        }
        rsLeng.close();
        psLeng.close();
        conn.close();

        // Notas de lenguajes del usuario (nombre -> nota 1-5)
        Map<String, Integer> notasLenguajes = new HashMap<>();
        if (python    != null) notasLenguajes.put("Python",           Integer.parseInt(python));
        if (mips      != null) notasLenguajes.put("Ensamblador MIPS", Integer.parseInt(mips));
        if (nivelJava != null) notasLenguajes.put("Java",             Integer.parseInt(nivelJava));
        if (c         != null) notasLenguajes.put("C",                Integer.parseInt(c));

        // Conjuntos para búsqueda rápida
        Set<String> setFavoritas  = toSet(favoritas);
        Set<String> setOdiadas    = toSet(odiadas);
        Set<String> setSuperadas  = toSet(superadas);
        Set<String> setSuspensas  = toSet(suspensas);

        // ── CALCULAR PUNTUACIÓN POR ASIGNATURA ────────────────────────────
        for (AsignaturaInfo info : infoAsignaturas.values()) {
            double puntuacion = 0.0;
            String codStr = String.valueOf(info.cod);

            // +3 si la asignatura está entre las favoritas del usuario
            if (setFavoritas.contains(codStr))  puntuacion += 3.0;

            // -3 si la asignatura está entre las odiadas
            if (setOdiadas.contains(codStr))    puntuacion -= 3.0;

            // +2 si la ha superado (la conoce y le fue bien)
            if (setSuperadas.contains(codStr))  puntuacion += 2.0;

            // -1 si la ha suspendido
            if (setSuspensas.contains(codStr))  puntuacion -= 1.0;

            // Bonus por afinidad de lenguajes
            List<String> lenguajes = lenguajesPorAsig.getOrDefault(info.cod, new ArrayList<>());
            for (String lng : lenguajes) {
                int nota = notasLenguajes.getOrDefault(lng, 0);
                // Escala de -2 a +2 según nota (1→-2, 2→-1, 3→0, 4→+1, 5→+2)
                puntuacion += (nota - 3);
            }

            // +1 adicional si es obligatoria en su mención (incentiva las troncales)
            for (Map.Entry<String, Boolean> e : info.menciones.entrySet()) {
                if (e.getValue()) puntuacion += 1.0;
            }

            puntuacionAsignatura.put(info.cod, puntuacion);

            // Acumular en puntuación de mención
            for (String men : info.menciones.keySet()) {
                puntuacionMencion.merge(men, puntuacion, Double::sum);
            }
        }

        // ── DETERMINAR MENCIÓN RECOMENDADA ────────────────────────────────
        String mencionRecomendada = puntuacionMencion.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("IS");

        // ── SELECCIONAR ASIGNATURAS DE 3º PARA CADA CUATRIMESTRE ──────────
        // Filtrar asignaturas de 3º de la mención recomendada, ordenadas por puntuación desc
        List<AsignaturaInfo> terceroCuatri1 = new ArrayList<>();
        List<AsignaturaInfo> terceroCuatri2 = new ArrayList<>();

        for (AsignaturaInfo info : infoAsignaturas.values()) {
            if (info.cursoAcademico == 3 && info.menciones.containsKey(mencionRecomendada)) {
                if (info.cuatri == 1) terceroCuatri1.add(info);
                else                  terceroCuatri2.add(info);
            }
        }

        // Ordenar por puntuación descendente
        terceroCuatri1.sort((a, b) ->
            Double.compare(puntuacionAsignatura.getOrDefault(b.cod, 0.0),
                           puntuacionAsignatura.getOrDefault(a.cod, 0.0)));
        terceroCuatri2.sort((a, b) ->
            Double.compare(puntuacionAsignatura.getOrDefault(b.cod, 0.0),
                           puntuacionAsignatura.getOrDefault(a.cod, 0.0)));

        // Convertir a listas de nombres
        List<String> nombresCuatri1 = new ArrayList<>();
        for (AsignaturaInfo info : terceroCuatri1) nombresCuatri1.add(info.nombre);

        List<String> nombresCuatri2 = new ArrayList<>();
        for (AsignaturaInfo info : terceroCuatri2) nombresCuatri2.add(info.nombre);

        // Convertir puntuaciones a mapa nombre -> puntuación para la entidad
        Map<String, Double> puntuacionesNombre = new HashMap<>();
        for (AsignaturaInfo info : infoAsignaturas.values()) {
            puntuacionesNombre.put(info.nombre,
                puntuacionAsignatura.getOrDefault(info.cod, 0.0));
        }

        return new ResultadoFormulario(
            mencionRecomendada,
            nombresCuatri1,
            nombresCuatri2,
            puntuacionesNombre
        );
    }

    private void actualizarMencion(String nomU, String mencion) throws Exception {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
        Connection conn = ds.getConnection();

        PreparedStatement ps = conn.prepareStatement(
            "UPDATE Usuario SET mencion = ? WHERE nombre = ?"
        );
        ps.setString(1, mencion);
        ps.setString(2, nomU);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    private Set<String> toSet(String[] array) {
        Set<String> set = new java.util.HashSet<>();
        if (array != null)
            for (String s : array) set.add(s);
        return set;
    }

    // ── Clase interna auxiliar ─────────────────────────────────────────────
    private static class AsignaturaInfo {
        int cod;
        String nombre;
        int cuatri;
        int cursoAcademico;
        Map<String, Boolean> menciones = new HashMap<>(); // mencion -> obligatoria

        AsignaturaInfo(int cod, String nombre, int cuatri, int cursoAcademico) {
            this.cod            = cod;
            this.nombre         = nombre;
            this.cuatri         = cuatri;
            this.cursoAcademico = cursoAcademico;
        }

        void addMencion(String mencion, boolean obligatoria) {
            menciones.put(mencion, obligatoria);
        }
    }
}