package controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.Asignatura;

public class ListarAsignaturas extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            List<Asignatura> lista = Asignatura.listarTodas();
            
            // Construcción manual de Array JSON
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 0; i < lista.size(); i++) {
                Asignatura a = lista.get(i);
                json.append("{");
                json.append("\"cod\":").append(a.getCod()).append(",");
                json.append("\"nombre\":\"").append(escape(a.getNombre())).append("\",");
                json.append("\"abreviatura\":\"").append(escape(a.getAbreviatura())).append("\",");
                json.append("\"cuatri\":").append(a.getCuatri());
                json.append("}");
                if (i < lista.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            
            out.print(json.toString());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        } finally {
            out.close();
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
