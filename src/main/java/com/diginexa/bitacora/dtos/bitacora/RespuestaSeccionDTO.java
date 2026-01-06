package com.diginexa.bitacora.dtos.bitacora;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaSeccionDTO {

    private Long id;
    private Integer usuarioId;
    private String seccionCodigo;
    private Map<String, Object> datos;
    private String estadoAvance;
    private LocalDateTime fechaActualizacion;
}
