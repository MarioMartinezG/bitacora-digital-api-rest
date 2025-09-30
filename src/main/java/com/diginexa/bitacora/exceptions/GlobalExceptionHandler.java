package com.diginexa.bitacora.exceptions;

import com.diginexa.bitacora.dtos.ErrorResponse;
import com.diginexa.bitacora.exceptions.domain.MenuNotFoundException;
import com.diginexa.bitacora.exceptions.domain.RoleNotFoundException;
import com.diginexa.bitacora.exceptions.validation.DuplicateMenuException;
import com.diginexa.bitacora.exceptions.validation.DuplicateMenuItemException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ----- EXCEPCIONES DE DOMINIO -----
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

    // Captura validaciones de @Valid / @Validated
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(message, "ValidationError", HttpStatus.BAD_REQUEST.value()));
    }

    // ----- CUALQUIER OTRA EXCEPCIÓN NO CONTROLADA -----
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("Error inesperado: " + ex.getMessage(), "InternalServerError", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}

