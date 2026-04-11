package com.diginexa.bitacora.integration;

import com.diginexa.bitacora.BitacoraDigitalApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BitacoraDigitalApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MenuControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "estudiante")
        //Simula un usuario autenticado
    void shouldReturnMenuForRoleEstudiante() throws Exception {
        mockMvc.perform(get("/api/menu/{role}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].label").value("Inicio"))
                .andExpect(jsonPath("$[0].items[0].label").value("Dashboard"))
                .andExpect(jsonPath("$[0].items[1].label").value("Documentos guía del curso"))
                .andExpect(jsonPath("$[1].label").value("Bitácora Digital"))
                .andExpect(jsonPath("$[1].items[0].label").value("Caracteriza tu Asignatura"));
    }

    @Test
    void shouldReturnUnauthorizedWhenNoAuthentication() throws Exception {
        // Prueba sin autenticación
        mockMvc.perform(get("/api/menu/{role}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
