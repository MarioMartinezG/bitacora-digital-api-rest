package com.diginexa.bitacora.dtos.notificacion;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionResumenDTO {

    private Integer usuarioId;
    private Long totalNoLeidas;
    private Long totalCriticas;
    private Long totalAlertas;
    private Long totalInfo;
}
