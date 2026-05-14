let periodosPorCuatri = {
    1: [],
    2: []
};


let usarColoresAsignatura = false;

const PALETA_BASE = [
    // Rojos
    0, 8, 15,
    // Naranjas
    25, 32,
    // Amarillos
    45, 55,
    // Verdes
    90, 105, 120, 135,
    // Turquesas
    160, 175,
    // Azules
    200, 215, 230,
    // Morados
    255, 270, 285,
    // Rosas
    310, 330
];

/* =========================================================
   CARGA INICIAL DEL HORARIO RECOMENDADO
   Llamado desde horario.jsp con los datos inyectados desde sesión.
   Carga los periodos de cada asignatura recomendada y renderiza.
========================================================= */
async function initHorario(mencion, cuatri1, cuatri2) {

    console.log("Mención recomendada:", mencion);
    console.log("Cuatri 1:", cuatri1);
    console.log("Cuatri 2:", cuatri2);

    // Cargar periodos de las asignaturas del cuatri 1
    for (const cod of cuatri1) {
        await cargarPeriodosEnCuatri(cod, 1);
    }

    // Cargar periodos de las asignaturas del cuatri 2
    for (const cod of cuatri2) {
        await cargarPeriodosEnCuatri(cod, 2);
    }

    // Mostrar el cuatrimestre 1 por defecto
    cuatriActivo = 1;
    renderHorario();
}

/* =========================================================
   CARGA LOS PERIODOS DE UNA ASIGNATURA EN UN CUATRI CONCRETO
========================================================= */
async function cargarPeriodosEnCuatri(codA, cuatri) {

    try {
        const res = await fetch(`ObtenerPeriodos?id=${codA}`);
        if (!res.ok) return;
        const periodos = await res.json();
        periodosPorCuatri[cuatri].push(...periodos);
    } catch (e) {
        console.warn(`Error cargando periodos de asignatura ${codA}:`, e);
    }
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
   CAMBIAR CUATRIMESTRE
========================================================= */
function cambiarCuatri(nuevo) {

    cuatriActivo = nuevo;

    document.getElementById("btn-cuatri-1").className =
        nuevo === 1 ? "activo" : "inactivo";
    document.getElementById("btn-cuatri-2").className =
        nuevo === 2 ? "activo" : "inactivo";

    renderHorario();
}

/* =========================================================
   MODO COLORES
========================================================= */
function toggleModoColores() {

    usarColoresAsignatura = !usarColoresAsignatura;
    actualizarBotonColores();
    renderHorario();
}

function actualizarBotonColores() {

    const btn = document.getElementById("btn-toggle-colores");
    if (!btn) return;

    if (usarColoresAsignatura) {
        btn.textContent = "Colores: ON";
        btn.classList.add("activo");
        btn.classList.remove("inactivo");
    } else {
        btn.textContent = "Colores: OFF";
        btn.classList.remove("activo");
        btn.classList.add("inactivo");
    }
}

function hashCode(str) {

    str = String(str);
    let hash = 0;

    for (let i = 0; i < str.length; i++) {
        hash = ((hash << 5) - hash) + str.charCodeAt(i);
        hash |= 0;
    }

    return Math.abs(hash);
}

function getColorAsignatura(codA, esLaboratorio = false) {

    const hash = hashCode(codA);
    const hue = (hash * 137.508) % 360;
    const hueOffset = (hash % 7) - 3;
    const finalHue = hue + hueOffset;

    const saturation = 90;
    const lightness = esLaboratorio ? 60 : 72;

    return `hsl(${finalHue}, ${saturation}%, ${lightness}%)`;
}

/* =========================================================
   CALCULAR LAYOUT SIN COLISIONES
========================================================= */
function calcularLayout(periodos) {

    const layout = {};

    const porDia = {};
    periodos.forEach(p => {
        if (!porDia[p.dia]) porDia[p.dia] = [];
        porDia[p.dia].push(p);
    });

    Object.values(porDia).forEach(listaDia => {

        // Grafo de conflictos
        const grafo = new Map();
        listaDia.forEach(p => grafo.set(p.idPeriodo, []));

        for (let i = 0; i < listaDia.length; i++) {
            for (let j = i + 1; j < listaDia.length; j++) {
                const a = listaDia[i];
                const b = listaDia[j];
                const solapan = a.horaIni < b.horaFin && b.horaIni < a.horaFin;
                if (solapan) {
                    grafo.get(a.idPeriodo).push(b);
                    grafo.get(b.idPeriodo).push(a);
                }
            }
        }

        // Componentes conexas
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

        // Layout por grupo
        grupos.forEach(grupo => {

            grupo.sort((a, b) => {
                if (a.horaIni !== b.horaIni) return a.horaIni - b.horaIni;
                return (b.horaFin - b.horaIni) - (a.horaFin - a.horaIni);
            });

            const columnas = [];
            let totalCols = 0;

            grupo.forEach(p => {
                let col = 0;
                while (true) {
                    const conflicto = columnas[col]?.some(otro =>
                        p.horaIni < otro.horaFin && otro.horaIni < p.horaFin
                    );
                    if (!conflicto) break;
                    col++;
                }
                if (!columnas[col]) columnas[col] = [];
                columnas[col].push(p);
                totalCols = Math.max(totalCols, col + 1);
                layout[p.idPeriodo] = { col };
            });

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

    const periodosSeleccionados = periodosPorCuatri[cuatriActivo];
    const layout = calcularLayout(periodosSeleccionados);
    const yaRenderizados = new Set();

    periodosSeleccionados.forEach(p => {

        if (yaRenderizados.has(p.idPeriodo)) return;
        yaRenderizados.add(p.idPeriodo);

        const div = document.createElement("div");
        const esTeoria = p.grupo.startsWith("T");
        const esLaboratorio = !esTeoria;

        div.classList.add("bloque", "ver-asignatura");
        if (esLaboratorio) div.classList.add("laboratorio");

        if (usarColoresAsignatura) {
            div.style.background = getColorAsignatura(p.codA, esLaboratorio);
        } else {
            div.style.background = esLaboratorio ? "#ff7a1a" : "#e9b86a";
        }

        div.dataset.id = p.codA;

        const rowStart = p.horaIni - 7;
        const rowSpan  = p.horaFin - p.horaIni;

        div.style.gridColumn = p.dia;
        div.style.gridRow    = `${rowStart} / span ${rowSpan}`;

        const info  = layout[p.idPeriodo];
        const width = 100 / info.totalCols;
        const left  = width * info.col;

        div.style.position = "relative";
        div.style.width    = `calc(${width}% - 4px)`;
        div.style.left     = `${left}%`;

        div.innerText = `${p.abreviatura} - ${p.grupo}`;

        grid.appendChild(div);
    });
}