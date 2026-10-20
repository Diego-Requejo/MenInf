package com.uva.meninf.controlador;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import com.uva.meninf.modelo.dao.UsuarioDAO;

import java.io.IOException;

public class RegistrarUsuario extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String user = request.getParameter("username");
        String pass = request.getParameter("password");

        try{
            UsuarioDAO dao = new UsuarioDAO();
            if (dao.existeUsuario(user)) {
                response.getWriter().write("EXISTE");
                return;
            }

            dao.registrar(user, pass);

            // CREAR SESIÓN TRAS INICIAR
            HttpSession session = request.getSession(true);
            session.setAttribute("usuario", user);

            response.getWriter().write("OK");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("ERROR: " + e.getMessage());
        }
    }
}