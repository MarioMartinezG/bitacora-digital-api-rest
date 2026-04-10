package com.diginexa.bitacora.constants;

/**
 * Claves de configuración para el sistema de notificaciones.
 * Corresponden a los registros en la tabla configuracion_notificaciones.
 */
public final class ClaveConfiguracion {

    private ClaveConfiguracion() {
        // Utility class
    }

    /**
     * Días de anticipación para notificar vencimientos.
     * Formato: lista separada por comas (ej: "7,3,1")
     */
    public static final String DIAS_ANTICIPACION_VENCIMIENTO = "DIAS_ANTICIPACION_VENCIMIENTO";

    /**
     * Porcentaje de progreso para notificar al tutor.
     * Valor entero (ej: "80")
     */
    public static final String UMBRAL_PROGRESO_NOTIFICACION = "UMBRAL_PROGRESO_NOTIFICACION";

    /**
     * Habilitar/deshabilitar envío de emails.
     * Valor booleano (ej: "true")
     */
    public static final String EMAIL_HABILITADO = "EMAIL_HABILITADO";

    /**
     * Habilitar/deshabilitar notificaciones WebSocket.
     * Valor booleano (ej: "true")
     */
    public static final String WEBSOCKET_HABILITADO = "WEBSOCKET_HABILITADO";

    /**
     * Hora de ejecución del scheduler de vencimientos.
     * Formato: HH:mm (ej: "08:00")
     */
    public static final String HORA_EJECUCION_SCHEDULER = "HORA_EJECUCION_SCHEDULER";

    // ── Configuración exclusiva del Coordinador ──────────────────────────────

    /**
     * Umbrales de completitud de bitácora configurados por el coordinador.
     * Genera alertas cuando un estudiante alcanza estos porcentajes.
     * Formato: lista separada por comas (ej: "25,50,75")
     */
    public static final String UMBRALES_COMPLETITUD_COORDINADOR = "UMBRALES_COMPLETITUD_COORDINADOR";

    /**
     * Días antes del vencimiento para marcar un estudiante en demora (coordinador).
     * Formato: lista separada por comas (ej: "7,3,1")
     */
    public static final String DIAS_DEMORA_COORDINADOR = "DIAS_DEMORA_COORDINADOR";
}
