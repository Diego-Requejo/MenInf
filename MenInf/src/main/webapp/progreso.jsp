<%-- 
    Document   : progreso
    Created on : 23 abr 2026, 13:59:02
    Author     : dierequ
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%
    String nomU        = (String) request.getAttribute("nomU");
    String biografia   = (String) request.getAttribute("biografia");
    String mencion     = (String) request.getAttribute("mencion");
    int porcentaje     = (Integer) request.getAttribute("porcentaje");
    String circ        = (String) request.getAttribute("circunferencia");
    String dash        = (String) request.getAttribute("dashoffset");
    List<String> superadas = (List<String>) request.getAttribute("superadas");
    List<String> suspensas = (List<String>) request.getAttribute("suspensas");
    List<String> odiadas  = (List<String>) request.getAttribute("odiadas");
    List<String> favoritas = (List<String>) request.getAttribute("favoritas");
    
    Boolean tfgObj = (Boolean) request.getAttribute("tfg");
    boolean tieneTfg = (tfgObj != null) ? tfgObj : false;

    Boolean practicasObj = (Boolean) request.getAttribute("practicas");
    boolean tienePracticas = (practicasObj != null) ? practicasObj : false;
%>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>MenInf - Descubre tu camino</title>
        <link rel="stylesheet" href="style.css?v=2">
        <link rel="icon" type="image/png" href="./img/Logotipo.png">
        <style>
    .circulo-progreso {
        stroke-dasharray: <%= circ %>;
        /* Inicializamos el offset igual a la circunferencia para que empiece vacío */
        stroke-dashoffset: <%= circ %>; 
        animation: llenar-circulo 1s ease forwards;
    }
    
    @keyframes llenar-circulo {
        to {
            /* Termina en el valor calculado del progreso */
            stroke-dashoffset: <%= dash %>; 
        }
    }
</style>
    </head>

    <body>
        <div class="popup" id="popup">
            <div id="contenido-popup"></div>
        </div>
        <header>
            <div class="icono">
                <a href="./index.html">
                    <img src="./img/Logotipo.png" alt="logo">
                </a>
                <span>MenInf - Descubre tu camino</span>
            </div>
            <div class="icono" id="usuario">
                <img src="./img/Usuario.png" alt="usuario">
            </div>
        </header>

        <div class="contenido-perfil">

            <!-- Columna izquierda -->
            <div class="columna-izq">

                <!-- Círculo de progreso -->
                <div class="circulo-contenedor">
                    <div class="circulo">
                        <svg viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg">
                            <defs>
                                <linearGradient id="gradCirculo" x1="0%" y1="0%" x2="100%" y2="0%">
                                    <stop offset="0%"   stop-color="#e9d5a1"/>
                                    <stop offset="100%" stop-color="#f9c16d"/>
                                </linearGradient>
                            </defs>
                            <circle class="circulo-fondo"    cx="60" cy="60" r="54"/>
                            <circle class="circulo-progreso" cx="60" cy="60" r="54"/>
                        </svg>
                        <div class="circulo-texto"><%= porcentaje %> %</div>
                    </div>
                </div>

                <!-- Formulario de edición de perfil -->
                <form action="ActualizarPerfil" method="POST" style="margin: 0;">

                    <div class="campo">
                        <label>Nombre de usuario: <b><%= nomU %></b></label>
                    </div>

                    <div class="campo">
                        <label>Mencion que está cursando</label>
                        <select name="mencion">
                            <option value="PreMención" <%= "PreMención".equals(mencion) ? "selected" : "" %>>Ninguna (no elegida)</option>
                            <option value="TI"         <%= "TI".equals(mencion)         ? "selected" : "" %>>Tecnologías de la Información (TI)</option>
                            <option value="IS"         <%= "IS".equals(mencion)         ? "selected" : "" %>>Ingeniería del Software (IS)</option>
                            <option value="CO"         <%= "CO".equals(mencion)         ? "selected" : "" %>>Computación (CO)</option>
                        </select>
                    </div>

                    <div class="campo">
                        <label>Biografía</label>
                        <textarea name="biografia" placeholder="Escriba aquí acerca suyo"><%= biografia %></textarea>
                    </div>
                    
                    <div class="campo-checkboxes">
                        <label class="titulo-secundario">TFG y prácticas en empresa</label>
                        <div class="fila-controles">
                            <label class="etiqueta-checkbox">
                                <input type="checkbox" name="practicas" value="true" <%= tienePracticas ? "checked" : "" %>>
                                Prácticas en empresa
                            </label>

                            <label class="etiqueta-checkbox">
                                <input type="checkbox" name="tfg" value="true" <%= tieneTfg ? "checked" : "" %>>
                                TFG defendido
                            </label>
                        </div>
                    </div>

                    <div class="botonesProgreso-div">
                        <button type="submit">Guardar cambios</button>
                    </div>

                </form>

            </div>

            <!-- Columna derecha: listas de asignaturas -->
            <div class="columna-der">

                <div class="bloque-lista">
                    <label>Asignaturas superadas (<%= superadas.size() %>)</label>
                    <select size="2">
                        <% if (superadas.isEmpty()) { %>
                            <option disabled>Sin asignaturas superadas</option>
                        <% } else { for (String a : superadas) { %>
                            <option><%= a %></option>
                        <% } } %>
                    </select>
                </div>

                <div class="bloque-lista">
                    <label>Asignaturas suspensas (<%= suspensas.size() %>)</label>
                    <select size="2">
                        <% if (suspensas.isEmpty()) { %>
                            <option disabled>Sin asignaturas suspensas</option>
                        <% } else { for (String a : suspensas) { %>
                            <option><%= a %></option>
                        <% } } %>
                    </select>
                </div>

                <div class="bloque-lista">
                    <label>Asignaturas odiadas (<%= odiadas.size() %>)</label>
                    <select size="2">
                        <% if (odiadas.isEmpty()) { %>
                            <option disabled>Sin asignaturas odiadas</option>
                        <% } else { for (String a : odiadas) { %>
                            <option><%= a %></option>
                        <% } } %>
                    </select>
                </div>

                <div class="bloque-lista">
                    <label>Asignaturas favoritas (<%= favoritas.size() %>)</label>
                    <select size="2">
                        <% if (favoritas.isEmpty()) { %>
                            <option disabled>Sin asignaturas favoritas</option>
                        <% } else { for (String a : favoritas) { %>
                            <option><%= a %></option>
                        <% } } %>
                    </select>
                </div>

                <div class="botonesProgreso-div" id="botonRehacerFormulario">
                    <button onclick="location.href='formulario.jsp'">Rehacer formulario</button>
                </div>

            </div>
        </div>

       
        <script src="./js/popup.js"></script>
    </body>
</html>
