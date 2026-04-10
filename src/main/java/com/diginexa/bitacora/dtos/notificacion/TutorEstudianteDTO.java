package com.diginexa.bitacora.dtos.notificacion;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorEstudianteDTO {

    private Long id;
    private Integer tutorId;
    private String nombreTutor;
    private String correoTutor;
    private Integer estudianteId;
    private String nombreEstudiante;
    private String correoEstudiante;
    private LocalDateTime fechaAsignacion;
    private Boolean activo;
}
