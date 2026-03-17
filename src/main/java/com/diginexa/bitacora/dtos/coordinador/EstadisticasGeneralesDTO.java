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
public class EstadisticasGeneralesDTO {
    private long totalEstudiantes;
    private long totalTutores;
    private long estudiantesActivos;
    private double promedioProgreso;
    private long alertasPendientes;
    private List<MetricaProgresoDTO> progresosPorSeccion;
}
