package com.diginexa.bitacora.integration;

import com.diginexa.bitacora.BitacoraDigitalApplication;
import com.diginexa.bitacora.dtos.notificacion.CalendarioModuloDTO;
import com.diginexa.bitacora.entities.CalendarioModulo;
import com.diginexa.bitacora.repositories.CalendarioModuloRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BitacoraDigitalApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CalendarioModuloControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CalendarioModuloRepository calendarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private CalendarioModulo calendarioProximo;
    private CalendarioModulo calendarioNormal;

    @BeforeEach
    void setUp() {
        calendarioRepository.deleteAllInBatch();

        calendarioProximo = CalendarioModulo.builder()
                .seccionCodigo("SEC-TEST-001")
                .nombreModulo("Módulo 1: Caracterización")
                .fechaLimite(LocalDate.now().plusDays(2))
                .descripcion("Fecha límite para completar el módulo 1")
                .activo(true)
                .build();
        calendarioProximo = calendarioRepository.save(calendarioProximo);

        calendarioNormal = CalendarioModulo.builder()
                .seccionCodigo("SEC-TEST-002")
                .nombreModulo("Módulo 2: Factores situacionales")
                .fechaLimite(LocalDate.now().plusDays(15))
                .descripcion("Fecha límite para completar el módulo 2")
                .activo(true)
                .build();
        calendarioNormal = calendarioRepository.save(calendarioNormal);
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void listarTodos_retornaCalendariosActivos() throws Exception {
        mockMvc.perform(get("/api/calendario")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].seccionCodigo", is("SEC-TEST-001")))
                .andExpect(jsonPath("$[0].estadoVencimiento", is("PROXIMO")))
                .andExpect(jsonPath("$[1].seccionCodigo", is("SEC-TEST-002")))
                .andExpect(jsonPath("$[1].estadoVencimiento", is("NORMAL")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerPorSeccion_cuandoExiste_retorna200() throws Exception {
        mockMvc.perform(get("/api/calendario/seccion/{seccionCodigo}", "SEC-TEST-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seccionCodigo", is("SEC-TEST-001")))
                .andExpect(jsonPath("$.nombreModulo", is("Módulo 1: Caracterización")))
                .andExpect(jsonPath("$.diasRestantes", is(2)));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerPorSeccion_cuandoNoExiste_retorna404() throws Exception {
        mockMvc.perform(get("/api/calendario/seccion/{seccionCodigo}", "SEC-NO-EXISTE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("CalendarioModuloNotFound")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerPorId_cuandoExiste_retorna200() throws Exception {
        mockMvc.perform(get("/api/calendario/{id}", calendarioProximo.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(calendarioProximo.getId().intValue())))
                .andExpect(jsonPath("$.nombreModulo", is("Módulo 1: Caracterización")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerPorId_cuandoNoExiste_retorna404() throws Exception {
        mockMvc.perform(get("/api/calendario/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("CalendarioModuloNotFound")));
    }

    @Test
    @WithMockUser(roles = "tutor")
    void crear_conDatosValidos_retorna201() throws Exception {
        CalendarioModuloDTO nuevoCalendario = CalendarioModuloDTO.builder()
                .seccionCodigo("SEC-NEW-001")
                .nombreModulo("Nuevo Módulo de Prueba")
                .fechaLimite(LocalDate.now().plusDays(30))
                .descripcion("Descripción del nuevo módulo")
                .activo(true)
                .build();

        mockMvc.perform(post("/api/calendario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoCalendario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.seccionCodigo", is("SEC-NEW-001")))
                .andExpect(jsonPath("$.nombreModulo", is("Nuevo Módulo de Prueba")))
                .andExpect(jsonPath("$.estadoVencimiento", is("NORMAL")));
    }

    @Test
    @WithMockUser(roles = "tutor")
    void crear_conSeccionDuplicada_retorna400() throws Exception {
        CalendarioModuloDTO calendarioDuplicado = CalendarioModuloDTO.builder()
                .seccionCodigo("SEC-TEST-001") // Ya existe
                .nombreModulo("Módulo Duplicado")
                .fechaLimite(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(post("/api/calendario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(calendarioDuplicado)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "tutor")
    void actualizar_cuandoExiste_retorna200() throws Exception {
        CalendarioModuloDTO actualizacion = CalendarioModuloDTO.builder()
                .nombreModulo("Módulo Actualizado")
                .descripcion("Nueva descripción actualizada")
                .seccionCodigo("SEC-TEST-001")
                .fechaLimite(LocalDate.now().plusDays(30))
                .build();

        mockMvc.perform(put("/api/calendario/{id}", calendarioProximo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreModulo", is("Módulo Actualizado")))
                .andExpect(jsonPath("$.descripcion", is("Nueva descripción actualizada")))
                .andExpect(jsonPath("$.seccionCodigo", is("SEC-TEST-001"))); // No cambia
    }

    @Test
    @WithMockUser(roles = "tutor")
    void actualizar_cuandoNoExiste_retorna404() throws Exception {
        CalendarioModuloDTO actualizacion = CalendarioModuloDTO.builder()
                .nombreModulo("No importa")
                .seccionCodigo("SEC-TEST-NVM")
                .fechaLimite(LocalDate.now().plusDays(30))
                .build();

        mockMvc.perform(put("/api/calendario/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "admin")
    void eliminar_cuandoExiste_retorna204() throws Exception {
        mockMvc.perform(delete("/api/calendario/{id}", calendarioProximo.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que fue eliminado
        mockMvc.perform(get("/api/calendario/{id}", calendarioProximo.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "admin")
    void eliminar_cuandoNoExiste_retorna404() throws Exception {
        mockMvc.perform(delete("/api/calendario/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerProximos_retornaCalendariosEnRango() throws Exception {
        mockMvc.perform(get("/api/calendario/proximos")
                        .param("dias", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Solo calendarioProximo está dentro de 7 días
                .andExpect(jsonPath("$[0].seccionCodigo", is("SEC-TEST-001")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerProximos_sinParametro_usaDefault7Dias() throws Exception {
        mockMvc.perform(get("/api/calendario/proximos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void listarTodos_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/calendario")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crear_sinAutenticacion_retorna401() throws Exception {
        CalendarioModuloDTO nuevoCalendario = CalendarioModuloDTO.builder()
                .seccionCodigo("SEC-UNAUTH")
                .nombreModulo("No autorizado")
                .fechaLimite(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(post("/api/calendario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoCalendario)))
                .andExpect(status().isUnauthorized());
    }
}
