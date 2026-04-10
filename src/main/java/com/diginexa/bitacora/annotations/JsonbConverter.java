package com.diginexa.bitacora.annotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Converter()
public class JsonbConverter implements AttributeConverter<Object, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonbConverter() {
        // Asegurar que el ObjectMapper esté configurado
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        try {
            if (attribute == null) {
                return "{}";
            }
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al convertir a JSONB", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty()) {
                return "{}";
            }
            if (dbData.startsWith("[")) {
                return objectMapper.readValue(dbData, List.class);
            } else if (dbData.startsWith("{")) {
                return objectMapper.readValue(dbData, Map.class);
            } else {
                return objectMapper.readValue(dbData, Object.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al convertir a JSONB: " + dbData, e);
        }
    }
}
