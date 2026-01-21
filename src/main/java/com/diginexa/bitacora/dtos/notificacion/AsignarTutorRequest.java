package com.diginexa.bitacora.dtos.notificacion;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarTutorRequest {

    @NotNull(message = "El ID del tutor es requerido")
    private Integer tutorId;

    @NotNull(message = "El ID del estudiante es requerido")
    private Integer estudianteId;
}
