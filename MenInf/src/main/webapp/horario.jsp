<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <title>MenInf - Horario</title>
        <link rel="stylesheet" href="style.css">
        <link rel="icon" type="image/png" href="./img/Logotipo.png">
    </head>
    <body>
        <div class="popup" id="popup">
            <div id="contenido-popup"></div>
        </div>
        <!-- HEADER -->
        <header>
            <div class="icono" >
                <a href="./index.html">
                <img src="./img/Logotipo.png" alt="logo" >
                </a>
                <span>MenInf - Descubre tu camino</span>
            </div>

            <div class="icono" id="usuario">
                <img src="./img/Usuario.png" alt="usuario">
            </div>
        </header>

        <main class="contenedor-principal-horario">

            <div class="horario">

                <!-- CABECERA -->
                <div class="cabecera-horario">
                    <div></div>
                    <div>Lunes</div>
                    <div>Martes</div>
                    <div>Miércoles</div>
                    <div>Jueves</div>
                    <div>Viernes</div>
                </div>

                <!-- CUERPO -->
                <div class="cuerpo-horario">

                    <!-- Horas -->
                    <div class="horas">
                        <div>9:00</div>
                        <div>10:00</div>
                        <div>11:00</div>
                        <div>12:00</div>
                        <div>13:00</div>
                        <div>14:00</div>
                        <div>15:00</div>
                        <div>16:00</div>
                        <div>17:00</div>
                        <div>18:00</div>
                        <div>19:00</div>
                        <div>20:00</div>
                    </div>

                    <!-- GRID -->
                    <div class="grid-horario" id="grid-horario"> </div>
                </div>
            </div>

            <!-- PANEL DERECHO -->
            <div class="panel-derecha-horario">
                <h2>ASIGNATURAS DISPONIBLES</h2>

                <div class="tabs">
                    <button id="btn-cuatri-1" class="activo">1º CUATRI</button>
                    <button id="btn-cuatri-2" class="inactivo">2º CUATRI</button>
                </div>

                <input type="text" id="buscardor-asignatura" placeholder="Buscar asignatura...">

                <h3>Filtrar por mención</h3>

                <div class="filtros">
                    <button class="inactivo">CO</button>
                    <button class="inactivo">IS</button>
                    <button class="inactivo">TI</button>
                </div>

                <div class="lista" id="contenedor-lista-asignaturas">
                    <div class="item">Cargando asignaturas...</div>
                </div>
                
                <button
                    id="btn-toggle-colores"
                    class="inactivo"
                    onclick="toggleModoColores()"
                >
                    Colores: ON
                </button>
            </div>

        </main>

        <script src="./js/popup.js"></script>
        <script src="./js/periodos.js"></script>
        <script src="./js/asignatura.js"></script>
    </body>
</html>