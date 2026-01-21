package com.diginexa.bitacora.dtos.notificacion;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionNotificacionDTO {

    private Long id;

    @NotBlank(message = "La clave es requerida")
    private String clave;

    @NotBlank(message = "El valor es requerido")
    private String valor;

    private String descripcion;
    private String tipoDato;
}
