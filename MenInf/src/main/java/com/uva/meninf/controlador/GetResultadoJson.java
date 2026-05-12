package com.uva.meninf.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.uva.meninf.modelo.entidades.ResultadoFormulario;

/**
 * Devuelve el ResultadoFormulario almacenado en sesión como JSON.
 * El JavaScript del horario hace fetch("/GetResultadoJson") para obtenerlo.
 *
 * Formato de respuesta:
 * {
 *   "mencion": "IS",
 *   "cuatri1": [46901, 46902, 46903, 46904, 46905],
 *   "cuatri2": [46906, 46907, 46908, 46909, 46910]
 * }
 */
@WebServlet(name = "GetResultadoJson", urlPatterns = {"/GetResultadoJson"})
public class GetResultadoJson extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        ResultadoFormulario resultado =
            (ResultadoFormulario) request.getSession().getAttribute("resultadoFormulario");

        if (resultado == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"No hay resultado en sesión\"}");
            return;
        }

        Object[] array = resultado.comoArray();
        String mencion            = (String)         array[0];
        List<Integer> cuatri1     = (List<Integer>)  array[1];
        List<Integer> cuatri2     = (List<Integer>)  array[2];

        // Construir JSON manualmente (sin dependencias externas)
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"mencion\":\"").append(mencion).append("\",");
        json.append("\"cuatri1\":").append(listToJsonArray(cuatri1)).append(",");
        json.append("\"cuatri2\":").append(listToJsonArray(cuatri2));
        json.append("}");

        out.print(json.toString());
    }

    private String listToJsonArray(List<Integer> lista) {
        if (lista == null || lista.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(lista.get(i));
            if (i < lista.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
