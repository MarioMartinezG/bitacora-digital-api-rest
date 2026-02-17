package com.diginexa.bitacora.dtos.bitacora;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomentoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaLimite;
    private Boolean activo;
    private List<String> secciones;
}
