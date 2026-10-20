package com.uva.meninf.controlador;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class ComprobarSesion extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("usuario") != null) {
            response.getWriter().write((String) session.getAttribute("usuario"));
        } else {
            response.getWriter().write("");
        }
    }
}
