package com.uva.meninf.modelo.servicios;

import com.uva.meninf.modelo.entidades.Asignatura;
import com.uva.meninf.modelo.entidades.ResultadoFormulario;
import com.uva.meninf.modelo.relaciones.AsignaturaMencion;

import java.util.*;

/**
 * Servicio de lógica de negocio para el cálculo de la mención recomendada
 * y la selección de asignaturas para cada cuatrimestre de 3º.
 */
public class RecomendadorMencion {

    // ── SLOTS DE OPTATIVAS (según el plan de estudios del PDF) ────────────
    // Estructura: mencion -> curso -> cuatri -> lista de slots
    // Cada slot es una lista de nombres de asignaturas que compiten entre sí
    private static final Map<String, Map<Integer, Map<Integer, List<List<String>>>>> SLOTS_OPTATIVAS;

    static {
        SLOTS_OPTATIVAS = new HashMap<>();

        // ── IS ────────────────────────────────────────────────────────────
        Map<Integer, Map<Integer, List<List<String>>>> is = new HashMap<>();

        Map<Integer, List<List<String>>> is3 = new HashMap<>();
        is3.put(1, List.of(
            List.of("Seguridad de Redes y Sistemas",
                    "Tecnologías para el Desarrollo del Software")
        ));
        is3.put(2, List.of(
            List.of("Servicios y Sistemas Web",
                    "Técnicas de Aprendizaje Automático",
                    "Computación Paralela")
        ));
        is.put(3, is3);

        Map<Integer, List<List<String>>> is4 = new HashMap<>();
        is4.put(1, List.of(
            List.of("Sistemas Móviles"),
            List.of("Arquitectura de Redes y Servicios", "Informática Forense")
        ));
        is4.put(2, List.of(
            List.of("Sistemas Empotrados", "Principios de Análisis Financiero")
        ));
        is.put(4, is4);
        SLOTS_OPTATIVAS.put("IS", is);

        // ── TI ────────────────────────────────────────────────────────────
        Map<Integer, Map<Integer, List<List<String>>>> ti = new HashMap<>();

        Map<Integer, List<List<String>>> ti3 = new HashMap<>();
        ti3.put(1, List.of(
            List.of("Sistemas Multimedia", "Arquitecturas de Redes y Servicios")
        ));
        ti3.put(2, List.of(
            List.of("Sistemas Empotrados"),
            List.of("Computación Paralela", "Arquitecturas de Computadores Avanzadas")
        ));
        ti.put(3, ti3);

        Map<Integer, List<List<String>>> ti4 = new HashMap<>();
        ti4.put(1, List.of(
            List.of("Informática Forense"),
            List.of("Sistemas Móviles",
                    "Sistemas de Información y Dirección de Organizaciones")
        ));
        ti4.put(2, List.of(
            List.of("Diseño de Hardware Específico",
                    "Rendimiento y Evaluación de Computadores",
                    "Hardware Empotrado")
        ));
        ti.put(4, ti4);
        SLOTS_OPTATIVAS.put("TI", ti);

        // ── CO ────────────────────────────────────────────────────────────
        Map<Integer, Map<Integer, List<List<String>>>> co = new HashMap<>();

        Map<Integer, List<List<String>>> co3 = new HashMap<>();
        co3.put(1, List.of(
            List.of("Códigos y Criptografía", "Programación de Aplicaciones Gráficas")
        ));
        co3.put(2, List.of(
            List.of("Computación Paralela",
                    "Sistemas Avanzados de Integración de la Información"),
            List.of("Servicios y Sistemas Web", "Inferencia Estadística I")
        ));
        co.put(3, co3);

        Map<Integer, List<List<String>>> co4 = new HashMap<>();
        co4.put(1, List.of(
            List.of("Sistemas de Información y Dirección de Organizaciones",
                    "Regresión y Anova", "Garantía y Seguridad de la Información",
                    "Señales y Sistemas", "Estadística Descriptiva"),
            List.of("Regresión y Anova", "Garantía y Seguridad de la Información",
                    "Señales y Sistemas", "Estadística Descriptiva")
        ));
        co4.put(2, List.of(
            List.of("Modelos de IO", "Evaluación y Rendimiento de Sistemas Software")
        ));
        co.put(4, co4);
        SLOTS_OPTATIVAS.put("CO", co);
    }

    // ══════════════════════════════════════════════════════════════════════
    // MÉTODO PÚBLICO
    // ══════════════════════════════════════════════════════════════════════

    public ResultadoFormulario calcularResultado(
            List<Asignatura> asignaturas,
            String[] favoritas, String[] odiadas,
            String[] superadas, String[] suspensas,
            String python, String mips,
            String nivelJava, String c) {

        // Notas de lenguajes (nombre -> nota 1-5)
        Map<String, Integer> notasLenguajes = new HashMap<>();
        if (python    != null) notasLenguajes.put("Python",           Integer.parseInt(python));
        if (mips      != null) notasLenguajes.put("Ensamblador MIPS", Integer.parseInt(mips));
        if (nivelJava != null) notasLenguajes.put("Java",             Integer.parseInt(nivelJava));
        if (c         != null) notasLenguajes.put("C",                Integer.parseInt(c));

        Set<String> setFavoritas = toSet(favoritas);
        Set<String> setOdiadas   = toSet(odiadas);
        Set<String> setSuperadas = toSet(superadas);
        Set<String> setSuspensas = toSet(suspensas);

        // ── CALCULAR PUNTUACIÓN POR ASIGNATURA (cod -> puntuación) ────────
        Map<Integer, Double> puntuacionPorCod = calcularPuntuaciones(
            asignaturas, setFavoritas, setOdiadas,
            setSuperadas, setSuspensas, notasLenguajes
        );

        // ── SELECCIÓN POR MENCIÓN Y CUATRIMESTRE ──────────────────────────
        String[] menciones      = {"IS", "CO", "TI"};
        int[][] cursosCuatris   = {{3, 1}, {3, 2}, {4, 1}, {4, 2}};

        Map<String, Double>        puntuacionMencion    = new HashMap<>();
        Map<String, List<Integer>> seleccionCuatri1Terc = new HashMap<>();
        Map<String, List<Integer>> seleccionCuatri2Terc = new HashMap<>();

        for (String mencion : menciones) {
            double totalMencion = 0.0;

            for (int[] cc : cursosCuatris) {
                int curso  = cc[0];
                int cuatri = cc[1];
                int limite = (curso == 4 && cuatri == 2) ? 1 : 5;

                List<Asignatura> seleccion = seleccionarAsignaturas(
                    asignaturas, mencion, curso, cuatri, limite, puntuacionPorCod
                );

                for (Asignatura asig : seleccion)
                    totalMencion += puntuacionPorCod.getOrDefault(asig.getCod(), 0.0);

                if (curso == 3) {
                    List<Integer> cods = new ArrayList<>();
                    for (Asignatura asig : seleccion) cods.add(asig.getCod());
                    if (cuatri == 1) seleccionCuatri1Terc.put(mencion, cods);
                    else             seleccionCuatri2Terc.put(mencion, cods);
                }
            }

            puntuacionMencion.put(mencion, totalMencion);
        }

        // ── MENCIÓN RECOMENDADA ───────────────────────────────────────────
        String mencionRecomendada = puntuacionMencion.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("IS");

        return new ResultadoFormulario(
            mencionRecomendada,
            seleccionCuatri1Terc.getOrDefault(mencionRecomendada, new ArrayList<>()),
            seleccionCuatri2Terc.getOrDefault(mencionRecomendada, new ArrayList<>()),
            puntuacionPorCod
        );
    }

    // ══════════════════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS
    // ══════════════════════════════════════════════════════════════════════

    private Map<Integer, Double> calcularPuntuaciones(
            List<Asignatura> asignaturas,
            Set<String> setFavoritas, Set<String> setOdiadas,
            Set<String> setSuperadas, Set<String> setSuspensas,
            Map<String, Integer> notasLenguajes) {

        Map<Integer, Double> puntuacionPorCod = new HashMap<>();

        for (Asignatura asig : asignaturas) {
            double puntuacion = 0.0;

            for (Asignatura req : asig.getRequisitos()) {
                String reqCod = String.valueOf(req.getCod());
                if (setFavoritas.contains(reqCod)) puntuacion += 3.0;
                if (setOdiadas.contains(reqCod))   puntuacion -= 3.0;
                if (setSuperadas.contains(reqCod)) puntuacion += 1.0;
                if (setSuspensas.contains(reqCod)) puntuacion -= 1.0;
            }

            for (var lenguaje : asig.getLenguajes()) {
                int nota = notasLenguajes.getOrDefault(lenguaje.getNombre(), 3);
                puntuacion += (nota - 3);
            }

            puntuacionPorCod.put(asig.getCod(), puntuacion);
        }

        return puntuacionPorCod;
    }

    private List<Asignatura> seleccionarAsignaturas(
            List<Asignatura> todasAsignaturas,
            String mencion, int curso, int cuatri,
            int limite,
            Map<Integer, Double> puntuacionPorCod) {

        List<Asignatura> obligatorias = new ArrayList<>();
        Map<String, Asignatura> optativasPorNombre = new HashMap<>();

        for (Asignatura asig : todasAsignaturas) {
            for (AsignaturaMencion am : asig.getMenciones()) {
                if (am.getMencion().getTipo().name().equals(mencion)
                        && am.getCursoAcademico() == curso
                        && asig.getCuatri() == cuatri) {
                    if (am.isObligatoriedad()) obligatorias.add(asig);
                    else                       optativasPorNombre.put(asig.getNombre(), asig);
                    break;
                }
            }
        }

        List<Asignatura> seleccion = new ArrayList<>(obligatorias);

        List<List<String>> slots = SLOTS_OPTATIVAS
            .getOrDefault(mencion, new HashMap<>())
            .getOrDefault(curso,   new HashMap<>())
            .getOrDefault(cuatri,  new ArrayList<>());

        for (List<String> slot : slots) {
            if (seleccion.size() >= limite) break;
            slot.stream()
                .map(optativasPorNombre::get)
                .filter(a -> a != null)
                .max(Comparator.comparingDouble(
                    a -> puntuacionPorCod.getOrDefault(a.getCod(), 0.0)))
                .ifPresent(mejor -> {
                    if (seleccion.size() < limite) seleccion.add(mejor);
                });
        }

        return seleccion;
    }

    private Set<String> toSet(String[] array) {
        Set<String> set = new HashSet<>();
        if (array != null)
            for (String s : array) set.add(s);
        return set;
    }
}