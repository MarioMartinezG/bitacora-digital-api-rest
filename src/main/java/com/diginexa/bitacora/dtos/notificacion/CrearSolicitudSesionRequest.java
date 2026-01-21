package com.diginexa.bitacora.dtos.notificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearSolicitudSesionRequest {

    @NotNull(message = "El ID del estudiante es requerido")
    private Integer estudianteId;

    @NotBlank(message = "El motivo de la solicitud es requerido")
    private String motivo;
}
