package com.diginexa.bitacora.dtos.bitacora;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoTutorSubseccionDTO {
    private Long id;
    private String seccionCodigo;
    private String subseccionCodigo;
    private String estado;
    private LocalDateTime fechaActualizacion;
}
