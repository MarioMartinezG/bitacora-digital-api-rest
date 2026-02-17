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

    /** Caracteriza tu asignatura (identificación, justificación, contenidos) */
    public static final String CARACTERIZA = "caracteriza";

    // ========== MÓDULO 2: FACTORES SITUACIONALES ==========

    /** 17 preguntas sobre contexto, estudiantes y docente */
    public static final String FACTORES = "factores";

    // ========== MÓDULO 3: AJUSTES RAZONABLES ==========

    /** Ajustes desde contenidos, actividades, evaluación y dinámicas */
    public static final String AJUSTES = "ajustes";

    // ========== MÓDULO 4: RAP Y RAC ==========

    /** Resultados de Aprendizaje del Programa y del Curso */
    public static final String RAP_RAC = "rap-rac";

    // ========== MÓDULO 5: ACTIVIDADES DE APRENDIZAJE ==========

    /** Actividades de aprendizaje del curso */
    public static final String ACTIVIDADES = "actividades";

    // ========== MÓDULO 6: CÓMO EVALUARÉ ==========

    /** Estrategias de evaluación */
    public static final String EVALUACION = "evaluacion";

    // ========== MÓDULO 7: SECUENCIA DEL CURSO ==========

    /** Secuencia y cronograma del curso */
    public static final String SECUENCIA = "secuencia";

    // ========== MÓDULO 8: BIBLIOGRAFÍA ==========

    /** Referencias bibliográficas */
    public static final String BIBLIOGRAFIA = "bibliografia";

    // ========== LISTA COMPLETA ==========

    /** Array con todos los códigos válidos de secciones (coincide con los códigos del frontend) */
    public static final String[] TODOS = {
        CARACTERIZA,
        FACTORES,
        AJUSTES,
        RAP_RAC,
        ACTIVIDADES,
        EVALUACION,
        SECUENCIA,
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
