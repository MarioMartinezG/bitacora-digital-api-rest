package com.diginexa.bitacora.dtos.bitacora;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para marcar o desmarcar una sección como revisada por el tutor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarcarRevisadoRequest {

    @NotNull(message = "El ID del estudiante es requerido")
    private Integer estudianteId;

    @NotBlank(message = "El código de sección es requerido")
    private String seccionCodigo;

    @NotNull(message = "El valor de revisado es requerido")
    private Boolean revisado;
}
