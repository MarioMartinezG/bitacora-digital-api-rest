package com.diginexa.bitacora.dtos.bitacora;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearInstrumentoRequest {

    @NotBlank(message = "El label del instrumento es obligatorio")
    @Size(max = 300, message = "El label no puede superar 300 caracteres")
    private String label;
}
