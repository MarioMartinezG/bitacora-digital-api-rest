package com.diginexa.bitacora.dtos.bitacora;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedioDTO {
    private Long id;
    private String label;
    private String value;
    private String categoria;
    private Boolean activo;
}
