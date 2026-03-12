package com.diginexa.bitacora.dtos.coordinador;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    private Integer id;
    private String nombre;
    private String correo;
    private List<Integer> roles;
    private List<String> rolesNombres;
    private Boolean activo;
    private Boolean graduado;
    private Boolean requiereCambioClave;
    private LocalDateTime ultimoAcceso;
    private LocalDateTime fechaCreacion;
}
