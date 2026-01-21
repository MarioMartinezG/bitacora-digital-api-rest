package com.diginexa.bitacora.dtos.notificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarioModuloDTO {

    private Long id;

    @NotBlank(message = "El código de sección es requerido")
    private String seccionCodigo;

    @NotBlank(message = "El nombre del módulo es requerido")
    private String nombreModulo;

    @NotNull(message = "La fecha límite es requerida")
    private LocalDate fechaLimite;

    private String descripcion;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Campos calculados para el frontend
    private Integer diasRestantes;
    private String estadoVencimiento;  // VENCIDO, PROXIMO, NORMAL
}
