package com.diginexa.bitacora.dtos.bitacora;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearTecnicaRequest {

    @NotBlank(message = "El label de la técnica es obligatorio")
    @Size(max = 300, message = "El label no puede superar 300 caracteres")
    private String label;

    @NotBlank(message = "El grupo es obligatorio")
    @Pattern(regexp = "ALUMNO_NO_INTERVIENE|ALUMNO_PARTICIPA", message = "Grupo inválido. Valores: ALUMNO_NO_INTERVIENE, ALUMNO_PARTICIPA")
    private String grupo;
}
