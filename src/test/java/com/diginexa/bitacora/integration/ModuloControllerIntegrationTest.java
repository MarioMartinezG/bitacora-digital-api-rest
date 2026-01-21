package com.diginexa.bitacora.integration;

import com.diginexa.bitacora.BitacoraDigitalApplication;
import com.diginexa.bitacora.entities.Modulo;
import com.diginexa.bitacora.repositories.ModuloRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Disabled("Controlador depreciado")
@SpringBootTest(classes = BitacoraDigitalApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ModuloControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModuloRepository moduloRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Limpiar y preparar datos de prueba
        moduloRepository.deleteAll();
    }

    private Modulo crearModuloDePrueba() {
        Modulo modulo = Modulo.builder()
                .nombre("Módulo de Prueba Integración")
                .descripcion("Descripción para pruebas de integración")
                .orden(1)
                .totalSecciones(3)
                .tipoEstructura("simple")
                .fechaInicio(LocalDate.of(2025, 1, 1))
                .fechaFin(LocalDate.of(2025, 12, 31))
                .build();
        return moduloRepository.save(modulo);
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void shouldReturnAllModulosForAuthenticatedUser() throws Exception {
        // Arrange
        Modulo modulo = crearModuloDePrueba();

        // Act & Assert
        mockMvc.perform(get("/api/modulos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Módulo de Prueba Integración")))
                .andExpect(jsonPath("$[0].descripcion", is("Descripción para pruebas de integración")))
                .andExpect(jsonPath("$[0].orden", is(1)));
    }

    @Test
    void shouldReturnUnauthorizedWhenNoAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/modulos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void shouldReturnModuloByIdWhenExists() throws Exception {
        // Arrange
        Modulo modulo = crearModuloDePrueba();
        Long moduloId = modulo.getId();

        // Act & Assert
        mockMvc.perform(get("/api/modulos/{id}", moduloId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(moduloId.intValue())))
                .andExpect(jsonPath("$.nombre", is("Módulo de Prueba Integración")))
                .andExpect(jsonPath("$.orden", is(1)));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void shouldReturnNotFoundWhenModuloNotExists() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/modulos/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Módulo no encontrado")))
                .andExpect(jsonPath("$.error", is("ModuloNotFound")));
    }

    @Test
    @WithMockUser(roles = "tutor")
    void shouldCreateModuloWithValidData() throws Exception {
        // Arrange
        Modulo nuevoModulo = Modulo.builder()
                .nombre("Nuevo Módulo desde Integración")
                .descripcion("Descripción del nuevo módulo")
                .orden(5)
                .totalSecciones(0)
                .tipoEstructura("simple")
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(6))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/modulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoModulo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre", is("Nuevo Módulo desde Integración")))
                .andExpect(jsonPath("$.orden", is(5)))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "tutor")
    void shouldUpdateExistingModulo() throws Exception {
        // Arrange
        Modulo moduloExistente = crearModuloDePrueba();
        moduloExistente.setNombre("Módulo Actualizado");
        moduloExistente.setDescripcion("Descripción actualizada");

        // Act & Assert
        mockMvc.perform(put("/api/modulos/{id}", moduloExistente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moduloExistente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Módulo Actualizado")))
                .andExpect(jsonPath("$.descripcion", is("Descripción actualizada")));
    }

    @Test
    @WithMockUser(roles = "admin")
    void shouldDeleteModuloWhenExists() throws Exception {
        // Arrange
        Modulo modulo = crearModuloDePrueba();
        Long moduloId = modulo.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/modulos/{id}", moduloId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que fue eliminado
        mockMvc.perform(get("/api/modulos/{id}", moduloId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "admin")
    void shouldReturnNotFoundWhenDeletingNonExistentModulo() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/modulos/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("ModuloNotFound")));
    }
}