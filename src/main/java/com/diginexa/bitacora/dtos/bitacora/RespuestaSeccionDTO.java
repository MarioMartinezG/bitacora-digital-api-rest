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
    /**
     * Estado asignado por el profesor/tutor.
     * Si está presente, tiene prioridad sobre estadoAvance para determinar el estado visual.
     */
    private String estadoProfesor;
    /**
     * Porcentaje de completitud calculado (0-100).
     */
    private Integer progresoPorcentaje;
    private LocalDateTime fechaActualizacion;
}
