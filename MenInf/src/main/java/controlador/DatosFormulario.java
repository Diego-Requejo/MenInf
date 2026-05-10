package controlador;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import modelo.dao.AsignaturaDAO;
import modelo.dao.FormularioDAO;
import modelo.entidades.Asignatura;
import modelo.entidades.ResultadoFormulario;
import modelo.servicios.RecomendadorMencion;

@WebServlet(name = "DatosFormulario", urlPatterns = {"/DatosFormulario"})
public class DatosFormulario extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        // ── 1. COMPROBAR SESIÓN ───────────────────────────────────────────
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
            // ── 3. PERSISTIR EN BD ────────────────────────────────────────
            FormularioDAO formularioDAO = new FormularioDAO();
            formularioDAO.guardarFormulario(nomU, favoritas, odiadas, superadas, suspensas,
                                            python, mips, nivelJava, c);

            // ── 4. OBTENER ASIGNATURAS DE 3º Y 4º ────────────────────────
            AsignaturaDAO asignaturaDAO = new AsignaturaDAO();
            List<Asignatura> asignaturas = asignaturaDAO.listarTerceroYCuarto();

            // ── 5. CALCULAR RECOMENDACIÓN ─────────────────────────────────
            RecomendadorMencion recomendador = new RecomendadorMencion();
            ResultadoFormulario resultado = recomendador.calcularResultado(
                asignaturas, favoritas, odiadas, superadas, suspensas,
                python, mips, nivelJava, c
            );

            // ── 6. GUARDAR EN SESIÓN Y REDIRIGIR ─────────────────────────
            // El array se guarda en sesión para que el JS del horario pueda leerlo
            // via un servlet GetResultadoJson que lo serializa a JSON
            request.getSession().setAttribute("resultadoFormulario", resultado);
            response.sendRedirect("horario.jsp");

        } catch (Exception e) {
            response.getWriter().println("<p>Error: " + e.getMessage() + "</p>");
        }
    }
}