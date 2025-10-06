package com.diginexa.bitacora.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("role")
    private Integer rolId;

    private String nombre;
    private String correo;

    public UserResponse(Integer id, String correo, Integer rolId, String nombre) {
        this.id = id;
        this.correo = correo;
        this.rolId = rolId;
        this.nombre = nombre;
        this.username = extractUsernameFromEmail(correo);
    }

    private String extractUsernameFromEmail(String correo) {
        if (correo != null && correo.contains("@")) {
            return correo.substring(0, correo.indexOf("@"));
        }
        return correo;
    }
}
