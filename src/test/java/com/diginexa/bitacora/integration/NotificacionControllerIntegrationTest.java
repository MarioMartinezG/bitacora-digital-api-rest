package com.diginexa.bitacora.integration;

import com.diginexa.bitacora.BitacoraDigitalApplication;
import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import com.diginexa.bitacora.entities.Notificacion;
import com.diginexa.bitacora.entities.Rol;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.repositories.NotificacionRepository;
import com.diginexa.bitacora.repositories.RolRepository;
import com.diginexa.bitacora.repositories.UsuarioRepository;
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

import java.util.HashMap;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BitacoraDigitalApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificacionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario usuario;
    private Notificacion notificacionCritica;
    private Notificacion notificacionAlerta;
    private Notificacion notificacionLeida;

    @BeforeEach
    void setUp() {
        // Limpiar en orden inverso a las dependencias
        notificacionRepository.deleteAllInBatch();
        usuarioRepository.deleteAllInBatch();

        // Buscar o crear rol
        Rol rolEstudiante = rolRepository.findByNombre("estudiante")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre("estudiante").build()));

        // Crear usuario de prueba
        usuario = Usuario.builder()
                .nombre("Usuario Notificaciones Test")
                .correo("notificaciones.test@bitacora.com")
                .contrasena("password123")
                .rol(rolEstudiante)
                .build();
        usuario = usuarioRepository.save(usuario);

        notificacionCritica = Notificacion.builder()
                .usuarioId(usuario.getId())
                .tipo(TipoNotificacion.VENCIMIENTO_PROXIMO.name())
                .prioridad(PrioridadNotificacion.CRITICO.name())
                .titulo("Plazo vencido - Módulo 1")
                .mensaje("El plazo para completar el módulo 1 ha vencido")
                .leida(false)
                .datosAdicionales(new HashMap<>())
                .build();
        notificacionCritica = notificacionRepository.save(notificacionCritica);

        notificacionAlerta = Notificacion.builder()
                .usuarioId(usuario.getId())
                .tipo(TipoNotificacion.VENCIMIENTO_PROXIMO.name())
                .prioridad(PrioridadNotificacion.ALERTA.name())
                .titulo("3 días restantes - Módulo 2")
                .mensaje("Quedan 3 días para completar el módulo 2")
                .leida(false)
                .datosAdicionales(new HashMap<>())
                .build();
        notificacionAlerta = notificacionRepository.save(notificacionAlerta);

        notificacionLeida = Notificacion.builder()
                .usuarioId(usuario.getId())
                .tipo(TipoNotificacion.SOLICITUD_SESION.name())
                .prioridad(PrioridadNotificacion.INFO.name())
                .titulo("Solicitud de sesión aceptada")
                .mensaje("Tu solicitud de sesión ha sido aceptada")
                .leida(true)
                .datosAdicionales(new HashMap<>())
                .build();
        notificacionLeida = notificacionRepository.save(notificacionLeida);
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerNotificaciones_retornaTodasLasNotificaciones() throws Exception {
        mockMvc.perform(get("/api/notificaciones/usuario/{usuarioId}", usuario.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].titulo", notNullValue()))
                .andExpect(jsonPath("$[0].severity", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerNotificaciones_usuarioSinNotificaciones_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/notificaciones/usuario/{usuarioId}", 9999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerNoLeidas_retornaSoloNoLeidas() throws Exception {
        mockMvc.perform(get("/api/notificaciones/usuario/{usuarioId}/no-leidas", usuario.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].leida", everyItem(is(false))));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerResumen_retornaConteoPorPrioridad() throws Exception {
        mockMvc.perform(get("/api/notificaciones/usuario/{usuarioId}/resumen", usuario.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId", is(usuario.getId())))
                .andExpect(jsonPath("$.totalNoLeidas", is(2)))
                .andExpect(jsonPath("$.totalCriticas", is(1)))
                .andExpect(jsonPath("$.totalAlertas", is(1)))
                .andExpect(jsonPath("$.totalInfo", is(0))); // La única INFO está leída
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerPorTipo_filtraCorrectamente() throws Exception {
        mockMvc.perform(get("/api/notificaciones/usuario/{usuarioId}/tipo/{tipo}",
                        usuario.getId(), TipoNotificacion.VENCIMIENTO_PROXIMO.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].tipo", everyItem(is(TipoNotificacion.VENCIMIENTO_PROXIMO.name()))));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerPorTipo_tipoInexistente_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/notificaciones/usuario/{usuarioId}/tipo/{tipo}",
                        usuario.getId(), "TIPO_NO_EXISTE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerPorId_cuandoExiste_retornaNotificacion() throws Exception {
        mockMvc.perform(get("/api/notificaciones/{id}", notificacionCritica.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notificacionCritica.getId().intValue())))
                .andExpect(jsonPath("$.titulo", is("Plazo vencido - Módulo 1")))
                .andExpect(jsonPath("$.prioridad", is(PrioridadNotificacion.CRITICO.name())))
                .andExpect(jsonPath("$.severity", is("error")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerPorId_cuandoNoExiste_retorna404() throws Exception {
        mockMvc.perform(get("/api/notificaciones/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NotificacionNotFound")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void marcarComoLeida_actualizaNotificacion() throws Exception {
        mockMvc.perform(put("/api/notificaciones/{id}/leer", notificacionCritica.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notificacionCritica.getId().intValue())))
                .andExpect(jsonPath("$.leida", is(true)))
                .andExpect(jsonPath("$.fechaLectura", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void marcarComoLeida_cuandoNoExiste_retorna404() throws Exception {
        mockMvc.perform(put("/api/notificaciones/{id}/leer", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void marcarTodasComoLeidas_marcaTodasLasNotificaciones() throws Exception {
        mockMvc.perform(put("/api/notificaciones/usuario/{usuarioId}/leer-todas", usuario.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verificar que todas están leídas
        mockMvc.perform(get("/api/notificaciones/usuario/{usuarioId}/no-leidas", usuario.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void eliminar_cuandoExiste_eliminaNotificacion() throws Exception {
        mockMvc.perform(delete("/api/notificaciones/{id}", notificacionCritica.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que fue eliminada
        mockMvc.perform(get("/api/notificaciones/{id}", notificacionCritica.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void eliminar_cuandoNoExiste_retorna404() throws Exception {
        mockMvc.perform(delete("/api/notificaciones/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerNotificaciones_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/notificaciones/usuario/{usuarioId}", usuario.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void marcarComoLeida_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(put("/api/notificaciones/{id}/leer", notificacionCritica.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "tutor")
    void obtenerNotificaciones_comoTutor_funcionaCorrectamente() throws Exception {
        // Los tutores también pueden acceder a notificaciones
        mockMvc.perform(get("/api/notificaciones/usuario/{usuarioId}", usuario.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
