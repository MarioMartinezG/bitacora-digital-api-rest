package com.diginexa.bitacora.dtos.coordinador;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteProgresoDTO {
    private Integer estudianteId;
    private String estudianteNombre;
    private String estudianteCorreo;
    private String tutorNombre;
    private double porcentajeGeneral;
    private List<SeccionProgresoDTO> secciones;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeccionProgresoDTO {
        private String seccionCodigo;
        private String estado;
        private Integer porcentaje;
    }
}
