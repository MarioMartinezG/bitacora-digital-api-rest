package com.diginexa.bitacora.constants;

/**
 * Códigos estándar para identificar secciones de la bitácora.
 * Estos códigos reemplazan los IDs numéricos de la tabla secciones.
 */
public final class SeccionCodigos {

    private SeccionCodigos() {
        // Utility class - no instantiation
    }

    // ========== MÓDULO 1: OBSERVAR ==========

    /** Observar, registrar y actuar de manera oportuna */
    public static final String OBSERVAR = "observar";

    // ========== MÓDULO 2: CARACTERIZA TU ASIGNATURA ==========

    /** Identificación de tu curso (identificación, justificación) */
    public static final String CARACTERIZA = "caracteriza";

    // ========== MÓDULO 3: FACTORES SITUACIONALES ==========

    /** 17 preguntas sobre contexto, estudiantes y docente */
    public static final String FACTORES = "factores";

    // ========== MÓDULO 4: ACTIVIDADES DE APRENDIZAJE ==========

    /** Actividades de aprendizaje del curso */
    public static final String ACTIVIDADES = "actividades";

    // ========== MÓDULO 5: DISEÑO DE LA EVALUACIÓN ==========

    /** Estrategias de evaluación */
    public static final String EVALUACION = "evaluacion";

    // ========== MÓDULO 6: SECUENCIA DEL CURSO ==========

    /** Secuencia y cronograma del curso */
    public static final String SECUENCIA = "secuencia";

    // ========== MÓDULO 7: CALIFICACIÓN ==========

    /** Escala de calificación por resultado de aprendizaje */
    public static final String CALIFICACION = "calificacion";

    // ========== MÓDULO 8: BIBLIOGRAFÍA ==========

    /** Referencias bibliográficas */
    public static final String BIBLIOGRAFIA = "bibliografia";

    // ========== LISTA COMPLETA ==========

    /** Array con todos los códigos válidos de secciones (coincide con los códigos del frontend) */
    public static final String[] TODOS = {
        OBSERVAR,
        CARACTERIZA,
        FACTORES,
        ACTIVIDADES,
        EVALUACION,
        SECUENCIA,
        CALIFICACION,
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
