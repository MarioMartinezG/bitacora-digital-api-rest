package com.diginexa.bitacora.dtos.notificacion;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponderSolicitudRequest {

    @NotBlank(message = "El estado es requerido")
    private String estado;  // ACEPTADA, RECHAZADA

    private String notasTutor;
}
