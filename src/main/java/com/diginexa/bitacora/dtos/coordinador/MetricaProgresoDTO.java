package com.diginexa.bitacora.dtos.coordinador;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricaProgresoDTO {
    private String seccionCodigo;
    private String seccionNombre;
    private long totalEstudiantes;
    private long completados;
    private long enDesarrollo;
    private long sinAvances;
    private double porcentajeCompletado;
}
