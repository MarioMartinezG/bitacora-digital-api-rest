package com.diginexa.bitacora.integration;

import com.diginexa.bitacora.BitacoraDigitalApplication;
import com.diginexa.bitacora.entities.Modulo;
import com.diginexa.bitacora.entities.Seccion;
import com.diginexa.bitacora.repositories.ModuloRepository;
import com.diginexa.bitacora.repositories.SeccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BitacoraDigitalApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SeccionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModuloRepository moduloRepository;

    @Autowired
    private SeccionRepository seccionRepository;

    private Modulo modulo;
    private Seccion seccion;

    @BeforeEach
    void setUp() {
        seccionRepository.deleteAllInBatch();
        moduloRepository.deleteAllInBatch();

        // Crear módulo de prueba
        modulo = Modulo.builder()
                .nombre("Módulo para Secciones")
                .descripcion("Módulo para pruebas de secciones")
                .orden(1)
                .totalSecciones(2)
                .tipoEstructura("flexible")
                .build();
        modulo = moduloRepository.save(modulo);

        // Crear sección de prueba
        Map<String, Object> configuracion = new HashMap<>();
        configuracion.put("titulo", "Sección de prueba");

        seccion = Seccion.builder()
                .modulo(modulo)
                .nombre("Sección de Prueba")
                .tipoSeccion("formulario")
                .orden(1)
                .tieneEstado(true)
                .esObligatorio(true)
                .configuracion(configuracion)
                .build();
        seccion = seccionRepository.save(seccion);
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void shouldReturnSeccionesByModuloId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/secciones/modulo/{moduloId}", modulo.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Sección de Prueba")))
                .andExpect(jsonPath("$[0].tipoSeccion", is("formulario")))
                .andExpect(jsonPath("$[0].orden", is(1)));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void shouldReturnEmptyListWhenModuloHasNoSecciones() throws Exception {
        // Arrange
        Modulo moduloSinSecciones = Modulo.builder()
                .nombre("Módulo Sin Secciones")
                .descripcion("Módulo sin secciones")
                .orden(2)
                .build();
        moduloSinSecciones = moduloRepository.save(moduloSinSecciones);

        // Act & Assert
        mockMvc.perform(get("/api/secciones/modulo/{moduloId}", moduloSinSecciones.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void shouldReturnNotFoundWhenModuloNotExists() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/secciones/modulo/{moduloId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("ModuloNotFound")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void shouldReturnSeccionWithCamposById() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/secciones/{seccionId}", seccion.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Sección de Prueba")))
                .andExpect(jsonPath("$.tipoSeccion", is("formulario")))
                .andExpect(jsonPath("$.campos", hasSize(0))); // No hay campos en esta sección
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void shouldReturnNotFoundWhenSeccionNotExists() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/secciones/{seccionId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("SeccionNotFound")));
    }

    @Test
    void shouldReturnUnauthorizedForSeccionesWithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/secciones/modulo/{moduloId}", modulo.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}