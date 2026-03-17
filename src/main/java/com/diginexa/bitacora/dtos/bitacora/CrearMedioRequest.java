package com.diginexa.bitacora.dtos.bitacora;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearMedioRequest {

    @NotBlank(message = "El label del medio es obligatorio")
    @Size(max = 300, message = "El label no puede superar 300 caracteres")
    private String label;

    @NotBlank(message = "La categoría es obligatoria")
    @Pattern(regexp = "ESCRITOS|ORALES|PRACTICOS", message = "Categoría inválida. Valores: ESCRITOS, ORALES, PRACTICOS")
    private String categoria;
}
