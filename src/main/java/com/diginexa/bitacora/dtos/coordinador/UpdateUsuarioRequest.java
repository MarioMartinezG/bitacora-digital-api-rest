package com.diginexa.bitacora.dtos.coordinador;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUsuarioRequest {
    private String nombre;

    @Email(message = "El formato del correo no es válido")
    private String correo;

    private String contrasena;
    private List<Integer> roles;
    private Boolean activo;
}
