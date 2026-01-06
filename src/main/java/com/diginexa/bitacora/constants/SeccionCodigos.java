package com.diginexa.bitacora.constants;

/**
 * Códigos estándar para identificar secciones de la bitácora.
 * Estos códigos reemplazan los IDs numéricos de la tabla secciones.
 */
public final class SeccionCodigos {

    private SeccionCodigos() {
        // Utility class - no instantiation
    }

    // ========== MÓDULO 1: CARACTERIZA TU ASIGNATURA ==========

    /** Identificación de la asignatura (datos básicos) */
    public static final String CARACTERIZA_IDENTIFICACION = "caracteriza-identificacion";

    /** Justificación de la asignatura (3 preguntas) */
    public static final String CARACTERIZA_JUSTIFICACION = "caracteriza-justificacion";

    // ========== MÓDULO 2: FACTORES SITUACIONALES ==========

    /** 17 preguntas sobre contexto, estudiantes y docente */
    public static final String FACTORES_SITUACIONALES = "factores-situacionales";

    // ========== MÓDULO 3: AJUSTES RAZONABLES ==========

    /** Ajustes desde contenidos, actividades, evaluación y dinámicas */
    public static final String AJUSTES_RAZONABLES = "ajustes-razonables";

    // ========== MÓDULO 4: RAP Y RAC ==========

    /** Resultados de Aprendizaje del Programa y del Curso */
    public static final String RAP_RAC = "rap-rac";

    // ========== MÓDULO 5: ACTIVIDADES DE APRENDIZAJE ==========

    /** Actividades de aprendizaje del curso */
    public static final String ACTIVIDADES_APRENDIZAJE = "actividades-aprendizaje";

    // ========== MÓDULO 6: CÓMO EVALUARÉ ==========

    /** Estrategias de evaluación */
    public static final String COMO_EVALUARE = "como-evaluare";

    // ========== MÓDULO 7: SECUENCIA DEL CURSO ==========

    /** Secuencia y cronograma del curso */
    public static final String SECUENCIA_CURSO = "secuencia-curso";

    // ========== MÓDULO 8: BIBLIOGRAFÍA ==========

    /** Referencias bibliográficas */
    public static final String BIBLIOGRAFIA = "bibliografia";

    // ========== LISTA COMPLETA ==========

    /** Array con todos los códigos válidos de secciones */
    public static final String[] TODOS = {
        CARACTERIZA_IDENTIFICACION,
        CARACTERIZA_JUSTIFICACION,
        FACTORES_SITUACIONALES,
        AJUSTES_RAZONABLES,
        RAP_RAC,
        ACTIVIDADES_APRENDIZAJE,
        COMO_EVALUARE,
        SECUENCIA_CURSO,
        BIBLIOGRAFIA
    };

    /**
     * Verifica si un código de sección es válido.
     * @param codigo El código a verificar
     * @return true si el código existe en la lista de códigos válidos
     */
    public static boolean esValido(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return false;
        }
        for (String c : TODOS) {
            if (c.equals(codigo)) {
                return true;
            }
        }
        return false;
    }
}
