package com.uva.meninf.controlador;

import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.util.ArrayList;
import com.uva.meninf.modelo.entidades.Asignatura;
import com.uva.meninf.modelo.entidades.Periodo;

public class ObtenerPeriodos extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();

        String idStr = request.getParameter("id");

        try {

            int cod = Integer.parseInt(idStr);
            
            ArrayList<Periodo> lista =
                    Periodo.obtenerPorAsignatura(cod);

            StringBuilder json = new StringBuilder();

            json.append("[");

            for(int i=0; i<lista.size(); i++) {

                Periodo p = lista.get(i);
                
                Asignatura a = Asignatura.obtenerPorId(p.getCodA());

                json.append("{");

                json.append("\"idPeriodo\":\"")
                        .append(p.getIdPeriodo())
                        .append("\",");

                json.append("\"grupo\":\"")
                        .append(p.getGrupo())
                        .append("\",");

                json.append("\"dia\":")
                        .append(p.getDia())
                        .append(",");

                json.append("\"horaIni\":")
                        .append(p.getHoraIni())
                        .append(",");

                json.append("\"horaFin\":")
                        .append(p.getHoraFin())
                        .append(",");

                json.append("\"codA\":")
                        .append(p.getCodA())
                        .append(",");
                
                json.append("\"abreviatura\":\"")
                        .append(a.getAbreviatura())
                        .append("\"");
                       
                json.append("}");

                if(i < lista.size()-1)
                    json.append(",");
            }

            json.append("]");

            out.print(json.toString());

        } catch(Exception e) {

            response.setStatus(400);

            out.print("{\"error\":\"Error\"}");
        }

        out.close();
    }
}