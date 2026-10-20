<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="com.uva.meninf.modelo.dao.AsignaturaDAO" %>
<%@ page import="com.uva.meninf.modelo.dao.UsuarioDAO" %>
<%@ page import="com.uva.meninf.modelo.dao.FormularioDAO" %>
<%@ page import="com.uva.meninf.modelo.entidades.Asignatura" %>
<%@ page import="com.uva.meninf.modelo.dto.DatosProgreso" %>

<%
    List<Asignatura> asignaturas = new ArrayList<>();
    try {
        AsignaturaDAO asigDAO = new AsignaturaDAO();
        asignaturas = asigDAO.listarTodas();
    } catch (Exception e) {
        out.println("<p>Error cargando asignaturas: " + e.getMessage() + "</p>");
    }
%>

<%
    String[] codsFavoritas = new String[0];
    String[] codsOdiadas   = new String[0];
    String[] codssuperadas = new String[0];
    String[] codsSuspensas = new String[0];
    Map<String, Integer> notasLenguajes = new HashMap<>();

    String nomU = (String) session.getAttribute("usuario");
    if (nomU != null) {
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            DatosProgreso progreso = usuarioDAO.obtenerDatosProgreso(nomU);

            codsFavoritas = progreso.favoritas.keySet().stream().map(String::valueOf).toArray(String[]::new);
            codsOdiadas   = progreso.odiadas.keySet().stream().map(String::valueOf).toArray(String[]::new);
            codssuperadas = progreso.superadas.keySet().stream().map(String::valueOf).toArray(String[]::new);
            codsSuspensas = progreso.suspensas.keySet().stream().map(String::valueOf).toArray(String[]::new);

            FormularioDAO formDAO = new FormularioDAO();
            notasLenguajes = formDAO.obtenerPreferenciasLenguaje(nomU);
        } catch (Exception e) {
            out.println("<p>Error cargando datos previos: " + e.getMessage() + "</p>");
        }
    }
%>

<%!
    boolean contiene(String[] array, String valor) {
        if (array == null) return false;
        for (String s : array)
            if (s.equals(valor)) return true;
        return false;
    }
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>MenInf - Descubre tu camino</title>
        <link rel="stylesheet" href="style.css">
        <link rel="icon" type="image/png" href="./img/Logotipo.png">
    </head>

    <body>
        <div class="popup" id="popup">
            <div id="contenido-popup"></div>
        </div>

        <% Boolean mostrarPopup = (Boolean) request.getAttribute("mostrarPopup"); %>
        <div class="popup <%= (mostrarPopup != null && mostrarPopup) ? "activo" : "" %>" id="popupRecomendacion">
            <div id="contenido-popup-recomendacion">
                <% if (mostrarPopup != null && mostrarPopup) { 
                    String mencionRec = (String) request.getAttribute("mencionRecomendada");
                    List<String> nombresC1 = (List<String>) request.getAttribute("nombresCuatri1");
                    List<String> nombresC2 = (List<String>) request.getAttribute("nombresCuatri2");
                %>
                    <div class="contenedor-detalle-asignatura">
                        <div class="header-asignatura">
                            <h1 class="titulo0">RECOMENDACIÓN ACADÉMICA</h1>
                            <div class="cerrar-asignatura" id="btn-cerrar-recomendacion">X</div>
                        </div>
                        
                        <div style="margin-bottom: 20px; text-align: center;">
                            <h2 class="titulo2" style="font-size: 1.6rem;">
                                Mención recomendada: <span style="color: #feebba; font-weight: bold;"><%= mencionRec %></span>
                            </h2>
                            <h2 class="titulo2" style="font-size: 1.6rem;">
                                Asignaturas recomendadas:
                            </h2>
                        </div>

                        <%-- Usamos tu clase nativa "especificaciones" que crea el grid de 3 columnas --%>
                        <div class="especificaciones">
                            
                            <div class="caja-izquierda-asignaturas">
                                <div>
                                    <div class="titulo2" style="margin-bottom: 10px;">1er Cuatrimestre</div>
                                    <%-- Usamos tu clase nativa "lista-asignaturas" para crear el recuadro con scroll --%>
                                    <div class="lista-asignaturas">
                                        <% if (nombresC1 == null || nombresC1.isEmpty()) { %>
                                            <div class="elemento-lista">No hay asignaturas recomendadas para este periodo</div>
                                        <% } else { 
                                            for (String n : nombresC1) { %>
                                                <div class="elemento-lista">• <%= n %></div>
                                        <%  } 
                                           } %>
                                    </div>
                                </div>
                            </div>

                            <div class="divisor"></div>

                            <div class="caja-derecha-asignaturas">
                                <div>
                                    <div class="titulo2" style="margin-bottom: 10px;">2º Cuatrimestre</div>
                                    <div class="lista-asignaturas">
                                        <% if (nombresC2 == null || nombresC2.isEmpty()) { %>
                                            <div class="elemento-lista">No hay asignaturas recomendadas para este periodo</div>
                                        <% } else { 
                                            for (String n : nombresC2) { %>
                                                <div class="elemento-lista">• <%= n %></div>
                                        <%  } 
                                           } %>
                                    </div>
                                </div>
                            </div>

                        </div>

                        <div style="display: flex; gap: 20px; justify-content: center; margin-top: 35px; padding-bottom: 10px;">
                            <button type="button" id="btn-ver-progreso" class="boton-enviar" style="width: auto; padding: 10px 30px; cursor: pointer;">Ver mi progreso</button>
                            <button type="button" id="btn-ver-horario" class="boton-enviar" style="width: auto; padding: 10px 30px; background-color: #28a745; border-color: #28a745; cursor: pointer;">Ver mi horario</button>
                        </div>
                    </div>
                <% } %>
            </div>
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

        <form action="DatosFormulario" method="POST">

            <h1>Formulario de afinidad académica</h1>
            
            <%-- Bloque para mostrar errores provenientes del Servlet --%>
            <% 
                String errorValidacion = (String) request.getAttribute("errorValidacion");
                if (errorValidacion != null) { 
            %>
                <div style="color: white; background-color: #d9534f; padding: 15px; margin-bottom: 20px; border-radius: 4px; text-align: center; font-weight: bold; border: 1px solid #d43f3a;">
                    <%= errorValidacion %>
                </div>
            <% } %>

            <div class="grid-asignaturas">

                <div class="columna-asignaturas">
                    <label for="favoritas">Asignaturas favoritas<span style="color: red;">*</span></label>
                    <select id="favoritas" name="favoritas" multiple required>
                        <% for (Asignatura a : asignaturas) { %>
                            <option value="<%= a.getCod() %>" <%= contiene(codsFavoritas, String.valueOf(a.getCod())) ? "selected" : "" %>>
                                <%= a.getNombre() %>
                            </option>
                        <% } %>
                    </select>
                </div>

                <div class="columna-asignaturas">
                    <label for="odiadas">Asignaturas más odiadas<span style="color: red;">*</span></label>
                    <select id="odiadas" name="odiadas" multiple required>
                        <% for (Asignatura a : asignaturas) { %>
                            <option value="<%= a.getCod() %>" <%= contiene(codsOdiadas, String.valueOf(a.getCod())) ? "selected" : "" %>>
                                <%= a.getNombre() %>
                            </option>
                        <% } %>
                    </select>
                </div>

                <div class="columna-asignaturas">
                    <label for="superadas">Asignaturas superadas</label>
                    <select id="superadas" name="superadas" multiple>
                        <% for (Asignatura a : asignaturas) { %>
                            <option value="<%= a.getCod() %>" <%= contiene(codssuperadas, String.valueOf(a.getCod())) ? "selected" : "" %>>
                                <%= a.getNombre() %>
                            </option>
                        <% } %>
                    </select>
                </div>

                <div class="columna-asignaturas">
                    <label for="suspensas">Asignaturas suspensas</label>
                    <select id="suspensas" name="suspensas" multiple>
                        <% for (Asignatura a : asignaturas) { %>
                            <option value="<%= a.getCod() %>" <%= contiene(codsSuspensas, String.valueOf(a.getCod())) ? "selected" : "" %>>
                                <%= a.getNombre() %>
                            </option>
                        <% } %>
                    </select>
                </div>

            </div>

            <div class="grid-lenguajes">

                <div class="valoracion">
                    <label>Nivel de manejo y gusto por el lenguaje Python</label>
                    <div class="estrellas">
                        <% for (int i = 5; i >= 1; i--) { %>
                            <input type="radio" id="python<%= i %>" name="python" value="<%= i %>"
                                <%= notasLenguajes.getOrDefault("Python", 0) == i ? "checked" : "" %>>
                            <label class="estrella" for="python<%= i %>">★</label>
                        <% } %>
                    </div>
                </div>

                <div class="valoracion">
                    <label>Nivel de manejo y gusto por el lenguaje Ensamblador (MIPS)</label>
                    <div class="estrellas">
                        <% for (int i = 5; i >= 1; i--) { %>
                            <input type="radio" id="mips<%= i %>" name="mips" value="<%= i %>"
                                <%= notasLenguajes.getOrDefault("Ensamblador MIPS", 0) == i ? "checked" : "" %>>
                            <label class="estrella" for="mips<%= i %>">★</label>
                        <% } %>
                    </div>
                </div>

                <div class="valoracion">
                    <label>Nivel de manejo y gusto por el lenguaje Java</label>
                    <div class="estrellas">
                        <% for (int i = 5; i >= 1; i--) { %>
                            <input type="radio" id="java<%= i %>" name="nivelJava" value="<%= i %>"
                                <%= notasLenguajes.getOrDefault("Java", 0) == i ? "checked" : "" %>>
                            <label class="estrella" for="java<%= i %>">★</label>
                        <% } %>
                    </div>
                </div>

                <div class="valoracion">
                    <label>Nivel de manejo y gusto por el lenguaje C</label>
                    <div class="estrellas">
                        <% for (int i = 5; i >= 1; i--) { %>
                            <input type="radio" id="c<%= i %>" name="c" value="<%= i %>"
                                <%= notasLenguajes.getOrDefault("C", 0) == i ? "checked" : "" %>>
                            <label class="estrella" for="c<%= i %>">★</label>
                        <% } %>
                    </div>
                </div>

            </div>

            <button type="submit" id="botonFormulario">Descubre tu camino</button>

        </form>

        <script src="./js/popup.js"></script>
        <%-- NUEVO: Script de control independiente para el popup de recomendación --%>
        <script src="./js/popupRecomendacion.js"></script>
    </body>
</html>