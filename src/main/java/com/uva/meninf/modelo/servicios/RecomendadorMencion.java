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

    /**
     * Estructura de optativas por mención/curso/cuatri.
     * Cada entrada es un hueco optativo: una lista de nombres candidatos
     * de los que se elegirá el de mayor puntuación.
     *
     * Formato: mencion -> curso -> cuatri -> lista de huecos
     * Cada hueco es una List<String> con los nombres candidatos.
     */
    private static final Map<String, Map<Integer, Map<Integer, List<List<String>>>>> OPTATIVAS;
 
    static {
        OPTATIVAS = new HashMap<>();

        // ── IS ────────────────────────────────────────────────────────────
        Map<Integer, Map<Integer, List<List<String>>>> is = new HashMap<>();

        Map<Integer, List<List<String>>> is3 = new HashMap<>();
        is3.put(1, List.of(
            List.of("SEGURIDAD DE REDES Y SISTEMAS",
                    "TECNOLOGIAS PARA EL DESARROLLO DE SOFTWARE")
        ));
        is3.put(2, List.of(
            List.of("SERVICIOS Y SISTEMAS WEB",
                    "TÉCNICAS DE APRENDIZAJE AUTOMATICO",
                    "COMPUTACIÓN PARALELA"),
            List.of("SERVICIOS Y SISTEMAS WEB",
                    "TÉCNICAS DE APRENDIZAJE AUTOMATICO",
                    "COMPUTACIÓN PARALELA")
        ));

        Map<Integer, List<List<String>>> is4 = new HashMap<>();
        is4.put(1, List.of(
            List.of("SISTEMAS MÓVILES"),
            List.of("ARQUITECTURA DE REDES Y SERVICIOS",
                    "INFORMÁTICA FORENSE")
        ));
        is4.put(2, List.of(
            List.of("SISTEMAS EMPOTRADOS",
                    "MODELOS DE INVESTIGACIÓN OPERATIVA")
        ));
        is.put(4, is4);
        OPTATIVAS.put("IS", is);

        // ── TI ────────────────────────────────────────────────────────────
        Map<Integer, Map<Integer, List<List<String>>>> ti = new HashMap<>();

        Map<Integer, List<List<String>>> ti3 = new HashMap<>();
        ti3.put(1, List.of(
            List.of("SISTEMAS MULTIMEDIA",
                    "ARQUITECTURA DE REDES Y SERVICIOS")
        ));
        ti3.put(2, List.of(
            List.of("SISTEMAS EMPOTRADOS"),
            List.of("COMPUTACIÓN PARALELA",
                    "ARQUITECTURAS DE COMPUTACIÓN AVANZADA")
        ));
        ti.put(3, ti3);

        Map<Integer, List<List<String>>> ti4 = new HashMap<>();
        ti4.put(1, List.of(
            List.of("INFORMÁTICA FORENSE"),
            List.of("SISTEMAS MÓVILES",
                    "SISTEMAS DE INFORMACIÓN Y DIRECCIÓN DE ORGANIZACIONES")
        ));
        ti4.put(2, List.of(
            List.of("RENDIMIENTO Y EVALUACIÓN DE COMPUTADORES",
                    "HARDWARE EMPOTRADO")
        ));
        ti.put(4, ti4);
        OPTATIVAS.put("TI", ti);

        // ── CO ────────────────────────────────────────────────────────────
        Map<Integer, Map<Integer, List<List<String>>>> co = new HashMap<>();

        Map<Integer, List<List<String>>> co3 = new HashMap<>();
        co3.put(1, List.of(
            List.of("CODIGOS Y CRIPTOGRAFIA",
                    "PROGRAMACIÓN DE APLICACIONES GRAFICAS")
        ));
        co3.put(2, List.of(
            List.of("COMPUTACIÓN PARALELA",
                    "SISTEMAS AVANZADOS DE INTEGRACION DE LA INFORMACION"),
            List.of("SERVICIOS Y SISTEMAS WEB",
                    "INFERENCIA ESTADISTICA I")
        ));
        co.put(3, co3);

        Map<Integer, List<List<String>>> co4 = new HashMap<>();
        co4.put(1, List.of(
            List.of("SISTEMAS DE INFORMACIÓN Y DIRECCIÓN DE ORGANIZACIONES",
                    "REGRESIÓN Y ANOVA",
                    "GARANTIA Y SEGURIDAD DE LA INFORMACION",
                    "ESTADÍSTICA DESCRIPTIVA"),
            List.of("REGRESIÓN Y ANOVA",
                    "GARANTIA Y SEGURIDAD DE LA INFORMACION",
                    "ESTADÍSTICA DESCRIPTIVA")
        ));
        co4.put(2, List.of(
            List.of("MODELOS PARA LA TOMA DE DECISISIONES",
                    "EVALUACIÓN Y RENDIMIENTO DE SISTEMAS SOFTWARE")
        ));
        co.put(4, co4);
        OPTATIVAS.put("CO", co);
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

        // Índice nombre -> Asignatura para búsqueda rápida
        Map<String, Asignatura> porNombre = new HashMap<>();
        for (Asignatura a : asignaturas) porNombre.put(a.getNombre(), a);
 
        // ── CALCULAR PUNTUACIÓN POR ASIGNATURA ────────────────────────────
        Map<Integer, Double> puntuacionPorCod = calcularPuntuaciones(
            asignaturas, setFavoritas, setOdiadas,
            setSuperadas, setSuspensas, notasLenguajes
        );
 
        // ── SELECCIÓN Y PUNTUACIÓN POR MENCIÓN ───────────────────────────
        String[] menciones    = {"IS", "CO", "TI"};
 
        Map<String, Double>        puntuacionMencion    = new HashMap<>();
        Map<String, List<Integer>> seleccionCuatri1Terc = new HashMap<>();
        Map<String, List<Integer>> seleccionCuatri2Terc = new HashMap<>();
 
        for (String mencion : menciones) {
            double totalMencion = 0.0;
            List<Integer> codsCuatri1 = new ArrayList<>();
            List<Integer> codsCuatri2 = new ArrayList<>();
 
            // Cursos 3 y 4, cuatris 1 y 2
            for (int curso = 3; curso <= 4; curso++) {
                for (int cuatri = 1; cuatri <= 2; cuatri++) {

 
                    // Obligatorias de esta mención/curso/cuatri
                    List<Asignatura> obligatorias = getObligatorias(
                        asignaturas, mencion, curso, cuatri
                    );
 
                    // Optativas elegidas para este hueco
                    List<Asignatura> optativas = elegirOptativas(
                        mencion, curso, cuatri, porNombre, puntuacionPorCod
                    );
 
                    // Combinar
                    List<Asignatura> seleccion = new ArrayList<>(obligatorias);
                    seleccion.addAll(optativas);
 
                    // Acumular puntuación de la mención
                    for (Asignatura a : seleccion)
                        totalMencion += puntuacionPorCod.getOrDefault(a.getCod(), 0.0);
 
                    // Guardar códigos de 3º
                    if (curso == 3) {
                        List<Integer> cods = new ArrayList<>();
                        for (Asignatura a : seleccion) cods.add(a.getCod());
                        if (cuatri == 1) codsCuatri1 = cods;
                        else             codsCuatri2 = cods;
                    }
                }
            }
 
            puntuacionMencion.put(mencion, totalMencion);
            seleccionCuatri1Terc.put(mencion, codsCuatri1);
            seleccionCuatri2Terc.put(mencion, codsCuatri2);
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
    
    /**
     * Devuelve las asignaturas obligatorias de una mención/curso/cuatri.
     */
    private List<Asignatura> getObligatorias(List<Asignatura> asignaturas,
                                              String mencion, int curso, int cuatri) {
        List<Asignatura> resultado = new ArrayList<>();
        String men;
        for (Asignatura asig : asignaturas) {
            for (AsignaturaMencion am : asig.getMenciones()) {
                men=am.getMencion().getTipo().name();
                if ((men.equals(mencion) || men.equals("PreMencion"))
                        && am.getCursoAcademico() == curso
                        && asig.getCuatri() == cuatri
                        && am.isObligatoriedad()) {
                    resultado.add(asig);
                    break;
                }
            }
        }
        
        
            
        return resultado;
    }

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

   /**
     * Para cada hueco optativo de una mención/curso/cuatri,
     * elige la asignatura candidata con mayor puntuación.
     */
    private List<Asignatura> elegirOptativas(String mencion, int curso, int cuatri,
                                          Map<String, Asignatura> porNombre,
                                          Map<Integer, Double> puntuacionPorCod) {

        List<Asignatura> elegidas = new ArrayList<>();
        Set<Integer> yaElegidosCods = new HashSet<>(); // evita repetir

        List<List<String>> huecos = OPTATIVAS
            .getOrDefault(mencion, new HashMap<>())
            .getOrDefault(curso,   new HashMap<>())
            .getOrDefault(cuatri,  new ArrayList<>());

        for (List<String> candidatas : huecos) {
            candidatas.stream()
                .map(porNombre::get)
                .filter(Objects::nonNull)
                .filter(a -> !yaElegidosCods.contains(a.getCod())) // excluir ya elegidas
                .max(Comparator.comparingDouble(
                    a -> puntuacionPorCod.getOrDefault(a.getCod(), 0.0)))
                .ifPresent(mejor -> {
                    elegidas.add(mejor);
                    yaElegidosCods.add(mejor.getCod());
                });
        }

        return elegidas;
    }

    private Set<String> toSet(String[] array) {
        Set<String> set = new HashSet<>();
        if (array != null)
            for (String s : array) set.add(s);
        return set;
    }
}