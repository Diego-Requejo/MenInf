let periodosPorCuatri = {
    1: [],
    2: []
};


function initHorario() {
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
   Para cada periodo, encuentra el slot (columna lógica) que
   esté libre en TODAS las franjas horarias que ocupa.
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

/* =========================================================
   RENDER HORARIO
========================================================= */
function renderHorario() {

    const grid = document.getElementById("grid-horario");
    if (!grid) return;

    grid.innerHTML = "";

    const periodosSeleccionados = periodosPorCuatri[cuatriActivo];

    const solapamientos = getSolapamientos();

    const renderData = [];

    /* =====================================================
       1ª PASADA: cálculo inicial
    ===================================================== */
    periodosSeleccionados.forEach(p => {

        const esTeoria = p.grupo.startsWith("T");

        let maxSolape = 1;
        let posicion = 0;

        for (let h = p.horaIni; h < p.horaFin; h++) {

            const key = `${p.dia}-${h}`;

            if (solapamientos[key]) {

                maxSolape = Math.max(maxSolape, solapamientos[key].length);

                posicion = Math.max(
                    posicion,
                    getPosicionEnSolape(p, h, solapamientos)
                );
            }
        }

        renderData.push({
            p,
            esTeoria,
            maxSolape,
            posicion
        });
    });

    /* =====================================================
        2ª PASADA: ocupación real por celda
     ===================================================== */

     const ocupacionCelda = {};

     // 1. contar cuántos periodos ocupan cada celda (día-hora)
     periodosSeleccionados.forEach(p => {

         for (let h = p.horaIni; h < p.horaFin; h++) {

             const key = `${p.dia}-${h}`;

             if (!ocupacionCelda[key]) {
                 ocupacionCelda[key] = 0;
             }

             ocupacionCelda[key]++;
         }
     });

     // 2. calcular el máximo solape que afecta a cada periodo
     const ajusteFinal = {};

     periodosSeleccionados.forEach(p => {

         let maxSolape = 1;

         for (let h = p.horaIni; h < p.horaFin; h++) {

             const key = `${p.dia}-${h}`;

             maxSolape = Math.max(maxSolape, ocupacionCelda[key] || 1);
         }

         ajusteFinal[p.idPeriodo] = maxSolape;
     });

    /* =====================================================
       RENDER FINAL
    ===================================================== */
    const yaRenderizados = new Set();

    renderData.forEach(r => {

        const p = r.p;

        if (yaRenderizados.has(p.idPeriodo)) return;
        yaRenderizados.add(p.idPeriodo);

        const div = document.createElement("div");

        const esTeoria = r.esTeoria;

        div.classList.add("bloque", "ver-asignatura");

        if (!esTeoria) div.classList.add("laboratorio");

        div.dataset.id = p.codA;

        const col = p.dia;
        const rowStart = p.horaIni - 8;
        const rowSpan = p.horaFin - p.horaIni;

        div.style.gridColumn = col;
        div.style.gridRow = `${rowStart} / span ${rowSpan}`;

        const max = ajusteFinal[p.idPeriodo];
        const ancho = 100 / max;
        const margen = ancho * r.posicion + 0.01;

        div.style.width = `${ancho - 1}%`;
        div.style.left = `${margen}%`;
        div.style.position = "relative";

        div.innerText = `${p.abreviatura} - ${p.grupo}`;

        grid.appendChild(div);
    });
}