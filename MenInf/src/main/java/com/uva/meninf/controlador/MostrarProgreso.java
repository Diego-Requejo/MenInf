package com.uva.meninf.controlador;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

@WebServlet(name = "MostrarProgreso", urlPatterns = {"/MostrarProgreso"})
public class MostrarProgreso extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nomU = (String) request.getSession().getAttribute("usuario");
        if (nomU == null) {
            response.sendRedirect("index.html");
            return;
        }

        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();

            // ── Datos básicos del usuario ──────────────────────────────────
            PreparedStatement psU = conn.prepareStatement(
                "SELECT biografia, mencion, tfg, practicas FROM Usuario WHERE nombre = ?"
            );
            psU.setString(1, nomU);
            ResultSet rsU = psU.executeQuery();
            if (rsU.next()) {
                String bio = rsU.getString("biografia");
                request.setAttribute("biografia",  bio != null ? bio : "");
                request.setAttribute("mencion",    rsU.getString("mencion"));
                request.setAttribute("tfg",        rsU.getBoolean("tfg"));
                request.setAttribute("practicas",  rsU.getBoolean("practicas"));
            }
            rsU.close();
            psU.close();

            // ── Total de asignaturas ───────────────────────────────────────
            PreparedStatement psTotal = conn.prepareStatement(
                "SELECT COUNT(*) FROM Asignatura"
            );
            ResultSet rsTotal = psTotal.executeQuery();
            int totalAsignaturas = rsTotal.next() ? rsTotal.getInt(1) : 0;
            rsTotal.close();
            psTotal.close();

            // ── Cursadas del usuario (cod -> nombre) ───────────────────────
            Map<Integer, String> superadas = new LinkedHashMap<>();
            Map<Integer, String> suspensas = new LinkedHashMap<>();
            Map<Integer, String> odiadas   = new LinkedHashMap<>();
            Map<Integer, String> favoritas = new LinkedHashMap<>();

            PreparedStatement psC = conn.prepareStatement(
                "SELECT a.cod, a.nombre, c.estado, c.favorita, c.odiada " +
                "FROM Cursada c JOIN Asignatura a ON c.codA = a.cod " +
                "WHERE c.nomU = ? ORDER BY a.nombre"
            );
            psC.setString(1, nomU);
            ResultSet rsC = psC.executeQuery();
            while (rsC.next()) {
                int     cod        = rsC.getInt("cod");
                String  nombre     = rsC.getString("nombre");
                String  estado     = rsC.getString("estado");
                boolean esFavorita = rsC.getBoolean("favorita");
                boolean esOdiada   = rsC.getBoolean("odiada");

                if ("superada".equals(estado)) superadas.put(cod, nombre);
                if ("suspensa".equals(estado)) suspensas.put(cod, nombre);
                if (esFavorita) favoritas.put(cod, nombre);
                if (esOdiada)   odiadas.put(cod, nombre);
            }
            rsC.close();
            psC.close();
            conn.close();

            // ── Calcular progreso ──────────────────────────────────────────
            int porcentaje = totalAsignaturas > 0
                ? (superadas.size() * 100 / totalAsignaturas) : 0;
            double circunferencia = 2 * Math.PI * 54;
            double dashoffset = circunferencia * (1.0 - porcentaje / 100.0);

            // ── Pasar datos a la vista ─────────────────────────────────────
            request.setAttribute("nomU",           nomU);
            request.setAttribute("superadas",      superadas);
            request.setAttribute("suspensas",      suspensas);
            request.setAttribute("odiadas",        odiadas);
            request.setAttribute("favoritas",      favoritas);
            request.setAttribute("porcentaje",     porcentaje);
            request.setAttribute("circunferencia",
                String.format("%.1f", circunferencia).replace(",", "."));
            request.setAttribute("dashoffset",
                String.format("%.1f", dashoffset).replace(",", "."));

            request.getRequestDispatcher("progreso.jsp").forward(request, response);

        } catch (Exception e) {
            response.getWriter().println("<p>Error: " + e.getMessage() + "</p>");
        }
    }
}