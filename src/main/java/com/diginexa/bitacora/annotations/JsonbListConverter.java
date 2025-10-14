package com.diginexa.bitacora.annotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter
public class JsonbListConverter implements AttributeConverter<List<Object>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonbListConverter() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public String convertToDatabaseColumn(List<Object> attribute) {
        try {
            if (attribute == null) {
                return "[]";
            }
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al convertir List a JSONB", e);
        }
    }

    @Override
    public List<Object> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty() || dbData.equals("[]")) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(dbData, new TypeReference<List<Object>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error al convertir JSONB a List: " + dbData, e);
        }
    }
}
