package com.uva.meninf.controlador;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



import com.uva.meninf.modelo.dao.UsuarioDAO;
import com.uva.meninf.modelo.dto.DatosProgreso;

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
            UsuarioDAO dao = new UsuarioDAO();
            DatosProgreso datos = dao.obtenerDatosProgreso(nomU);

            int totalAsignaturas = 40;
            int progreso = datos.superadas.size();
            if (datos.tfg)       progreso += 2;
            if (datos.practicas) progreso += 2;
            int porcentaje = (int) Math.round((progreso * 100.0) / totalAsignaturas);

            double circunferencia = 2 * Math.PI * 54;
            double dashoffset = circunferencia * (1.0 - porcentaje / 100.0);

            request.setAttribute("nomU",          nomU);
            request.setAttribute("biografia",     datos.biografia);
            request.setAttribute("mencion",       datos.mencion);
            request.setAttribute("tfg",           datos.tfg);
            request.setAttribute("practicas",     datos.practicas);
            request.setAttribute("superadas",     datos.superadas);
            request.setAttribute("suspensas",     datos.suspensas);
            request.setAttribute("odiadas",       datos.odiadas);
            request.setAttribute("favoritas",     datos.favoritas);
            request.setAttribute("porcentaje",    porcentaje);
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
