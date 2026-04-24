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
        
        // 1. Obtener el usuario de la sesión
        String nomU = (String) request.getSession().getAttribute("usuario");
        if (nomU == null) {
            response.sendRedirect("index.html");
            return;
        }

        // 2. Capturar datos del formulario 
        String biografia = request.getParameter("biografia");
        String mencion = request.getParameter("mencion");
        
        // Lógica para los checkboxes: si es null, es que no se marcó
        boolean tienePracticas = request.getParameter("practicas") != null;
        boolean tieneTfg = request.getParameter("tfg") != null;

        try {
            // 3. Conexión a la base de datos (usando tu DataSource)
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();

            // 4. Sentencia SQL de actualización
            String sql = "UPDATE Usuario SET biografia = ?, mencion = ?, practicas = ?, tfg = ? WHERE nombre = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, biografia);
            ps.setString(2, mencion);
            ps.setBoolean(3, tienePracticas);
            ps.setBoolean(4, tieneTfg);
            ps.setString(5, nomU);

            ps.executeUpdate();

            ps.close();
            conn.close();

            // 5. Redirigir para refrescar los datos
            response.sendRedirect("MostrarProgreso");

        } catch (Exception e) {
            response.getWriter().println("Error al actualizar: " + e.getMessage());
        }
    }
}