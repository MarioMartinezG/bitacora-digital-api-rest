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
public class DocumentListResponse {
    private List<DocumentDTO> documents;

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("raw_path")
    private String rawPath;
}
