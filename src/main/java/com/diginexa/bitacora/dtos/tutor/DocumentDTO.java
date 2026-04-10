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
public class DocumentDTO {
    private String filename;
    private String path;

    @JsonProperty("size_bytes")
    private Long sizeBytes;

    private String extension;

    @JsonProperty("modified_at")
    private String modifiedAt;
}
