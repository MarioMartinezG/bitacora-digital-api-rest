package com.diginexa.bitacora.dtos.coordinador;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateUsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    private String correo;

    // Opcional: si no se provee, se asigna la clave por defecto del sistema
    private String contrasena;

    @NotEmpty(message = "Debe asignar al menos un rol")
    private List<Integer> roles;
}
