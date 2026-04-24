/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
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
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.sql.DataSource;



//Para controlar el contesto con el que trabajamos:
import javax.naming.Context;
import javax.naming.InitialContext;
/**
 *
 * @author ASUS
 */
@WebServlet(name = "MostrarProgreso", urlPatterns = {"/MostrarProgreso"})
public class MostrarProgreso extends HttpServlet {


  

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
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
            
            boolean tfg = false, practicas = false;

            // Datos básicos del usuario
            PreparedStatement psU = conn.prepareStatement(
                "SELECT biografia, mencion, tfg, practicas FROM Usuario WHERE nombre = ?"
            );
            psU.setString(1, nomU);
            ResultSet rsU = psU.executeQuery();
            if (rsU.next()) {
                request.setAttribute("biografia", rsU.getString("biografia") != null ? rsU.getString("biografia") : "");
                request.setAttribute("mencion",   rsU.getString("mencion"));
                tfg = rsU.getBoolean("tfg");
                practicas = rsU.getBoolean("practicas");
            }
            rsU.close();
            psU.close();

            // Total de asignaturas
            int totalAsignaturas = 40;
            

            // Cursadas del usuario
            ArrayList<modelo.entidades.Asignatura> superadas = new ArrayList<>();
            ArrayList<modelo.entidades.Asignatura> suspensas = new ArrayList<>();
            ArrayList<modelo.entidades.Asignatura> odiadas  = new ArrayList<>();
            ArrayList<modelo.entidades.Asignatura> favoritas = new ArrayList<>();

            PreparedStatement psC = conn.prepareStatement(
                "SELECT a.cod, a.nombre, a.abreviatura, c.estado, c.favorita, c.odiada " +
                "FROM Cursada c JOIN Asignatura a ON c.codA = a.cod " +
                "WHERE c.nomU = ? ORDER BY a.nombre"
            );
            psC.setString(1, nomU);
            ResultSet rsC = psC.executeQuery();
            while (rsC.next()) {
                modelo.entidades.Asignatura asig = new modelo.entidades.Asignatura();
                asig.setCod(rsC.getInt("cod"));
                asig.setNombre(rsC.getString("nombre"));
                asig.setAbreviatura(rsC.getString("abreviatura"));
                
                String estado      = rsC.getString("estado");
                boolean esOdiada = rsC.getBoolean("odiada");
                boolean esFavorita = rsC.getBoolean("favorita");
               

                if ("superada".equals(estado))       superadas.add(asig);
                else  suspensas.add(asig);
                if (esOdiada) odiadas.add(asig);
                if (esFavorita) favoritas.add(asig);
                
            }
            rsC.close();
            psC.close();
            conn.close();

            // Calcular progreso
            int progreso = superadas.size();
            if(tfg)
                progreso+=2;
            if(practicas)
                progreso+=2;
            int porcentaje = (int) Math.round((progreso * 100.0) / totalAsignaturas);

            double circunferencia = 2 * Math.PI * 54;
            double dashoffset = circunferencia * (1.0 - (porcentaje / 100.0));

            // Pasar datos a la vista
            request.setAttribute("nomU",          nomU);
            request.setAttribute("superadas",     superadas);
            request.setAttribute("suspensas",     suspensas);
            request.setAttribute("odiadas",      odiadas);
            request.setAttribute("favoritas",     favoritas);
            request.setAttribute("porcentaje",    porcentaje);
            request.setAttribute("tfg", tfg);
            request.setAttribute("practicas", practicas);
            request.setAttribute("circunferencia", String.format("%.1f", circunferencia).replace(",", "."));
            request.setAttribute("dashoffset",     String.format("%.1f", dashoffset).replace(",", "."));

            request.getRequestDispatcher("progreso.jsp").forward(request, response);

        } catch (Exception e) {
            response.getWriter().println("<p>Error: " + e.getMessage() + "</p>");
        }
    }
}
