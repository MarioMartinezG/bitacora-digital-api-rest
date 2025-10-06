package com.diginexa.bitacora.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "NoAutorizado");

        body.put("message", getSpanishMessage(authException));
        body.put("timestamp", LocalDateTime.now());
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }

    private String getSpanishMessage(AuthenticationException exception) {
        String exceptionMessage = exception.getMessage();

        if (exceptionMessage.contains("Full authentication is required")) {
            return "Se requiere token de autenticación para acceder a este recurso";
        } else if (exceptionMessage.contains("Bad credentials")) {
            return "Credenciales inválidas";
        } else if (exceptionMessage.contains("JWT expired")) {
            return "El token JWT ha expirado";
        } else if (exceptionMessage.contains("Unable to parse JWT")) {
            return "No se puede analizar el token JWT";
        } else if (exceptionMessage.contains("JWT signature does not match")) {
            return "La firma del token JWT no coincide";
        } else if (exceptionMessage.contains("JWT strings must contain exactly 2 period characters")) {
            return "Formato de token JWT inválido";
        } else {
            return "Error de autenticación: " + exceptionMessage;
        }
    }
}
