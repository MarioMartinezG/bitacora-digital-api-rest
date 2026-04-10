package com.diginexa.bitacora.dtos.bitacora;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemaContenidoDTO {

    private Long id;

    @NotNull(message = "El número de tema es requerido")
    private Integer numeroTema;

    @NotBlank(message = "El nombre del tema es requerido")
    private String nombreTema;

    private List<String> subtemas;
}
