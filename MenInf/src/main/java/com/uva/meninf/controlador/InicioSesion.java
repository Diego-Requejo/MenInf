package com.uva.meninf.controlador;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.uva.meninf.modelo.entidades.Usuario;

import java.io.IOException;

public class InicioSesion extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String user = request.getParameter("username");
        String pass = request.getParameter("password");

        if (Usuario.login(user, pass)) {
            HttpSession session = request.getSession();
            session.setAttribute("usuario", user);

            response.getWriter().write("OK");
        }
    }
}