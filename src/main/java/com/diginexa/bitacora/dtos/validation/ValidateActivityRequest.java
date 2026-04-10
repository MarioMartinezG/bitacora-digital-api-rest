package com.diginexa.bitacora.dtos.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateActivityRequest {

    @JsonProperty("resultado_aprendizaje")
    private String resultadoAprendizaje;

    private String dimension;

    private String metodologia;

    private String descripcion;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("session_id")
    private String sessionId;
}
