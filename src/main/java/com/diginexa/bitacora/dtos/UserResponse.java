package com.diginexa.bitacora.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("roles")
    private List<Integer> roles;

    private String nombre;
    private String correo;

    public UserResponse(Integer id, String correo, List<Integer> roles, String nombre) {
        this.id = id;
        this.correo = correo;
        this.roles = roles;
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
