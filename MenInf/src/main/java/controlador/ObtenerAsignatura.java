package controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.entidades.Asignatura;

public class ObtenerAsignatura extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Código no proporcionado\"}");
            return;
        }

        try {
            int cod = Integer.parseInt(idStr);
            Asignatura asig = Asignatura.obtenerPorId(cod);

            if (asig != null) {
                // Construcción manual de JSON
                StringBuilder json = new StringBuilder();
                json.append("{");
                json.append("\"cod\":").append(asig.getCod()).append(",");
                json.append("\"nombre\":\"").append(escape(asig.getNombre())).append("\",");
                json.append("\"abreviatura\":\"").append(escape(asig.getAbreviatura())).append("\",");
                json.append("\"cuatri\":").append(asig.getCuatri()).append(",");
                json.append("\"menciones\":\"").append(escape(asig.getMenciones())).append("\",");
                json.append("\"curso\":\"").append(escape(asig.getCursoAcademico())).append("\",");
                
                json.append("\"listaProfesores\":[ ");
                ArrayList<modelo.entidades.Profesor> profes = asig.getProfesores();
                if(profes != null){
                    for(int i = 0; i < profes.size(); i++){
                        json.append("{");
                        json.append("\"nombre\":\"").append(escape(profes.get(i).getNombre())).append("\",");
                        json.append("\"correo\":\"").append(escape(profes.get(i).getCorreo())).append("\"");
                        json.append("}");
                        if(i < profes.size() - 1) json.append(", ");
                    }
                }
                json.append("],");     
                           
                json.append("\"requisitos\":\"").append(escape(asig.getRequisitos())).append("\",");
                
                json.append("\"listaLenguajes\":[ ");
                ArrayList<modelo.entidades.Lenguaje> lengs = asig.getLenguajes();
                if(lengs != null){
                    for(int i = 0; i < lengs.size(); i++){
                        json.append("{");
                        json.append("\"nombre\":\"").append(escape(lengs.get(i).getNombre())).append("\"");
                        json.append("}");
                        if(i < lengs.size() - 1) json.append(", ");
                    }
                }
                json.append("]");

                json.append("}");
                out.print(json.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Asignatura no encontrada\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Código inválido\"}");
        } finally {
            out.close();
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
