package modelo.entidades;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Almacena el resultado del análisis del formulario de afinidad:
 * - La mención recomendada
 * - Los códigos de las asignaturas recomendadas para cada cuatrimestre de 3º
 * - Las puntuaciones calculadas por asignatura (cod -> puntuación)
 *
 * Implementa Serializable para poder guardarse en sesión HTTP.
 */
public class ResultadoFormulario implements Serializable {

    private String mencionRecomendada;
    private List<Integer> codsCuatri1Tercero;  // códigos de asignaturas recomendadas 3º cuatri 1
    private List<Integer> codsCuatri2Tercero;  // códigos de asignaturas recomendadas 3º cuatri 2
    private Map<Integer, Double> puntuaciones; // cod -> puntuación

    public ResultadoFormulario() {}

    public ResultadoFormulario(String mencionRecomendada,
                                List<Integer> codsCuatri1Tercero,
                                List<Integer> codsCuatri2Tercero,
                                Map<Integer, Double> puntuaciones) {
        this.mencionRecomendada  = mencionRecomendada;
        this.codsCuatri1Tercero  = codsCuatri1Tercero;
        this.codsCuatri2Tercero  = codsCuatri2Tercero;
        this.puntuaciones        = puntuaciones;
    }

    /**
     * Devuelve un array con tres elementos:
     *   [0] String         → mención recomendada
     *   [1] List<Integer>  → códigos de asignaturas recomendadas cuatrimestre 1 de 3º
     *   [2] List<Integer>  → códigos de asignaturas recomendadas cuatrimestre 2 de 3º
     */
    public Object[] comoArray() {
        return new Object[]{
            mencionRecomendada,
            codsCuatri1Tercero,
            codsCuatri2Tercero
        };
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    public String getMencionRecomendada() { return mencionRecomendada; }
    public void setMencionRecomendada(String mencionRecomendada) {
        this.mencionRecomendada = mencionRecomendada;
    }

    public List<Integer> getCodsCuatri1Tercero() { return codsCuatri1Tercero; }
    public void setCodsCuatri1Tercero(List<Integer> codsCuatri1Tercero) {
        this.codsCuatri1Tercero = codsCuatri1Tercero;
    }

    public List<Integer> getCodsCuatri2Tercero() { return codsCuatri2Tercero; }
    public void setCodsCuatri2Tercero(List<Integer> codsCuatri2Tercero) {
        this.codsCuatri2Tercero = codsCuatri2Tercero;
    }

    public Map<Integer, Double> getPuntuaciones() { return puntuaciones; }
    public void setPuntuaciones(Map<Integer, Double> puntuaciones) {
        this.puntuaciones = puntuaciones;
    }
}