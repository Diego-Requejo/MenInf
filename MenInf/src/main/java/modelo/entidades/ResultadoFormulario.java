package modelo.entidades;

import java.util.List;

/**
 * Almacena el resultado del análisis del formulario de afinidad:
 * - La mención recomendada
 * - Las puntuaciones por asignatura de 3º y 4º
 * - Las asignaturas recomendadas para cada cuatrimestre de 3º
 */
public class ResultadoFormulario {

    private String mencionRecomendada;
    private List<String> asignaturasCuatri1Tercero;  // asignaturas recomendadas 3º cuatri 1
    private List<String> asignaturasCuatri2Tercero;  // asignaturas recomendadas 3º cuatri 2

    // Puntuaciones calculadas por asignatura (nombre -> puntuación)
    private java.util.Map<String, Double> puntuaciones;

    public ResultadoFormulario(String mencionRecomendada,
                                List<String> asignaturasCuatri1Tercero,
                                List<String> asignaturasCuatri2Tercero,
                                java.util.Map<String, Double> puntuaciones) {
        this.mencionRecomendada       = mencionRecomendada;
        this.asignaturasCuatri1Tercero = asignaturasCuatri1Tercero;
        this.asignaturasCuatri2Tercero = asignaturasCuatri2Tercero;
        this.puntuaciones              = puntuaciones;
    }

    /**
     * Devuelve un array con tres elementos:
     *   [0] String        → mención recomendada
     *   [1] List<String>  → asignaturas recomendadas cuatrimestre 1 de 3º
     *   [2] List<String>  → asignaturas recomendadas cuatrimestre 2 de 3º
     */
    public Object[] comoArray() {
        return new Object[]{
            mencionRecomendada,
            asignaturasCuatri1Tercero,
            asignaturasCuatri2Tercero
        };
    }

    // ── Getters y Setters ──────────────────────────────────────────

    public String getMencionRecomendada() { return mencionRecomendada; }
    public void setMencionRecomendada(String mencionRecomendada) {
        this.mencionRecomendada = mencionRecomendada;
    }

    public List<String> getAsignaturasCuatri1Tercero() { return asignaturasCuatri1Tercero; }
    public void setAsignaturasCuatri1Tercero(List<String> asignaturasCuatri1Tercero) {
        this.asignaturasCuatri1Tercero = asignaturasCuatri1Tercero;
    }

    public List<String> getAsignaturasCuatri2Tercero() { return asignaturasCuatri2Tercero; }
    public void setAsignaturasCuatri2Tercero(List<String> asignaturasCuatri2Tercero) {
        this.asignaturasCuatri2Tercero = asignaturasCuatri2Tercero;
    }

    public java.util.Map<String, Double> getPuntuaciones() { return puntuaciones; }
    public void setPuntuaciones(java.util.Map<String, Double> puntuaciones) {
        this.puntuaciones = puntuaciones;
    }
}
