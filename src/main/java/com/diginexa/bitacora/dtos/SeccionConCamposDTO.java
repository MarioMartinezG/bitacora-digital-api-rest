package com.diginexa.bitacora.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeccionConCamposDTO {
    private Integer id;
    private String nombre;
    private String tipoSeccion;
    private Integer orden;
    private Map<String, Object> configuracion;
    private List<CampoSeccionDTO> campos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CampoSeccionDTO {
        private Integer id;
        private String label;
        private String tipoCampo;
        private List<String> opciones;
        private Boolean esRequerido;
        private Integer orden;
        private Map<String, Object> configuracion;
        private String placeholder;
    }
}
