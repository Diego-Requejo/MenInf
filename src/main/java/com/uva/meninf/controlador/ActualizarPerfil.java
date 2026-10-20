package com.uva.meninf.controlador;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.uva.meninf.modelo.dao.UsuarioDAO;

@WebServlet(name = "ActualizarPerfil", urlPatterns = {"/ActualizarPerfil"})
public class ActualizarPerfil extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        String nomU = (String) request.getSession().getAttribute("usuario");
        if (nomU == null) {
            response.sendRedirect("index.html");
            return;
        }

        String biografia = request.getParameter("biografia");
        String mencion   = request.getParameter("mencion");

        boolean tfg = "true".equals(request.getParameter("tfg"));
        boolean practicas = "true".equals(request.getParameter("practicas"));

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            usuarioDAO.actualizarPerfil(nomU, biografia, mencion, tfg, practicas);
            response.sendRedirect("MostrarProgreso");

        } catch (Exception e) {
            response.getWriter().println("<p>Error al guardar: " + e.getMessage() + "</p>");
        }
    }
}
