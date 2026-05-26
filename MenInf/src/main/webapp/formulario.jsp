<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, javax.sql.DataSource, javax.naming.*, java.util.*" %>
<%
    List<String[]> asignaturas = new ArrayList<>();
    try {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
        Connection conn = ds.getConnection();

        PreparedStatement ps = conn.prepareStatement(
            "SELECT cod, nombre FROM Asignatura ORDER BY nombre"
        );
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            asignaturas.add(new String[]{
                rs.getString("cod"),
                rs.getString("nombre")
            });
        }
        rs.close();
        ps.close();
        conn.close();
    } catch (Exception e) {
        out.println("<p>Error cargando asignaturas: " + e.getMessage() + "</p>");
    }
%>

<%
    //
    // Datos previos del usuario
    String[] codsFavoritas = new String[0];
    String[] codsOdiadas   = new String[0];
    String[] codssuperadas = new String[0];
    String[] codsSuspensas = new String[0];
    Map<String, Integer> notasLenguajes = new HashMap<>();

    String nomU = (String) session.getAttribute("usuario");

    if (nomU != null) {
        try {
            Context ctx2 = new InitialContext();
            DataSource ds2 = (DataSource) ctx2.lookup("java:comp/env/jdbc/meninf");
            Connection conn2 = ds2.getConnection();

            // Cargar cursadas
            PreparedStatement psCursadas = conn2.prepareStatement(
                "SELECT codA, estado, favorita, odiada FROM Cursada WHERE nomU = ?"
            );
            psCursadas.setString(1, nomU);
            ResultSet rsCursadas = psCursadas.executeQuery();

            List<String> favList  = new ArrayList<>();
            List<String> odiaList = new ArrayList<>();
            List<String> supList  = new ArrayList<>();
            List<String> susList  = new ArrayList<>();

            while (rsCursadas.next()) {
                String cod    = rsCursadas.getString("codA");
                String estado = rsCursadas.getString("estado");
                if (rsCursadas.getBoolean("favorita")) favList.add(cod);
                if (rsCursadas.getBoolean("odiada"))   odiaList.add(cod);
                if ("superada".equals(estado)) supList.add(cod);
                if ("suspensa".equals(estado)) susList.add(cod);
            }
            rsCursadas.close();
            psCursadas.close();

            codsFavoritas = favList.toArray(new String[0]);
            codsOdiadas   = odiaList.toArray(new String[0]);
            codssuperadas  = supList.toArray(new String[0]);
            codsSuspensas = susList.toArray(new String[0]);

            // Cargar preferencias de lenguajes
            PreparedStatement psLenguajes = conn2.prepareStatement(
                "SELECT nomL, nota FROM PreferenciaLenguaje WHERE nomU = ?"
            );
            psLenguajes.setString(1, nomU);
            ResultSet rsLenguajes = psLenguajes.executeQuery();
            while (rsLenguajes.next()) {
                notasLenguajes.put(rsLenguajes.getString("nomL"), rsLenguajes.getInt("nota"));
            }
            rsLenguajes.close();
            psLenguajes.close();
            conn2.close();

        } catch (Exception e) {
            out.println("<p>Error cargando datos previos: " + e.getMessage() + "</p>");
        }
    }
%>

<%!
    boolean contiene(String[] array, String valor) {
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
                        <% for (String[] a : asignaturas) { %>
                            <option value="<%= a[0] %>"
                                <%= contiene(codsFavoritas, a[0]) ? "selected" : "" %>>
                                <%= a[1] %>
                            </option>
                        <% } %>
                    </select>
                </div>

                <div class="columna-asignaturas">
                    <label for="odiadas">Asignaturas más odiadas<span style="color: red;">*</span></label>
                    <select id="odiadas" name="odiadas" multiple required>
                        <% for (String[] a : asignaturas) { %>
                            <option value="<%= a[0] %>"
                                <%= contiene(codsOdiadas, a[0]) ? "selected" : "" %>>
                                <%= a[1] %>
                            </option>
                        <% } %>
                    </select>
                </div>

                <div class="columna-asignaturas">
                    <label for="superadas">Asignaturas superadas</label>
                    <select id="superadas" name="superadas" multiple>
                        <% for (String[] a : asignaturas) { %>
                            <option value="<%= a[0] %>"
                                <%= contiene(codssuperadas, a[0]) ? "selected" : "" %>>
                                <%= a[1] %>
                            </option>
                        <% } %>
                    </select>
                </div>

                <div class="columna-asignaturas">
                    <label for="suspensas">Asignaturas suspensas</label>
                    <select id="suspensas" name="suspensas" multiple>
                        <% for (String[] a : asignaturas) { %>
                            <option value="<%= a[0] %>"
                                <%= contiene(codsSuspensas, a[0]) ? "selected" : "" %>>
                                <%= a[1] %>
                            </option>
                        <% } %>
                    </select>
                </div>

            </div>

            <!-- Valoraciones de lenguajes -->
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
    </body>
</html>