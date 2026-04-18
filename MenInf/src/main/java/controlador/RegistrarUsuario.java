package controlador;

import modelo.UsuarioTODO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class RegistrarUsuario extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String user = request.getParameter("username");
        String pass = request.getParameter("password");

        if (UsuarioTODO.existeUsuario(user)) {
            response.getWriter().write("EXISTE");
            return;
        }

        UsuarioTODO.registrar(user, pass);

        // CREAR SESIÓN TRAS INICIAR
        HttpSession session = request.getSession(true);
        session.setAttribute("usuario", user);

        response.getWriter().write("OK");
    }
}