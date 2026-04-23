package controlador;
 
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
 
@WebServlet(name = "ActualizarPerfil", urlPatterns = {"/ActualizarPerfil"})
public class ActualizarPerfil extends HttpServlet {
 
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
 
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
 
        // Obtener usuario de sesión
        String nomU = (String) request.getSession().getAttribute("usuario");
        if (nomU == null) {
            response.sendRedirect("index.html");
            return;
        }
 
        // Recoger datos del formulario
        String biografia = request.getParameter("biografia");
        String mencion   = request.getParameter("mencion");
 
        // Validar mención
        if (!mencion.equals("IS") && !mencion.equals("CO") &&
            !mencion.equals("TI") && !mencion.equals("PreMención")) {
            response.sendRedirect("progreso.jsp?error=mencion");
            return;
        }
 
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();
 
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE Usuario SET biografia = ?, mencion = ? WHERE nombre = ?"
            );
            ps.setString(1, biografia);
            ps.setString(2, mencion);
            ps.setString(3, nomU);
            ps.executeUpdate();
 
            ps.close();
            conn.close();
 
            response.sendRedirect("MostrarProgreso");
 
        } catch (Exception e) {
            response.getWriter().println("<p>Error al guardar: " + e.getMessage() + "</p>");
        }
    }
}