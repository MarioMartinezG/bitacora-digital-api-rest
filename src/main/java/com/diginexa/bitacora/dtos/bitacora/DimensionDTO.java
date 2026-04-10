package com.diginexa.bitacora.dtos.bitacora;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionDTO {
    private Long id;
    private String nombre;
    private Boolean activo;
}
