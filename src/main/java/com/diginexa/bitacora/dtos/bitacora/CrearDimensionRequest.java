package com.diginexa.bitacora.dtos.bitacora;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearDimensionRequest {

    @NotBlank(message = "El nombre de la dimensión es obligatorio")
    @Size(max = 300, message = "El nombre no puede superar 300 caracteres")
    private String nombre;
}
