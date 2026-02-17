package com.diginexa.bitacora.dtos.bitacora;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearComentarioSubseccionRequest {

    @NotNull(message = "El ID del estudiante es requerido")
    private Integer estudianteId;

    @NotBlank(message = "El código de sección es requerido")
    private String seccionCodigo;

    @NotBlank(message = "El código de sub-sección es requerido")
    private String subseccionCodigo;

    @NotBlank(message = "El comentario es requerido")
    private String comentario;
}
