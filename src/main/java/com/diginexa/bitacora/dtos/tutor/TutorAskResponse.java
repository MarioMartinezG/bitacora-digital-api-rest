package com.diginexa.bitacora.dtos.tutor;

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
public class TutorAskResponse {

    @JsonProperty("request_id")
    private String requestId;

    private String timestamp;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("session_id")
    private String sessionId;

    private String module;

    private String answer;

    private Double confidence;

    private List<String> sources;

    @JsonProperty("suggested_actions")
    private List<String> suggestedActions;

    @JsonProperty("model_used")
    private String modelUsed;

    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;
}
