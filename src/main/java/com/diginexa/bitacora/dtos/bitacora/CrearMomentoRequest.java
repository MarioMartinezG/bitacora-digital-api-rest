package com.diginexa.bitacora.dtos.bitacora;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearMomentoRequest {

    @NotBlank(message = "El nombre del momento es requerido")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La fecha límite es requerida")
    private LocalDate fechaLimite;

    @NotNull(message = "Las secciones son requeridas")
    private List<String> secciones;
}
