package com.diginexa.bitacora.dtos.coordinador;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaDTO {
    private Integer id;
    private String nombre;
    private String codigo;
    private String descripcion;
    private Integer creditos;
    private Integer semestre;
    private Boolean activa;
    private int totalEstudiantes;
    private int totalTutores;
    private LocalDateTime fechaCreacion;
}
