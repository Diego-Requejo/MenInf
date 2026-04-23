document.addEventListener("DOMContentLoaded", () => {

    const path = window.location.pathname;

    // 1. Si estamos en la página de asignatura.html
    if (path.includes("asignatura.html")) {
        const params = new URLSearchParams(window.location.search);
        const id = params.get("id");
        if (id) { cargarDatosEnPagina(id); }
    }

    // 2. Si estamos en horario.html
    if (path.includes("horario.html")) {
        configurarBotonesHorario();
        cargarListaAsignaturas();
    }

    // 3. Si estamos en progreso.jsp
    if (path.includes("progreso.jsp")) {
        cargarProgresoUsuario();
    }

    // Event listener global para clics en elementos con clase ver-asignatura (Horario)
    document.addEventListener("click", (e) => {
        const el = e.target.closest(".ver-asignatura");
        if (el) {
            const id = el.dataset.id;
            abrirPopupAsignatura(id);
        }
    });

});

let cuatriActivo = 1; 
let todasAsignaturas = [];
let textoBusqueda = "";
/**
 * Carga el progreso del usuario y rellena los SELECTS originales de progreso.jsp
 */
function cargarProgresoUsuario() {
    const listSuperadas = document.getElementById("lista-superadas");
    const listSuspensas = document.getElementById("lista-suspensas");
    const listCursando = document.getElementById("lista-cursando");
    const listFavoritas = document.getElementById("lista-favoritas");

    if (!listSuperadas) return;

    fetch("ObtenerProgreso")
        .then(res => res.json())
        .then(data => {
            // Limpiar listas y añadir evento de cambio para abrir popup
            [listSuperadas, listSuspensas, listCursando, listFavoritas].forEach(select => {
                select.innerHTML = "";
                // Al cambiar la selección en el select, abrir el popup
                select.onchange = (e) => {
                    if (e.target.value) abrirPopupAsignatura(e.target.value);
                };
            });

            let superadasCount = 0;
            const totalAsig = 24;

            data.forEach(asig => {
                // Crear una opción para el select
                const opt = document.createElement("option");
                opt.value = asig.cod;
                opt.innerText = `${asig.nombre} (${asig.abreviatura})`;

                if (asig.estado === "superada") {
                    listSuperadas.appendChild(opt);
                    superadasCount++;
                } else if (asig.estado === "suspensa") {
                    listSuspensas.appendChild(opt.cloneNode(true));
                } else if (asig.estado === "cursando") {
                    listCursando.appendChild(opt.cloneNode(true));
                }

                if (asig.favorita) {
                    listFavoritas.appendChild(opt.cloneNode(true));
                }
            });

            // Actualizar círculo de progreso
            const porcentaje = Math.round((superadasCount / totalAsig) * 100);
            const texto = document.getElementById("porcentaje-texto");
            if (texto) texto.innerText = `${porcentaje} %`;
            
            const circulo = document.querySelector(".circulo-progreso");
            if (circulo) {
                const circunferencia = 339.3;
                const offset = circunferencia * (1 - porcentaje / 100);
                circulo.style.strokeDashoffset = offset;
            }
        })
        .catch(err => console.error("Error al cargar progreso:", err));
}

/**
 * Carga todas las asignaturas desde la BD y las pone en el menú lateral (Horario)
 */
function cargarListaAsignaturas() {
    const contenedor = document.getElementById("contenedor-lista-asignaturas");
    if (!contenedor) return;

    fetch("ListarAsignaturas")
        .then(res => res.json())
        .then(lista => {
            todasAsignaturas = lista; 
            actualizarListaAsignaturas(); 
    })
    .catch(err => console.error("Error al cargar:", err));
}

function configurarBotonesHorario() {
    const btnC1 = document.getElementById("btn-cuatri-1");
    const btnC2 = document.getElementById("btn-cuatri-2");

    if (!btnC1 || !btnC2) return;

   if (btnC1 && btnC2) {
        btnC1.addEventListener("click", () => {
            cuatriActivo = 1;
            btnC1.className = "activo"; 
            btnC2.className = "inactivo";
            actualizarListaAsignaturas(); 
        });
        btnC2.addEventListener("click", () => {
            cuatriActivo = 2;
            btnC2.className = "activo"; 
            btnC1.className = "inactivo";
            actualizarListaAsignaturas();
        });
    }

    const buscador = document.getElementById("buscardor-asignatura");
    if (buscador) {
        buscador.addEventListener("input", (e) => {
            textoBusqueda = e.target.value.toLowerCase();
            actualizarListaAsignaturas();
        });
    }
}

function abrirPopupAsignatura(id) {
    if (typeof abrirPopup === "function") abrirPopup(false); // Abrir sin cargar sesión

    const contenido = document.getElementById("contenido-popup");
    contenido.innerHTML = "<p style='text-align:center; padding:20px;'>Cargando información...</p>";

    fetch(`ObtenerAsignatura?id=${id}`)
        .then(res => res.json())
        .then(asig => {
            if (asig.error) {
                contenido.innerHTML = `<p class="mensaje-error">${asig.error}</p>`;
            } else {
                contenido.innerHTML = generarHTMLAsignatura(asig, true);
            }
        });
}

function actualizarListaAsignaturas() {
    const contenedor = document.getElementById("contenedor-lista-asignaturas");
    if (!contenedor) return;

    contenedor.innerHTML = "";

    const filtradas  = todasAsignaturas.filter(a => {
        const coindiceCuatri = a.cuatri === cuatriActivo;
        const coincideBusqueda = a.nombre.toLowerCase().includes(textoBusqueda) || 
                                a.abreviatura.toLowerCase().includes(textoBusqueda);
        return coindiceCuatri && coincideBusqueda;      
    });

    if(filtradas.length === 0) {
        contenedor.innerHTML = "<div class='item'>No hay asignaturas para este cuatrimestre.</div>";
        return;
    }

    filtradas.forEach(asig => {
        const item = document.createElement("div");
        item.className = "item";
        item.dataset.id = asig.cod;
        item.innerText = `${asig.nombre} (${asig.abreviatura})`;
        contenedor.appendChild(item);
    });
}

function cargarDatosEnPagina(id) {
    const main = document.getElementById("detalle-asignatura");
    if (!main) return;

    fetch(`ObtenerAsignatura?id=${id}`)
        .then(res => res.json())
        .then(asig => {
            if (!asig.error) {
                main.innerHTML = generarHTMLAsignatura(asig, false);
            }
        });
}

function generarHTMLAsignatura(asig, esPopup) {
    const listaProfes = asig.listaProfesores || [];
    const listaRequisitos = asig.listaRequisitos || [];
    const listaLenguajes = asig.listaLenguajes || [];
    const listaMenciones = asig.listaMenciones || [];

    const cuatriTexto = asig.cuatri ? `${asig.cuatri}º cuatrimestre` : "";
    let mencionesHTML = "";

    if (listaMenciones.length > 0) {
        mencionesHTML = listaMenciones.map(m => 
            `<div class="comun1">${m.mencion} - ${m.curso}º curso</div>`
        ).join('');
    } else {
        mencionesHTML = "<div class='comun1'>Formación básica/obligatoria</div>";
    }

    return `
        <div class="contenedor-detalle-asignatura">
            <div class="header-asignatura">
                <h1 class="titulo0">${asig.nombre.toUpperCase()} (${asig.abreviatura})</h1>
                ${esPopup ? '<div class="cerrar-asignatura" onclick="cerrarPopup()">X</div>' : ''}
            </div>

            <div class="especificaciones">
                <div class="caja-izquierda-asignaturas">
                    <div>
                        <div class="titulo2">Mención / Rama</div>
                        ${mencionesHTML}
                        <div class="comun1">${cuatriTexto}</div>
                    </div>
                    
                    <div>
                        <div class="titulo2">Profesores</div>
                        <div class="lista-profesores">
                            ${listaProfes.length > 0
                                ? listaProfes.map(p => `<div class="elemento-lista">
                                    ${p.nombre} (${p.correo})</div>`).join('')
                                : '<div class="elemento-lista">No asignados</div>'}
                        </div>
                    </div>
                </div>

                <div class="divisor"></div>

                <div class="caja-derecha-asignaturas">
                    <div>
                        <div class="titulo2">Prerrequisitos</div>
                        <div class="lista-asignaturas">
                            ${listaRequisitos.length > 0
                                ? listaRequisitos.map(r => `<div class="elemento-lista ver-asignatura" data-id="${r.cod}" style="cursor:pointer;">
                                    ${r.nombre} (${r.abreviatura})</div>`).join('')
                                : '<div class="elemento-lista">Ninguno</div>'}
                        </div>
                    </div>
                    
                    <div>
                        <div class="titulo2">Lenguajes o aplicaciones</div>
                        <div class="lista-asignaturas">
                            ${listaLenguajes.length > 0
                                ? listaLenguajes.map(l => `<div class="elemento-lista">${l.nombre}</div>`).join('')
                                : '<div class="elemento-lista">No especificados</div>'}
                        </div>
                    </div>
                </div>
            </div>
            
            ${!esPopup ? '<img class="flecha-regreso" src="./img/Flecha_regreso.png" alt="Regresar" onclick="window.history.back()">' : ''}
        </div>
    `;
}