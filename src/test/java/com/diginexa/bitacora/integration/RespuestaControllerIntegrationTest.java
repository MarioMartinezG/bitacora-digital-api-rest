package com.diginexa.bitacora.integration;

import com.diginexa.bitacora.BitacoraDigitalApplication;
import com.diginexa.bitacora.dtos.RespuestaRequest;
import com.diginexa.bitacora.entities.*;
import com.diginexa.bitacora.repositories.*;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BitacoraDigitalApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RespuestaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ModuloRepository moduloRepository;

    @Autowired
    private SeccionRepository seccionRepository;

    @Autowired
    private CampoSeccionRepository campoSeccionRepository;

    @Autowired
    private RespuestaRepository respuestaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Modulo modulo;
    private Seccion seccion;
    private CampoSeccion campo;
    private Usuario usuario; // Cambiar a objeto completo

    @BeforeEach
    void setUp() {
        // Limpiar en orden inverso a las dependencias
        respuestaRepository.deleteAllInBatch(); // Usar deleteAllInBatch
        campoSeccionRepository.deleteAllInBatch();
        seccionRepository.deleteAllInBatch();
        moduloRepository.deleteAllInBatch();
        usuarioRepository.deleteAllInBatch();

        // Crear y guardar rol primero
        Rol rol = Rol.builder()
                .nombre("pruebas")
                .build();
        rol = rolRepository.save(rol);

        // Crear usuario con ID generado automáticamente
        usuario = Usuario.builder()
                .nombre("Usuario prueba")
                .correo("usuario.prueba@test.com") // Agregar correo único
                .rol(rol)
                .contrasena("Prueba123")
                .build();
        usuario = usuarioRepository.save(usuario); // ID se genera automáticamente

        // Crear estructura de prueba
        modulo = Modulo.builder()
                .nombre("Módulo para Respuestas")
                .descripcion("Módulo para pruebas de respuestas")
                .orden(1)
                .build();
        modulo = moduloRepository.save(modulo);

        seccion = Seccion.builder()
                .modulo(modulo)
                .nombre("Sección para Respuestas")
                .tipoSeccion("formulario")
                .orden(1)
                .build();
        seccion = seccionRepository.save(seccion);

        campo = CampoSeccion.builder()
                .seccion(seccion)
                .label("Campo de prueba")
                .tipoCampo("texto")
                .esRequerido(true)
                .orden(1)
                .build();
        campo = campoSeccionRepository.save(campo);
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void shouldSaveRespuestasEnLote() throws Exception {
        // Arrange - usar usuario.getId() en lugar de usuarioId fijo
        RespuestaRequest respuestaRequest = RespuestaRequest.builder()
                .usuarioId(usuario.getId()) // ← ID generado automáticamente
                .seccionId(Long.valueOf(seccion.getId()))
                .campoId(Long.valueOf(campo.getId()))
                .respuestaTexto("Respuesta de prueba")
                .estadoAvance("completado")
                .build();

        List<RespuestaRequest> requests = Arrays.asList(respuestaRequest);

        // Act & Assert
        mockMvc.perform(post("/api/respuestas/lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].respuestaTexto", is("Respuesta de prueba")))
                .andExpect(jsonPath("$[0].estadoAvance", is("completado")));
    }

    // Actualizar todos los otros tests para usar usuario.getId()
    @Test
    @WithMockUser(roles = "estudiante")
    void shouldGetRespuestasByUsuarioAndModulo() throws Exception {
        // Arrange
        Respuesta respuesta = Respuesta.builder()
                .usuarioId(usuario.getId()) // ← Usar ID generado
                .seccion(seccion)
                .campo(campo)
                .respuestaTexto("Respuesta existente")
                .estadoAvance("completado")
                .build();
        respuestaRepository.save(respuesta);

        // Act & Assert
        mockMvc.perform(get("/api/respuestas/usuario/{usuarioId}/modulo/{moduloId}",
                        usuario.getId(), modulo.getId()) // ← Usar ID generado
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].respuestaTexto", is("Respuesta existente")))
                .andExpect(jsonPath("$[0].estadoAvance", is("completado")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void shouldReturnEmptyListWhenNoRespuestasForUsuarioAndModulo() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/respuestas/usuario/{usuarioId}/modulo/{moduloId}",
                        999L, modulo.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void shouldGetRespuestasByUsuarioAndSeccion() throws Exception {
        // Arrange - Crear una respuesta primero
        Respuesta respuesta = Respuesta.builder()
                .usuarioId(usuario.getId())
                .seccion(seccion)
                .campo(campo)
                .respuestaTexto("Respuesta por sección")
                .estadoAvance("en_desarrollo")
                .build();
        respuestaRepository.save(respuesta);

        // Act & Assert
        mockMvc.perform(get("/api/respuestas/usuario/{usuarioId}/seccion/{seccionId}",
                        usuario.getId(), seccion.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].respuestaTexto", is("Respuesta por sección")))
                .andExpect(jsonPath("$[0].estadoAvance", is("en_desarrollo")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void shouldDeleteRespuesta() throws Exception {
        // Arrange - Crear una respuesta primero
        Respuesta respuesta = Respuesta.builder()
                .usuarioId(usuario.getId())
                .seccion(seccion)
                .campo(campo)
                .respuestaTexto("Respuesta a eliminar")
                .estadoAvance("completado")
                .build();
        respuesta = respuestaRepository.save(respuesta);

        // Act & Assert
        mockMvc.perform(delete("/api/respuestas/usuario/{usuarioId}/campo/{campoId}",
                        usuario.getId(), campo.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que fue eliminada
        mockMvc.perform(get("/api/respuestas/usuario/{usuarioId}/modulo/{moduloId}",
                        usuario.getId(), modulo.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnUnauthorizedForRespuestasWithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/respuestas/usuario/{usuarioId}/modulo/{moduloId}",
                        1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}