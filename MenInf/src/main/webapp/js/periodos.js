let periodosPorCuatri = {
    1: [],
    2: []
};

let usarColoresAsignatura = false;

const PALETA_BASE = [

    // Rojos
    0,
    8,
    15,

    // Naranjas
    25,
    32,

    // Amarillos
    45,
    55,

    // Verdes
    90,
    105,
    120,
    135,

    // Turquesas
    160,
    175,

    // Azules
    200,
    215,
    230,

    // Morados
    255,
    270,
    285,

    // Rosas
    310,
    330
];

async function cargarRecomendacion() {
    const res = await fetch("GetResultadoJson");
    
    if (!res.ok) {
        console.warn("No hay recomendación en sesión");
        return;
    }

    const data = await res.json();
    // data.mencion  → "IS" | "TI" | "CO"
    // data.cuatri1  → [46901, 46902, ...]
    // data.cuatri2  → [46906, 46907, ...]

    mostrarHorario(data.mencion, data.cuatri1, data.cuatri2);
}

function initHorario(mencion, cuatri1, cuatri2) {
    console.log("Mención:", mencion);
    console.log("Cuatri 1:", cuatri1);
    console.log("Cuatri 2:", cuatri2);
    // reservado para inicialización futura
}

/* =========================================================
   AGRUPAR SOLAPAMIENTOS (día + hora)
========================================================= */
function getSolapamientos() {

    const mapa = {};

    periodosPorCuatri[cuatriActivo].forEach(p => {

        for (let h = p.horaIni; h < p.horaFin; h++) {

            const key = `${p.dia}-${h}`;

            if (!mapa[key]) mapa[key] = [];

            mapa[key].push(p);
        }
    });

    return mapa;
}

/* =========================================================
   POSICIÓN dentro del grupo de solapamiento
========================================================= */
function getPosicionEnSolape(p, h, solapamientos) {

    const key = `${p.dia}-${h}`;
    const grupo = solapamientos[key];

    if (!grupo) return 0;

    const ordenado = [...grupo].sort((a, b) => a.codA - b.codA);

    return ordenado.findIndex(x => x.idPeriodo === p.idPeriodo);
}

/* =========================================================
   TOGGLE asignatura
========================================================= */
function toggleAsignatura(codA) {

    const lista = periodosPorCuatri[cuatriActivo];

    const yaExiste = lista.some(p => p.codA === codA);

    if (yaExiste) {
        periodosPorCuatri[cuatriActivo] =
            lista.filter(p => p.codA !== codA);
        renderHorario();
        return;
    }

    fetch(`ObtenerPeriodos?id=${codA}`)
        .then(res => res.json())
        .then(periodos => {

            periodosPorCuatri[cuatriActivo].push(...periodos);
            renderHorario();
        });
}

/* =========================================================
   ELIMINAR asignatura
========================================================= */
function eliminarAsignatura(codA) {

    periodosPorCuatri[cuatriActivo] =
        periodosPorCuatri[cuatriActivo].filter(p => p.codA !== codA);

    renderHorario();
}

/* =========================================================
   CALCULAR LAYOUT SIN COLISIONES
========================================================= */
function calcularLayout(periodosSeleccionados) {

    // Para cada celda (día-hora), guardamos qué slot ocupa cada periodo
    // slots[key] = { slotIndex: idPeriodo, ... }
    const slots = {};

    // Resultado: idPeriodo -> { slot, totalSlots }
    const layout = {};

    // Ordenar por duración descendente para que los bloques grandes
    // reserven primero su columna coherente
    const ordenados = [...periodosSeleccionados].sort(
        (a, b) => (b.horaFin - b.horaIni) - (a.horaFin - a.horaIni)
    );

    ordenados.forEach(p => {

        // Recopilar todas las celdas que ocupa este periodo
        const celdas = [];
        for (let h = p.horaIni; h < p.horaFin; h++) {
            celdas.push(`${p.dia}-${h}`);
        }

        // Inicializar mapas de slots para cada celda si no existen
        celdas.forEach(key => {
            if (!slots[key]) slots[key] = {};
        });

        // Encontrar el primer slot libre en TODAS las celdas a la vez
        let slotElegido = 0;
        while (true) {
            const libreEnTodas = celdas.every(
                key => slots[key][slotElegido] === undefined
            );
            if (libreEnTodas) break;
            slotElegido++;
        }

        // Reservar ese slot en todas las celdas
        celdas.forEach(key => {
            slots[key][slotElegido] = p.idPeriodo;
        });

        layout[p.idPeriodo] = { slot: slotElegido };
    });

    // Segunda pasada: calcular totalSlots por celda y el máximo que afecta a cada periodo
    const totalPorCelda = {};
    periodosSeleccionados.forEach(p => {
        for (let h = p.horaIni; h < p.horaFin; h++) {
            const key = `${p.dia}-${h}`;
            totalPorCelda[key] = Object.keys(slots[key] || {}).length;
        }
    });

    periodosSeleccionados.forEach(p => {
        let maxTotal = 1;
        for (let h = p.horaIni; h < p.horaFin; h++) {
            const key = `${p.dia}-${h}`;
            maxTotal = Math.max(maxTotal, totalPorCelda[key] || 1);
        }
        layout[p.idPeriodo].totalSlots = maxTotal;
    });

    return layout;
}

function cambiarCuatri(nuevo) {
    cuatriActivo = nuevo;
    renderHorario();
}

function toggleModoColores() {

    usarColoresAsignatura =
        !usarColoresAsignatura;

    actualizarBotonColores();

    renderHorario();
}

function actualizarBotonColores() {

    const btn =
        document.getElementById("btn-toggle-colores");

    if (!btn) return;

    if (usarColoresAsignatura) {

        btn.textContent =
            "Colores: ON";

        btn.classList.add("activo");
        btn.classList.remove("inactivo");
    }
    else {

        btn.textContent =
            "Colores: OFF";

        btn.classList.remove("activo");
        btn.classList.add("inactivo");
    }
}

function hashCode(str) {

    str = String(str);

    let hash = 0;

    for (let i = 0; i < str.length; i++) {

        hash =
            ((hash << 5) - hash)
            + str.charCodeAt(i);

        hash |= 0;
    }

    return Math.abs(hash);
}

/* =========================================================
   COLOR CONSISTENTE POR ASIGNATURA
========================================================= */
function getColorAsignatura(codA, esLaboratorio = false) {

    const hash = hashCode(codA);

    // Elegir hue desde paleta
    const hue = (hash * 137.508) % 360;

    // Pequeña variación para evitar clones exactos
    const hueOffset =
        (hash % 7) - 3;

    const finalHue =
        hue + hueOffset;

    let saturation;
    let lightness;

    // TEORÍA
    if (!esLaboratorio) {

        saturation = 90;
        lightness = 72;
    }

    // LABORATORIO
    else {
        saturation = 90;
        lightness = 60;
    }

    const background =
        `hsl(${finalHue}, ${saturation}%, ${lightness}%)`;

    return background;
}

function calcularLayout(periodos) {

    const layout = {};

    // =====================================================
    // AGRUPAR POR DÍA
    // =====================================================

    const porDia = {};

    periodos.forEach(p => {

        if (!porDia[p.dia]) {
            porDia[p.dia] = [];
        }

        porDia[p.dia].push(p);
    });

    // =====================================================
    // PROCESAR CADA DÍA
    // =====================================================

    Object.values(porDia).forEach(listaDia => {

        // -------------------------------------------------
        // 1. Construir grafo de conflictos
        // -------------------------------------------------

        const grafo = new Map();

        listaDia.forEach(p => {
            grafo.set(p.idPeriodo, []);
        });

        for (let i = 0; i < listaDia.length; i++) {

            for (let j = i + 1; j < listaDia.length; j++) {

                const a = listaDia[i];
                const b = listaDia[j];

                const solapan =
                    a.horaIni < b.horaFin &&
                    b.horaIni < a.horaFin;

                if (solapan) {

                    grafo.get(a.idPeriodo).push(b);
                    grafo.get(b.idPeriodo).push(a);
                }
            }
        }

        // -------------------------------------------------
        // 2. Encontrar componentes conexas
        // -------------------------------------------------

        const visitados = new Set();

        const grupos = [];

        listaDia.forEach(p => {

            if (visitados.has(p.idPeriodo)) return;

            const grupo = [];

            const stack = [p];

            visitados.add(p.idPeriodo);

            while (stack.length) {

                const actual = stack.pop();

                grupo.push(actual);

                grafo.get(actual.idPeriodo).forEach(vecino => {

                    if (!visitados.has(vecino.idPeriodo)) {

                        visitados.add(vecino.idPeriodo);

                        stack.push(vecino);
                    }
                });
            }

            grupos.push(grupo);
        });

        // -------------------------------------------------
        // 3. Layout por grupo
        // -------------------------------------------------

        grupos.forEach(grupo => {

            grupo.sort((a, b) => {

                if (a.horaIni !== b.horaIni) {
                    return a.horaIni - b.horaIni;
                }

                return (b.horaFin - b.horaIni)
                     - (a.horaFin - a.horaIni);
            });

            const columnas = [];

            let totalCols = 0;

            grupo.forEach(p => {

                let col = 0;

                while (true) {

                    const conflicto = columnas[col]?.some(otro => {

                        return (
                            p.horaIni < otro.horaFin &&
                            otro.horaIni < p.horaFin
                        );
                    });

                    if (!conflicto) break;

                    col++;
                }

                if (!columnas[col]) {
                    columnas[col] = [];
                }

                columnas[col].push(p);

                totalCols = Math.max(totalCols, col + 1);

                layout[p.idPeriodo] = {
                    col
                };
            });

            // IMPORTANTÍSIMO:
            // mismo totalCols para TODO el grupo
            grupo.forEach(p => {

                layout[p.idPeriodo].totalCols = totalCols;
            });
        });
    });

    return layout;
}

/* =========================================================
   RENDER HORARIO
========================================================= */
function renderHorario() {

    const grid = document.getElementById("grid-horario");
    if (!grid) return;

    grid.innerHTML = "";

   const periodosSeleccionados =
    periodosPorCuatri[cuatriActivo];

const layout = calcularLayout(periodosSeleccionados);

const yaRenderizados = new Set();

periodosSeleccionados.forEach(p => {

    if (yaRenderizados.has(p.idPeriodo)) return;
    yaRenderizados.add(p.idPeriodo);

    const div = document.createElement("div");

    const esTeoria = p.grupo.startsWith("T");

    div.classList.add("bloque", "ver-asignatura");

    const esLaboratorio = !esTeoria;

    if (esLaboratorio) {
        div.classList.add("laboratorio");
    }

    if (usarColoresAsignatura) {
        const color = getColorAsignatura(
            p.codA,
            esLaboratorio
        );

        div.style.background = color;
    }
    else {

        // Modo clásico accesible
        div.style.background =
            esLaboratorio
                ? "#ff7a1a"
                : "#e9b86a";
    }

    div.dataset.id = p.codA;

    const rowStart = p.horaIni - 8;
    const rowSpan = p.horaFin - p.horaIni;

    div.style.gridColumn = p.dia;
    div.style.gridRow = `${rowStart} / span ${rowSpan}`;

    // Layout robusto
    const info = layout[p.idPeriodo];

    const width = 100 / info.totalCols;
    const left = width * info.col;

    div.style.position = "relative";
    div.style.width = `calc(${width}% - 4px)`;
    div.style.left = `${left}%`;

    div.innerText =
        `${p.abreviatura} - ${p.grupo}`;

    grid.appendChild(div);
});
}