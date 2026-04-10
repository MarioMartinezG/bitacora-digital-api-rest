package com.diginexa.bitacora.constants;

/**
 * Niveles de prioridad para las notificaciones.
 * Los valores de severity son compatibles con sistemas de toast estándar.
 */
public enum PrioridadNotificacion {
    /**
     * Crítico - Requiere acción inmediata.
     * Severity: error (rojo).
     */
    CRITICO("error", "Crítico"),

    /**
     * Alerta - Requiere atención.
     * Severity: warn (naranja/amarillo).
     */
    ALERTA("warn", "Alerta"),

    /**
     * Informativo - Solo para información.
     * Severity: info (azul).
     */
    INFO("info", "Informativo"),

    /**
     * Éxito - Acción completada exitosamente.
     * Severity: success (verde).
     */
    SUCCESS("success", "Éxito");

    private final String severity;
    private final String descripcion;

    PrioridadNotificacion(String severity, String descripcion) {
        this.severity = severity;
        this.descripcion = descripcion;
    }

    /**
     * Retorna el valor de severity para uso en componentes de UI.
     */
    public String getSeverity() {
        return severity;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
