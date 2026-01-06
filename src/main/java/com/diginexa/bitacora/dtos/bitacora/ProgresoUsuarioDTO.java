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
    }
}
