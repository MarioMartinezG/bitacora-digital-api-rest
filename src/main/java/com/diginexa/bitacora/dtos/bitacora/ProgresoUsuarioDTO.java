package com.diginexa.bitacora.dtos.bitacora;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgresoUsuarioDTO {

    private Integer usuarioId;
    private Map<String, ProgresoSeccionDTO> secciones;
    private Integer porcentajeTotal;
    private String estadoGeneral;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProgresoSeccionDTO {
        private String seccionCodigo;
        private String estado;
        private Integer porcentaje;
        /**
         * Estado asignado por el profesor/tutor.
         * Si está presente, el frontend debería mostrar este estado en lugar del calculado.
         */
        private String estadoProfesor;
        /**
         * Indica si el tutor ya revisó esta sección del estudiante.
         */
        private Boolean revisado;
    }
}
