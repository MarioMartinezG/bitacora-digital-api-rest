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
public class IndexStatusResponse {

    @JsonProperty("task_id")
    private String taskId;

    private String status;
    private String message;

    @JsonProperty("started_at")
    private String startedAt;

    @JsonProperty("completed_at")
    private String completedAt;

    @JsonProperty("documents_processed")
    private Integer documentsProcessed;

    @JsonProperty("chunks_created")
    private Integer chunksCreated;

    private String error;
}
