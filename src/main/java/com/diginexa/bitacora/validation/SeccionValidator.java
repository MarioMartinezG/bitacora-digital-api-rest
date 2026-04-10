package com.diginexa.bitacora.validation;

import com.diginexa.bitacora.constants.SeccionCodigos;
import com.diginexa.bitacora.exceptions.validation.RespuestaValidationException;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validador para respuestas de secciones.
 * Implementa validación básica por tipo de sección.
 */
@Component
public class SeccionValidator {

    private static final Map<String, List<String>> CAMPOS_REQUERIDOS = new HashMap<>();

    static {
        // Factores Situacionales - preguntas obligatorias
        CAMPOS_REQUERIDOS.put(SeccionCodigos.FACTORES, Arrays.asList(
            "pregunta1", "pregunta2", "pregunta3", "pregunta4", "pregunta5",
            "pregunta6", "pregunta7", "pregunta8", "pregunta9"
        ));

        // Caracteriza: sin campos requeridos a nivel raíz
    }

    /**
     * Valida los datos de una sección según su código.
     * @param seccionCodigo Código de la sección
     * @param datos Datos a validar
     * @throws RespuestaValidationException si la validación falla
     */
    public void validar(String seccionCodigo, Map<String, Object> datos) {
        if (datos == null) {
            throw new RespuestaValidationException("Los datos no pueden ser nulos");
        }

        if (!SeccionCodigos.esValido(seccionCodigo)) {
            throw new RespuestaValidationException("Código de sección inválido: " + seccionCodigo);
        }

        // Validar campos requeridos si están definidos para esta sección
        List<String> requeridos = CAMPOS_REQUERIDOS.get(seccionCodigo);
        if (requeridos != null && !requeridos.isEmpty()) {
            validarCamposRequeridos(datos, requeridos);
        }

        // Validaciones específicas por sección
        switch (seccionCodigo) {
            case SeccionCodigos.FACTORES:
                validarFactoresSituacionales(datos);
                break;
            case SeccionCodigos.CARACTERIZA:
                validarJustificacion(datos);
                break;
            default:
                // Sin validación adicional
                break;
        }
    }

    /**
     * Validación sin excepción - retorna lista de errores.
     */
    public List<String> validarSinExcepcion(String seccionCodigo, Map<String, Object> datos) {
        List<String> errores = new ArrayList<>();

        if (datos == null) {
            errores.add("Los datos no pueden ser nulos");
            return errores;
        }

        if (!SeccionCodigos.esValido(seccionCodigo)) {
            errores.add("Código de sección inválido: " + seccionCodigo);
            return errores;
        }

        List<String> requeridos = CAMPOS_REQUERIDOS.get(seccionCodigo);
        if (requeridos != null) {
            for (String campo : requeridos) {
                Object valor = datos.get(campo);
                if (valor == null || valor.toString().trim().isEmpty()) {
                    errores.add("Campo requerido faltante: " + campo);
                }
            }
        }

        return errores;
    }

    private void validarCamposRequeridos(Map<String, Object> datos, List<String> requeridos) {
        List<String> faltantes = new ArrayList<>();

        for (String campo : requeridos) {
            Object valor = datos.get(campo);
            if (valor == null || valor.toString().trim().isEmpty()) {
                faltantes.add(campo);
            }
        }

        if (!faltantes.isEmpty()) {
            throw new RespuestaValidationException(
                "Campos requeridos faltantes: " + String.join(", ", faltantes)
            );
        }
    }

    private void validarFactoresSituacionales(Map<String, Object> datos) {
        // Validar que pregunta3 (número de estudiantes) sea numérico
        Object pregunta3 = datos.get("pregunta3");
        if (pregunta3 != null && !esNumerico(pregunta3.toString())) {
            throw new RespuestaValidationException(
                "El campo 'pregunta3' (número de estudiantes) debe ser numérico"
            );
        }

        // Validar pregunta9 condicional
        Object pregunta9 = datos.get("pregunta9");
        if (pregunta9 != null && "si".equalsIgnoreCase(pregunta9.toString())) {
            Object detalle = datos.get("detallePregunta9");
            if (detalle == null || detalle.toString().trim().isEmpty()) {
                throw new RespuestaValidationException(
                    "Si pregunta9 es 'sí', debe proporcionar el detalle en 'detallePregunta9'"
                );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validarJustificacion(Map<String, Object> datos) {
        Object justificacionObj = datos.get("justificacion");
        if (justificacionObj instanceof Map) {
            Map<String, Object> justificacion = (Map<String, Object>) justificacionObj;
            Object respuesta = justificacion.get("respuesta");
            if (respuesta != null && respuesta.toString().trim().isEmpty()) {
                throw new RespuestaValidationException(
                    "El campo 'respuesta' de justificación no puede estar vacío"
                );
            }
        }
    }

    private boolean esNumerico(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
