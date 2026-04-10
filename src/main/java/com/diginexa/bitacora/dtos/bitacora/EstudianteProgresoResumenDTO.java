package com.diginexa.bitacora.dtos.bitacora;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteProgresoResumenDTO {
    private Integer estudianteId;
    private String nombreEstudiante;
    private String correoEstudiante;
    private Integer porcentajeTotal;
    private String estadoGeneral;
    private Map<String, ProgresoUsuarioDTO.ProgresoSeccionDTO> secciones;
}
