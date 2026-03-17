package com.diginexa.bitacora.dtos.bitacora;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TecnicaDTO {
    private Long id;
    private String label;
    private String value;
    private String grupo;
    private Boolean activo;
}
