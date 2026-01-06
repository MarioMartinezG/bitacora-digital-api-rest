package com.diginexa.bitacora.dtos.tutor;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorAskRequest {

    @NotBlank(message = "La pregunta es obligatoria")
    private String question;

    @NotBlank(message = "El modulo es obligatorio")
    private String module;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_role")
    private String userRole;

    @JsonProperty("course_id")
    private String courseId;
}
