package com.diginexa.bitacora.dtos.bitacora;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentoDTO {
    private Long id;
    private String label;
    private String value;
    private Boolean activo;
}
