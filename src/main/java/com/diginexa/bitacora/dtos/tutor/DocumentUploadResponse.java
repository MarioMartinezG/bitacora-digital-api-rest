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
public class DocumentUploadResponse {
    private String message;

    @JsonProperty("uploaded_files")
    private List<DocumentDTO> uploadedFiles;

    @JsonProperty("total_uploaded")
    private Integer totalUploaded;

    private List<String> errors;
}
