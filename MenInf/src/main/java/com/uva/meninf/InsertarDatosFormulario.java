/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.uva.meninf;

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

/**
 *
 * @author ASUS
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
        String java   = request.getParameter("java");
        String c      = request.getParameter("c");
        
        // ID del usuario en sesión (asumiendo que está guardado en sesión)
        int idUsuario = (int) request.getSession().getAttribute("idUsuario");
        
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();

            // --- CURSADA ---
            // Para cada asignatura marcada, insertar o actualizar su Cursada

            // Superadas: estado = 'superada'
            insertarCursadas(conn, idUsuario, superadas, "superada", favoritas, odiadas);

            // Suspensas: estado = 'suspensa'
            insertarCursadas(conn, idUsuario, suspensas, "suspensa", favoritas, odiadas);

            // Las que están en favoritas u odiadas pero no en superadas ni suspensas: estado = 'cursando'
            insertarCursadas(conn, idUsuario, favoritas, "cursando", favoritas, odiadas);
            insertarCursadas(conn, idUsuario, odiadas,   "cursando", favoritas, odiadas);

            // --- PREFERENCIA LENGUAJE ---
            guardarPreferenciaLenguaje(conn, idUsuario, "Python", python);
            guardarPreferenciaLenguaje(conn, idUsuario, "Ensamblador MIPS", mips);
            guardarPreferenciaLenguaje(conn, idUsuario, "Java", java);
            guardarPreferenciaLenguaje(conn, idUsuario, "C", c);

            conn.close();

            response.sendRedirect("progreso.html");

        } catch (Exception e) {
            out.println("<p>Error: " + e.getMessage() + "</p>");
        }
    }

    private void insertarCursadas(Connection conn, int idUsuario, String[] asignaturas,
                                   String estado, String[] favoritas, String[] odiadas)
            throws Exception {

        if (asignaturas == null) return;

        String sql = "INSERT INTO Cursada (idUsuario, idAsignatura, estado, favorita, odiada) " +
                     "VALUES (?, (SELECT id FROM Asignatura WHERE nombre = ?), ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE estado = ?, favorita = ?, odiada = ?";

        PreparedStatement ps = conn.prepareStatement(sql);

        for (String asignatura : asignaturas) {
            boolean esFavorita = favoritas != null && contains(favoritas, asignatura);
            boolean esOdiada   = odiadas   != null && contains(odiadas,   asignatura);

            ps.setInt(1, idUsuario);
            ps.setString(2, asignatura);
            ps.setString(3, estado);
            ps.setBoolean(4, esFavorita);
            ps.setBoolean(5, esOdiada);
            // Para el ON DUPLICATE KEY UPDATE
            ps.setString(6, estado);
            ps.setBoolean(7, esFavorita);
            ps.setBoolean(8, esOdiada);

            ps.executeUpdate();
        }
        ps.close();
    }

    private void guardarPreferenciaLenguaje(Connection conn, int idUsuario,
                                             String nombreLenguaje, String notaStr)
            throws Exception {

        if (notaStr == null) return;
        int nota = Integer.parseInt(notaStr);

        String sql = "INSERT INTO PreferenciaLenguaje (idUsuario, nombreLenguaje, nota) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE nota = ?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idUsuario);
        ps.setString(2, nombreLenguaje);
        ps.setInt(3, nota);
        ps.setInt(4, nota);
        ps.executeUpdate();
        ps.close();
    }

    private boolean contains(String[] array, String valor) {
        for (String s : array) {
            if (s.equals(valor)) return true;
        }
        return false;
    }
    

}
