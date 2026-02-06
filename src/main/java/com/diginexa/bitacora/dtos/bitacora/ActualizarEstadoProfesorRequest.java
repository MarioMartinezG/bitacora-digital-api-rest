package com.diginexa.bitacora.dtos.bitacora;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * DTO para la solicitud de actualización de estado por parte del profesor/tutor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarEstadoProfesorRequest {

    @NotNull(message = "El ID del estudiante es requerido")
    private Integer estudianteId;

    @NotBlank(message = "El código de sección es requerido")
    private String seccionCodigo;

    @NotBlank(message = "El estado es requerido")
    @Pattern(regexp = "^(sin_avances|en_desarrollo|completado)$",
             message = "El estado debe ser: sin_avances, en_desarrollo o completado")
    private String estadoProfesor;
}
