document.addEventListener("DOMContentLoaded", function() {
    // Referencias a los elementos del DOM
    const popup = document.getElementById("popupRecomendacion");
    const btnCerrar = document.getElementById("btn-cerrar-recomendacion");
    const btnVerProgreso = document.getElementById("btn-ver-progreso");
    const btnVerHorario = document.getElementById("btn-ver-horario");

    // Evento para cerrar el popup
    if (btnCerrar) {
        btnCerrar.addEventListener("click", function() {
            popup.classList.remove("activo");
        });
    }

    // Eventos de redirección
    if (btnVerProgreso) {
        btnVerProgreso.addEventListener("click", function() {
            window.location.href = "MostrarProgreso";
        });
    }

    if (btnVerHorario) {
        btnVerHorario.addEventListener("click", function() {
            window.location.href = "horario.jsp";
        });
    }
});


