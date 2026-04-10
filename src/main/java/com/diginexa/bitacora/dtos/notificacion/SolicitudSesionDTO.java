package com.diginexa.bitacora.dtos.notificacion;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudSesionDTO {

    private Long id;
    private Integer estudianteId;
    private String nombreEstudiante;
    private String correoEstudiante;
    private Integer tutorId;
    private String nombreTutor;
    private String correoTutor;
    private String motivo;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;
    private String notasTutor;
}
