package controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ObtenerProgreso extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String usuario = (String) request.getSession().getAttribute("usuario");
        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"No hay sesión activa\"}");
            return;
        }

        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();

            // Consulta que trae todas las asignaturas cursadas por el usuario con su nombre
            String sql = "SELECT c.codA, a.nombre, a.abreviatura, c.estado, c.favorita " +
                         "FROM Cursada c " +
                         "JOIN Asignatura a ON c.codA = a.cod " +
                         "WHERE c.nomU = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, usuario);
            ResultSet rs = ps.executeQuery();

            StringBuilder json = new StringBuilder();
            json.append("[");
            boolean primero = true;
            while (rs.next()) {
                if (!primero) json.append(",");
                json.append("{");
                json.append("\"cod\":").append(rs.getInt("codA")).append(",");
                json.append("\"nombre\":\"").append(rs.getString("nombre")).append("\",");
                json.append("\"abreviatura\":\"").append(rs.getString("abreviatura")).append("\",");
                json.append("\"estado\":\"").append(rs.getString("estado")).append("\",");
                json.append("\"favorita\":").append(rs.getBoolean("favorita"));
                json.append("}");
                primero = false;
            }
            json.append("]");

            out.print(json.toString());
            
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        } finally {
            out.close();
        }
    }
}
