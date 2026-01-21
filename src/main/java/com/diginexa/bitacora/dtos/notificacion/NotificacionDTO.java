package com.diginexa.bitacora.dtos.notificacion;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionDTO {

    private Long id;
    private Integer usuarioId;
    private String tipo;
    private String prioridad;
    private String severity;  // Para compatibilidad con componentes Toast UI
    private String titulo;
    private String mensaje;
    private Map<String, Object> datosAdicionales;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLectura;
}
