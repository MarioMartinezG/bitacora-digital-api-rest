package com.diginexa.bitacora.dtos.coordinador;

import lombok.Data;

@Data
public class UpdateAsignaturaRequest {
    private String nombre;
    private String codigo;
    private String descripcion;
    private Integer creditos;
    private Integer semestre;
    private Boolean activa;
}
