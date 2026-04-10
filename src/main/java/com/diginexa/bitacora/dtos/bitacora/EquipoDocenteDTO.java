package com.diginexa.bitacora.dtos.bitacora;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoDocenteDTO {

    private Long id;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    private String correo;
    private String rol;
    private String atencion;
    private List<String> dias;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String horario;
    private String sitio;
    private Integer orden;
}
