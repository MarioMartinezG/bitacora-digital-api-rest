package com.diginexa.bitacora.exceptions;

import com.diginexa.bitacora.dtos.ErrorResponse;
import com.diginexa.bitacora.exceptions.domain.*;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.exceptions.domain.TutorServiceException;
import com.diginexa.bitacora.exceptions.security.InvalidJwtTokenException;
import com.diginexa.bitacora.exceptions.security.JwtAuthenticationException;
import com.diginexa.bitacora.exceptions.validation.DuplicateMenuException;
import com.diginexa.bitacora.exceptions.validation.DuplicateMenuItemException;
import com.diginexa.bitacora.exceptions.validation.RespuestaValidationException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===== EXCEPCIONES DE AUTENTICACIÓN =====

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("Credenciales inválidas", "BadCredentials", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("Usuario no encontrado", "UserNotFound", HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ex.getMessage(), "EmailAlreadyExists", HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(InvalidJwtTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJwtToken(InvalidJwtTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(ex.getMessage(), "InvalidToken", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(InvalidEmailFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmailFormat(InvalidEmailFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ex.getMessage(), "InvalidEmailFormat", HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtAuthenticationException(JwtAuthenticationException ex) {
        log.warn("Error de autenticación JWT: {} - {}", ex.getErrorCode(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(ex.getMessage(), ex.getErrorCode(), HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(ExpiredJwtException ex) {
        log.warn("Token JWT expirado: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("Token JWT expirado", "TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJwtException(MalformedJwtException ex) {
        log.warn("Token JWT malformado: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("Token JWT malformado", "TOKEN_MALFORMED", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ErrorResponse> handleSignatureException(SignatureException ex) {
        log.warn("Firma JWT inválida: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("Firma del token JWT inválida", "TOKEN_SIGNATURE_INVALID", HttpStatus.UNAUTHORIZED.value()));
    }

    // ===== EXCEPCIONES DE DOMINIO =====

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFound(RoleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getMessage(), "RoleNotFound", HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(MenuNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMenuNotFound(MenuNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getMessage(), "MenuNotFound", HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(DuplicateMenuException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateMenu(DuplicateMenuException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ex.getMessage(), "DuplicateMenu", HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(DuplicateMenuItemException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateMenuItem(DuplicateMenuItemException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ex.getMessage(), "DuplicateMenuItem", HttpStatus.CONFLICT.value()));
    }

    // ===== VALIDACIONES Y EXCEPCIONES GENERALES =====

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Mejorado: Mostrar todos los errores de validación
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "Errores de validación: " + String.join(", ", errors.values());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.withDetails(message, "ValidationError", HttpStatus.BAD_REQUEST.value(), errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ex.getMessage(), "IllegalArgument", HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ModuloNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleModuloNotFound(ModuloNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getMessage(), "ModuloNotFound", HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(SeccionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSeccionNotFound(SeccionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getMessage(), "SeccionNotFound", HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(CampoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCampoNotFound(CampoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getMessage(), "CampoNotFound", HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(RespuestaValidationException.class)
    public ResponseEntity<ErrorResponse> handleRespuestaValidation(RespuestaValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ex.getMessage(), "RespuestaValidationError", HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getMessage(), "ResourceNotFound", HttpStatus.NOT_FOUND.value()));
    }

    // ===== EXCEPCIONES DEL TUTOR INTELIGENTE =====

    @ExceptionHandler(TutorServiceException.class)
    public ResponseEntity<ErrorResponse> handleTutorServiceException(TutorServiceException ex) {
        log.error("Error en servicio de tutor inteligente: {} - {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of(ex.getMessage(), ex.getErrorCode(), HttpStatus.SERVICE_UNAVAILABLE.value()));
    }

    // ===== CUALQUIER OTRA EXCEPCIÓN NO CONTROLADA =====

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        // Log the exception for debugging
        log.error("Error interno del servidor no controlado: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("Error interno del servidor", "InternalServerError", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}

