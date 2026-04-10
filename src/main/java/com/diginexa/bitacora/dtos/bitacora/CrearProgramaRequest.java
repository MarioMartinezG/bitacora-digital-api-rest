package com.diginexa.bitacora.dtos.bitacora;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearProgramaRequest {

    @NotBlank(message = "El nombre del programa es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String nombre;
}
