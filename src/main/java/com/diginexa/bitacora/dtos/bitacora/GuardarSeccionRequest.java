package com.diginexa.bitacora.dtos.bitacora;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuardarSeccionRequest {

    @NotNull(message = "El ID de usuario es requerido")
    private Integer usuarioId;

    @NotBlank(message = "El código de sección es requerido")
    private String seccionCodigo;

    @NotNull(message = "Los datos son requeridos")
    private Map<String, Object> datos;

    private String estadoAvance;

    private Integer progresoPorcentaje;
}
