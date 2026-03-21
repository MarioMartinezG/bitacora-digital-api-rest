package com.diginexa.bitacora.constants;

/**
 * Tipos de notificación soportados por el sistema.
 */
public enum TipoNotificacion {
    VENCIMIENTO_PROXIMO("Vencimiento próximo de módulo"),
    SOLICITUD_SESION("Solicitud de sesión con tutor"),
    RESPUESTA_SOLICITUD("Respuesta a solicitud de sesión"),
    UMBRAL_ALCANZADO("Umbral de progreso alcanzado"),
    COMENTARIO_TUTOR("Comentario del tutor en sub-sección"),
    COMENTARIO_RESUELTO("Comentario marcado como resuelto por el estudiante"),
    ESTADO_TUTOR_ACTUALIZADO("Cambio de estado por el tutor en sub-sección"),
    ESTUDIANTE_EN_RIESGO("Estudiante en riesgo de no completar la bitácora");

    private final String descripcion;

    TipoNotificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
