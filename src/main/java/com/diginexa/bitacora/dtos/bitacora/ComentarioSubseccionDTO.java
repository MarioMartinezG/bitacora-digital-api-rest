package com.diginexa.bitacora.dtos.bitacora;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComentarioSubseccionDTO {
    private Long id;
    private Integer tutorId;
    private String nombreTutor;
    private Integer estudianteId;
    private String seccionCodigo;
    private String subseccionCodigo;
    private String comentario;
    private LocalDateTime fechaCreacion;
}
