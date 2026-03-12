package com.diginexa.bitacora.dtos.coordinador;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportarUsuariosResponse {

    private int totalProcesados;
    private int creados;
    private int errores;
    private List<ErrorFilaDTO> detalleErrores;

    @Data
    @Builder
    public static class ErrorFilaDTO {
        private int fila;
        private String correo;
        private String error;
    }
}
