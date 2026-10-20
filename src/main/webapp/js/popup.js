document.addEventListener("DOMContentLoaded", () => {

    const usuario = document.getElementById("usuario");

    if (usuario) {
        usuario.addEventListener("click", abrirPopup);
    }

});

const popup = document.getElementById("popup");
const contenido = document.getElementById("contenido-popup");

if (!popup || !contenido) {
    console.warn("Popup no encontrado en DOM");
}

// ---------- CONTROL DE SESION --------------
document.addEventListener("click", (e) => {

    const el = e.target.closest(".requiere-sesion");

    if (!el) return;

    e.preventDefault();

    fetch("ComprobarSesion")
        .then(res => res.text())
        .then(user => {

            if (user === "") {
                abrirPopup();
                return;
            }

            if (el.tagName === "A") {
                window.location.href = el.href;
            }

            if (el.tagName === "BUTTON") {
                window.location.href = el.dataset.href;
            }

        });

});

// --------- VISTAS ---------
function vistaOpciones() {
    contenido.innerHTML = `
        <div class="popup-titulo">
            <span>Elige una opción:</span>
            <div class="cerrar" onclick="cerrarPopup()">X</div>
        </div>

        <div class="boton-opciones">
            <button onclick="vistaLogin()" class="boton-inicio-sesion">Iniciar sesión</button>
            <button onclick="vistaRegistro()" class="boton-inicio-sesion">Registrarse</button>
        </div>
    `;
}

function vistaLogin() {
    contenido.innerHTML = `
        <div class="popup-titulo">
            <span>Iniciar sesión</span>
            <div class="cerrar" onclick="cerrarPopup()">X</div>
        </div>

        <label>Nombre de usuario:</label>
        <input id="loginUser" type="text" placeholder="Usuario">

        <label>Contraseña:</label>
        <input id="loginPass" type="password" placeholder="Contraseña">
    
        <div id="mensaje-error" class="mensaje-error"></div>

        <button onclick="enviarLogin()" class="boton-enviar">Enviar</button>
    `;

    // Detectar Enter
    document.getElementById("loginPass").addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            enviarLogin();
        }
    });

    document.getElementById("loginUser").addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            enviarLogin();
        }
    });
}

function vistaRegistro() {
    contenido.innerHTML = `
        <div class="popup-titulo">
            <span>Registrar usuario</span>
            <div class="cerrar" onclick="cerrarPopup()">X</div>
        </div>

        <label>Nombre de usuario:</label>
        <input id="regUser" type="text" placeholder="Usuario">

        <label>Contraseña:</label>
        <input id="regPass" type="password" placeholder="Contraseña">
    
        <div id="mensaje-error" class="mensaje-error"></div>

        <button onclick="enviarRegistro()" class="boton-enviar">Enviar</button>
    `;

    // Detectar Enter
    document.getElementById("regPass").addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            enviarRegistro();
        }
    });

    document.getElementById("regUser").addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            enviarRegistro();
        }
    });
}

function vistaSesion(nombre) {
    contenido.innerHTML = `
        <div class="popup-titulo">
            <span>${nombre}</span>
            <div class="cerrar" onclick="cerrarPopup()">X</div>
        </div>

        <button onclick="logout()" class="boton-cerrar-sesion">
            Cerrar sesión
        </button>
    `;
}

// --------- CONTROL ---------

function logout() {
    fetch("CerrarSesion")
    .then(() => {
        window.location.href = "index.html";
    });
}

function abrirPopup() {
    popup.classList.add("activo");

    fetch("ComprobarSesion")
        .then(res => res.text())
        .then(user => {

            if (user !== "") {
                vistaSesion(user);
            } else {
                vistaOpciones();
            }

        });
}
function cerrarPopup() {
    popup.classList.remove("activo");
}

function enviarLogin() {
    const user = document.getElementById("loginUser").value;
    const pass = document.getElementById("loginPass").value;
    
    if(user==="" || pass===""){
        mostrarError("Rellena ambos campos");
        return;
    }

    fetch("InicioSesion", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `username=${user}&password=${pass}`
    })
    .then(res => res.text())
    .then(data => {

        if (data === "OK") {
            vistaSesion(user);
        } else {
            mostrarError("El usuario y/o contraseña son incorrectos.");
        }

    });
}

function enviarRegistro() {

    const user = document.getElementById("regUser").value;
    const pass = document.getElementById("regPass").value;
    
    if(user==="" || pass===""){
        mostrarError("Rellena ambos campos");
        return;
    }

    fetch("RegistrarUsuario", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `username=${user}&password=${pass}`
    })
    .then(res => res.text())
    .then(data => {

        if (data === "OK") {
            vistaSesion(user);
        }

        else if (data === "EXISTE") {
            mostrarError("El usuario ya existe, inicia sesión");
        }

    });

}

function mostrarError(msg) {
    const errorBox = document.getElementById("mensaje-error");
    if (errorBox) {
        errorBox.innerText = msg;
    }
}

window.addEventListener("load", () => {
fetch("ComprobarSesion")
    .then(res => res.text())
    .then(user => {
        if (user !== "") {
            vistaSesion(user);
        }
    });
});