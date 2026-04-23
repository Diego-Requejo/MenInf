/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.nivelJava to edit this template
 */
package controlador;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Para establecer la conexión con la base de datos
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;



//Para controlar el contesto con el que trabajamos:
import javax.naming.Context;
import javax.naming.InitialContext;

import java.time.Year;

/**
 *
 * @author dierequ
 */
@WebServlet(name = "InsertarDatosFormulario", urlPatterns = {"/InsertarDatosFormulario"})
public class InsertarDatosFormulario extends HttpServlet {

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        
        //1.- OBTENER DATOS FORMULARIOS
        
        // Recoger valores de los select múltiples (devuelven array)
        String[] favoritas = request.getParameterValues("favoritas");
        String[] odiadas   = request.getParameterValues("odiadas");
        String[] superadas = request.getParameterValues("superadas");
        String[] suspensas = request.getParameterValues("suspensas");
        
        // Recoger valoraciones de estrellas
        String python = request.getParameter("python");
        String mips   = request.getParameter("mips");
        String nivelJava   = request.getParameter("nivelJava");
        String c      = request.getParameter("c");
        
        int year = Year.now().getValue();
        
        // Nombre del usuario en sesión
        String nomU = (String) request.getSession().getAttribute("usuario");
        
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();

            // --- CURSADA ---
            // Superadas
            if (superadas != null)
                for (String cod : superadas)
                    insertarCursada(conn, Integer.parseInt(cod), nomU, "superada", year,
                                   contiene(favoritas, cod), contiene(odiadas, cod));

            // Suspensas
            if (suspensas != null)
                for (String cod : suspensas)
                    insertarCursada(conn, Integer.parseInt(cod), nomU, "suspensa", year,
                                   contiene(favoritas, cod), contiene(odiadas, cod));

            // Favoritas u odiadas que no estén ya en superadas/suspensas → cursando
            String[] cursando = union(favoritas, odiadas);
            if (cursando != null)
                for (String cod : cursando)
                    if (!contiene(superadas, cod) && !contiene(suspensas, cod))
                        insertarCursada(conn, Integer.parseInt(cod), nomU, "cursando", year,
                                        contiene(favoritas, cod), contiene(odiadas, cod));

            // --- PREFERENCIA LENGUAJE ---
            // Solo se inserta si el usuario seleccionó alguna estrella
            guardarPreferenciaLenguaje(conn, nomU, "Python", python);
            guardarPreferenciaLenguaje(conn, nomU, "Ensamblador MIPS", mips);
            guardarPreferenciaLenguaje(conn, nomU, "Java", nivelJava);
            guardarPreferenciaLenguaje(conn, nomU, "C", c);

            conn.close();
            response.sendRedirect("horario.html");

        } catch (Exception e) {
            response.getWriter().println("<p>Error: " + e.getMessage() + "</p>");
        }
    }

    private void insertarCursada(Connection conn, int codA, String nomU,
                                  String estado, int anio,
                                  boolean favorita, boolean odiada) throws Exception {
        String sql = "INSERT INTO Cursada (codA, nomU, estado, año, favorita, odiada) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE estado = ?, año = ?, favorita = ?, odiada = ?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, codA);
        ps.setString(2, nomU);
        ps.setString(3, estado);
        ps.setInt(4, anio);
        ps.setBoolean(5, favorita);
        ps.setBoolean(6, odiada);
        // ON DUPLICATE KEY UPDATE
        ps.setString(7, estado);
        ps.setInt(8, anio);
        ps.setBoolean(9, favorita);
        ps.setBoolean(10, odiada);

        ps.executeUpdate();
        ps.close();
    }

    private void guardarPreferenciaLenguaje(Connection conn, String nomU,
                                         String nomL, String valor) throws Exception {
    if (valor == null) return;

    int nota = Integer.parseInt(valor);

    String sql = "INSERT INTO PreferenciaLenguaje (nomU, nomL, nota) " +
                 "VALUES (?, ?, ?) " +
                 "ON DUPLICATE KEY UPDATE nota = ?";

    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setString(1, nomU);
    ps.setString(2, nomL);
    ps.setInt(3, nota);
    ps.setInt(4, nota);
    ps.executeUpdate();
    ps.close();
}

    private boolean contiene(String[] array, String valor) {
        if (array == null) return false;
        for (String s : array)
            if (s.equals(valor)) return true;
        return false;
    }

    private String[] union(String[] a, String[] b) {
        if (a == null) return b;
        if (b == null) return a;
        java.util.Set<String> set = new java.util.LinkedHashSet<>();
        for (String s : a) set.add(s);
        for (String s : b) set.add(s);
        return set.toArray(new String[0]);
    }
    

}
