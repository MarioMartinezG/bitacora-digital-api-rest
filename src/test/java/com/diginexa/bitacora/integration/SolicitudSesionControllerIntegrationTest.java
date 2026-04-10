package com.diginexa.bitacora.integration;

import com.diginexa.bitacora.BitacoraDigitalApplication;
import com.diginexa.bitacora.constants.EstadoSolicitudSesion;
import com.diginexa.bitacora.dtos.notificacion.CrearSolicitudSesionRequest;
import com.diginexa.bitacora.dtos.notificacion.ResponderSolicitudRequest;
import com.diginexa.bitacora.entities.Rol;
import com.diginexa.bitacora.entities.SolicitudSesion;
import com.diginexa.bitacora.entities.TutorEstudiante;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.repositories.RolRepository;
import com.diginexa.bitacora.repositories.SolicitudSesionRepository;
import com.diginexa.bitacora.repositories.TutorEstudianteRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BitacoraDigitalApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SolicitudSesionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SolicitudSesionRepository solicitudRepository;

    @Autowired
    private TutorEstudianteRepository tutorEstudianteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario tutor;
    private Usuario estudiante;
    private Usuario estudianteSinTutor;
    private TutorEstudiante asignacion;
    private SolicitudSesion solicitudPendiente;
    private SolicitudSesion solicitudAceptada;

    @BeforeEach
    void setUp() {
        // Limpiar en orden inverso a las dependencias
        solicitudRepository.deleteAllInBatch();
        tutorEstudianteRepository.deleteAllInBatch();
        usuarioRepository.deleteAllInBatch();

        // Buscar o crear roles
        Rol rolTutor = rolRepository.findByNombre("tutor")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre("tutor").build()));
        Rol rolEstudiante = rolRepository.findByNombre("estudiante")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre("estudiante").build()));

        // Crear usuarios
        tutor = Usuario.builder()
                .nombre("Tutor Sesiones Test")
                .correo("tutor.sesiones@bitacora.com")
                .contrasena("password123")
                .rol(rolTutor)
                .build();
        tutor = usuarioRepository.save(tutor);

        estudiante = Usuario.builder()
                .nombre("Estudiante Sesiones Test")
                .correo("estudiante.sesiones@bitacora.com")
                .contrasena("password123")
                .rol(rolEstudiante)
                .build();
        estudiante = usuarioRepository.save(estudiante);

        estudianteSinTutor = Usuario.builder()
                .nombre("Estudiante Sin Tutor")
                .correo("estudiante.sintutor@bitacora.com")
                .contrasena("password123")
                .rol(rolEstudiante)
                .build();
        estudianteSinTutor = usuarioRepository.save(estudianteSinTutor);

        // Crear asignación tutor-estudiante
        asignacion = TutorEstudiante.builder()
                .tutorId(tutor.getId())
                .estudianteId(estudiante.getId())
                .activo(true)
                .build();
        asignacion = tutorEstudianteRepository.save(asignacion);

        // Crear solicitudes de prueba
        solicitudPendiente = SolicitudSesion.builder()
                .estudianteId(estudiante.getId())
                .tutorId(tutor.getId())
                .motivo("Necesito ayuda con el módulo 3")
                .estado(EstadoSolicitudSesion.PENDIENTE.name())
                .build();
        solicitudPendiente = solicitudRepository.save(solicitudPendiente);

        solicitudAceptada = SolicitudSesion.builder()
                .estudianteId(estudiante.getId())
                .tutorId(tutor.getId())
                .motivo("Revisión de avance general")
                .estado(EstadoSolicitudSesion.ACEPTADA.name())
                .notasTutor("Agendada para el viernes 10am")
                .build();
        solicitudAceptada = solicitudRepository.save(solicitudAceptada);
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void crearSolicitud_conDatosValidos_retorna201() throws Exception {
        // Primero eliminar la solicitud pendiente existente para evitar conflicto
        solicitudRepository.delete(solicitudPendiente);

        CrearSolicitudSesionRequest request = CrearSolicitudSesionRequest.builder()
                .estudianteId(estudiante.getId())
                .motivo("Nueva solicitud de ayuda con el módulo 4")
                .build();

        mockMvc.perform(post("/api/solicitudes-sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.estudianteId", is(estudiante.getId())))
                .andExpect(jsonPath("$.tutorId", is(tutor.getId())))
                .andExpect(jsonPath("$.motivo", is("Nueva solicitud de ayuda con el módulo 4")))
                .andExpect(jsonPath("$.estado", is(EstadoSolicitudSesion.PENDIENTE.name())))
                .andExpect(jsonPath("$.nombreEstudiante", is("Estudiante Sesiones Test")))
                .andExpect(jsonPath("$.nombreTutor", is("Tutor Sesiones Test")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void crearSolicitud_sinTutorAsignado_retorna404() throws Exception {
        CrearSolicitudSesionRequest request = CrearSolicitudSesionRequest.builder()
                .estudianteId(estudianteSinTutor.getId())
                .motivo("Quiero solicitar una sesión")
                .build();

        mockMvc.perform(post("/api/solicitudes-sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("TutorNoAsignado")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void crearSolicitud_conSolicitudPendienteExistente_retorna409() throws Exception {
        // Ya existe una solicitud pendiente creada en setUp
        CrearSolicitudSesionRequest request = CrearSolicitudSesionRequest.builder()
                .estudianteId(estudiante.getId())
                .motivo("Otra solicitud")
                .build();

        mockMvc.perform(post("/api/solicitudes-sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void crearSolicitud_estudianteNoExiste_retorna404() throws Exception {
        CrearSolicitudSesionRequest request = CrearSolicitudSesionRequest.builder()
                .estudianteId(99999)
                .motivo("Motivo de prueba")
                .build();

        mockMvc.perform(post("/api/solicitudes-sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void crearSolicitud_sinMotivo_retorna400() throws Exception {
        CrearSolicitudSesionRequest request = CrearSolicitudSesionRequest.builder()
                .estudianteId(estudiante.getId())
                .motivo("")
                .build();

        mockMvc.perform(post("/api/solicitudes-sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerPorEstudiante_conSolicitudes_retornaLista() throws Exception {
        mockMvc.perform(get("/api/solicitudes-sesion/estudiante/{estudianteId}", estudiante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].estudianteId", everyItem(is(estudiante.getId()))));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerPorEstudiante_sinSolicitudes_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/solicitudes-sesion/estudiante/{estudianteId}", estudianteSinTutor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "tutor")
    void obtenerPorTutor_conSolicitudes_retornaLista() throws Exception {
        mockMvc.perform(get("/api/solicitudes-sesion/tutor/{tutorId}", tutor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].tutorId", everyItem(is(tutor.getId()))));
    }

    @Test
    @WithMockUser(roles = "tutor")
    void obtenerPendientesPorTutor_filtraSoloPendientes() throws Exception {
        mockMvc.perform(get("/api/solicitudes-sesion/tutor/{tutorId}/pendientes", tutor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estado", is(EstadoSolicitudSesion.PENDIENTE.name())))
                .andExpect(jsonPath("$[0].motivo", is("Necesito ayuda con el módulo 3")));
    }

    @Test
    @WithMockUser(roles = "tutor")
    void obtenerPendientesPorTutor_sinPendientes_retornaListaVacia() throws Exception {
        // Eliminar la solicitud pendiente
        solicitudRepository.delete(solicitudPendiente);

        mockMvc.perform(get("/api/solicitudes-sesion/tutor/{tutorId}/pendientes", tutor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerPorId_cuandoExiste_retornaSolicitud() throws Exception {
        mockMvc.perform(get("/api/solicitudes-sesion/{id}", solicitudPendiente.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(solicitudPendiente.getId().intValue())))
                .andExpect(jsonPath("$.motivo", is("Necesito ayuda con el módulo 3")))
                .andExpect(jsonPath("$.estado", is(EstadoSolicitudSesion.PENDIENTE.name())))
                .andExpect(jsonPath("$.nombreEstudiante", is("Estudiante Sesiones Test")))
                .andExpect(jsonPath("$.nombreTutor", is("Tutor Sesiones Test")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerPorId_cuandoNoExiste_retorna404() throws Exception {
        mockMvc.perform(get("/api/solicitudes-sesion/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("SolicitudSesionNotFound")));
    }

    @Test
    @WithMockUser(roles = "tutor")
    void responderSolicitud_aceptar_actualizaEstado() throws Exception {
        ResponderSolicitudRequest request = ResponderSolicitudRequest.builder()
                .estado(EstadoSolicitudSesion.ACEPTADA.name())
                .notasTutor("Te espero el lunes a las 3pm en mi oficina")
                .build();

        mockMvc.perform(put("/api/solicitudes-sesion/{id}/responder", solicitudPendiente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(solicitudPendiente.getId().intValue())))
                .andExpect(jsonPath("$.estado", is(EstadoSolicitudSesion.ACEPTADA.name())))
                .andExpect(jsonPath("$.notasTutor", is("Te espero el lunes a las 3pm en mi oficina")))
                .andExpect(jsonPath("$.fechaRespuesta", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "tutor")
    void responderSolicitud_rechazar_actualizaEstado() throws Exception {
        ResponderSolicitudRequest request = ResponderSolicitudRequest.builder()
                .estado(EstadoSolicitudSesion.RECHAZADA.name())
                .notasTutor("No tengo disponibilidad esta semana, intenta de nuevo la próxima")
                .build();

        mockMvc.perform(put("/api/solicitudes-sesion/{id}/responder", solicitudPendiente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is(EstadoSolicitudSesion.RECHAZADA.name())))
                .andExpect(jsonPath("$.fechaRespuesta", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "tutor")
    void responderSolicitud_completar_actualizaEstado() throws Exception {
        ResponderSolicitudRequest request = ResponderSolicitudRequest.builder()
                .estado(EstadoSolicitudSesion.COMPLETADA.name())
                .notasTutor("Sesión realizada exitosamente")
                .build();

        mockMvc.perform(put("/api/solicitudes-sesion/{id}/responder", solicitudAceptada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is(EstadoSolicitudSesion.COMPLETADA.name())));
    }

    @Test
    @WithMockUser(roles = "tutor")
    void responderSolicitud_estadoInvalido_retorna400() throws Exception {
        ResponderSolicitudRequest request = ResponderSolicitudRequest.builder()
                .estado("ESTADO_INVALIDO")
                .build();

        mockMvc.perform(put("/api/solicitudes-sesion/{id}/responder", solicitudPendiente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "tutor")
    void responderSolicitud_sinEstado_retorna400() throws Exception {
        ResponderSolicitudRequest request = ResponderSolicitudRequest.builder()
                .estado("")
                .build();

        mockMvc.perform(put("/api/solicitudes-sesion/{id}/responder", solicitudPendiente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "tutor")
    void responderSolicitud_solicitudNoExiste_retorna404() throws Exception {
        ResponderSolicitudRequest request = ResponderSolicitudRequest.builder()
                .estado(EstadoSolicitudSesion.ACEPTADA.name())
                .build();

        mockMvc.perform(put("/api/solicitudes-sesion/{id}/responder", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void cancelarSolicitud_cuandoPendienteYEsEstudiante_retorna204() throws Exception {
        mockMvc.perform(delete("/api/solicitudes-sesion/{id}/estudiante/{estudianteId}",
                        solicitudPendiente.getId(), estudiante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que fue eliminada
        mockMvc.perform(get("/api/solicitudes-sesion/{id}", solicitudPendiente.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void cancelarSolicitud_cuandoNoEsEstudiante_retorna403() throws Exception {
        mockMvc.perform(delete("/api/solicitudes-sesion/{id}/estudiante/{estudianteId}",
                        solicitudPendiente.getId(), estudianteSinTutor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void cancelarSolicitud_cuandoNoEsPendiente_retorna409() throws Exception {
        // solicitudAceptada ya está ACEPTADA
        mockMvc.perform(delete("/api/solicitudes-sesion/{id}/estudiante/{estudianteId}",
                        solicitudAceptada.getId(), estudiante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void cancelarSolicitud_solicitudNoExiste_retorna404() throws Exception {
        mockMvc.perform(delete("/api/solicitudes-sesion/{id}/estudiante/{estudianteId}",
                        99999L, estudiante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearSolicitud_sinAutenticacion_retorna401() throws Exception {
        CrearSolicitudSesionRequest request = CrearSolicitudSesionRequest.builder()
                .estudianteId(estudiante.getId())
                .motivo("Motivo de prueba")
                .build();

        mockMvc.perform(post("/api/solicitudes-sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenerPorEstudiante_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/solicitudes-sesion/estudiante/{estudianteId}", estudiante.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void responderSolicitud_sinAutenticacion_retorna401() throws Exception {
        ResponderSolicitudRequest request = ResponderSolicitudRequest.builder()
                .estado(EstadoSolicitudSesion.ACEPTADA.name())
                .build();

        mockMvc.perform(put("/api/solicitudes-sesion/{id}/responder", solicitudPendiente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
