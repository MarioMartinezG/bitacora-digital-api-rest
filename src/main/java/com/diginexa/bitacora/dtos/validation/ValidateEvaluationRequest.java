package com.diginexa.bitacora.dtos.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateEvaluationRequest {

    @JsonProperty("resultado_aprendizaje")
    private String resultadoAprendizaje;

    @JsonProperty("nombre_actividad")
    private String nombreActividad;

    @JsonProperty("descripcion_evaluacion")
    private String descripcionEvaluacion;

    private String tipo;

    private String momento;

    private String actores;

    private List<String> medios;

    private List<String> tecnicas;

    private List<String> instrumentos;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("session_id")
    private String sessionId;
}
