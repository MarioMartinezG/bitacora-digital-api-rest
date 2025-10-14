package com.diginexa.bitacora.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaRequest {
    @NotNull(message = "El ID de usuario es requerido")
    private Integer usuarioId;

    @NotNull(message = "El ID de sección es requerido")
    private Long seccionId;

    @NotNull(message = "El ID de campo es requerido")
    private Long campoId;

    private String respuestaTexto;

    private Object respuestaJson;

    @NotBlank(message = "El estado de avance es requerido")
    @Pattern(regexp = "sin_avances|en_desarrollo|completado", message = "Estado de avance inválido")
    private String estadoAvance;
}
