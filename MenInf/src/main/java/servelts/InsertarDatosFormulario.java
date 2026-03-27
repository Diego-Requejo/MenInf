/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servelts;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Para establecer la conexión con la base de datos
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;



//Para controlar el contesto con el que trabajamos:
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 *
 * @author ASUS
 */
@WebServlet(name = "InsertarDatosFormulario", urlPatterns = {"/InsertarDatosFormulario"})
public class InsertarDatosFormulario extends HttpServlet {

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        /*Aquí hay añadir una serie de instrucciones para recoger los datos del
        formulario pero como no sé todavía exactamnete como hacerlo con lo que 
        nosotros tenemos he hecho fotos del ejemplo que ha hecho el profe en
        clase y las he mandado por el grupo*/
        
        
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
