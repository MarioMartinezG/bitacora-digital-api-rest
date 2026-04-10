package com.diginexa.bitacora.dtos.tutor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorHealthResponse {
    private String status;

    @JsonProperty("ollama_connected")
    private boolean ollamaConnected;

    private String message;
}
