package com.diginexa.bitacora.dtos.coordinador;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAsignaturaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;

    private String descripcion;
    private Integer creditos;
    private Integer semestre;
}
