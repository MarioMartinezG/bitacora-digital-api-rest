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
public class ValidationResponse {

    @JsonProperty("request_id")
    private String requestId;

    private String timestamp;

    private String verdict;

    private String justification;

    private List<Object> sources;

    @JsonProperty("model_used")
    private String modelUsed;

    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;
}
