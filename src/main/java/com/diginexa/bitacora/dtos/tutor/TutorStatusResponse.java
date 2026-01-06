package com.diginexa.bitacora.dtos.tutor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorStatusResponse {
    private String status;
    private List<String> models;
    private Map<String, Object> systemInfo;
}
