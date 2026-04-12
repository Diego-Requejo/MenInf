/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.uva.meninf;

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
        
        
        //1.- OBTENER DATOS FORMULARIOS
        
        // Recoger valores de los select múltiples (devuelven array)
        String[] favoritas = request.getParameterValues("favoritas");
        String[] odiadas   = request.getParameterValues("odiadas");
        String[] superadas = request.getParameterValues("superadas");
        String[] suspensas = request.getParameterValues("suspensas");
        
        // Recoger valoraciones de estrellas
        String python = request.getParameter("python");
        String mips   = request.getParameter("mips");
        String java   = request.getParameter("java");
        String c      = request.getParameter("c");
        
        // ID del usuario en sesión (asumiendo que está guardado en sesión)
        int idUsuario = (int) request.getSession().getAttribute("idUsuario");
        
        
        
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
