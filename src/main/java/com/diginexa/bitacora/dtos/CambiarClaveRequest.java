package com.diginexa.bitacora.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CambiarClaveRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String claveActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String claveNueva;
}
