package com.diginexa.bitacora.dtos.coordinador;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaSistemaDTO {
    private Integer id;
    private String tipo;
    private String prioridad;
    private String titulo;
    private String mensaje;
    private Integer usuarioReferenciaId;
    private String usuarioReferenciaNombre;
    private Map<String, Object> datosAdicionales;
    private Boolean resuelta;
    private LocalDateTime fechaCreacion;
}
