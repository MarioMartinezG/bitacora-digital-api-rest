package com.diginexa.bitacora.integration;

import com.diginexa.bitacora.BitacoraDigitalApplication;
import com.diginexa.bitacora.dtos.notificacion.AsignarTutorRequest;
import com.diginexa.bitacora.entities.Rol;
import com.diginexa.bitacora.entities.TutorEstudiante;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.repositories.RolRepository;
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

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BitacoraDigitalApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TutorEstudianteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TutorEstudianteRepository tutorEstudianteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario tutor;
    private Usuario estudiante1;
    private Usuario estudiante2;
    private Usuario estudianteSinTutor;
    private TutorEstudiante asignacion;
    private Rol rolTutor;
    private Rol rolEstudiante;

    @BeforeEach
    void setUp() {
        // Limpiar en orden inverso a las dependencias
        tutorEstudianteRepository.deleteAllInBatch();
        usuarioRepository.deleteAllInBatch();

        // Buscar o crear roles
        rolTutor = rolRepository.findByNombre("tutor")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre("tutor").build()));
        rolEstudiante = rolRepository.findByNombre("estudiante")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre("estudiante").build()));

        // Crear usuarios de prueba
        tutor = Usuario.builder()
                .nombre("Tutor de Prueba")
                .correo("tutor.test@bitacora.com")
                .contrasena("password123")
                .roles(new HashSet<>(Set.of(rolTutor)))
                .build();
        tutor = usuarioRepository.save(tutor);

        estudiante1 = Usuario.builder()
                .nombre("Estudiante Uno")
                .correo("estudiante1.test@bitacora.com")
                .contrasena("password123")
                .roles(new HashSet<>(Set.of(rolEstudiante)))
                .build();
        estudiante1 = usuarioRepository.save(estudiante1);

        estudiante2 = Usuario.builder()
                .nombre("Estudiante Dos")
                .correo("estudiante2.test@bitacora.com")
                .contrasena("password123")
                .roles(new HashSet<>(Set.of(rolEstudiante)))
                .build();
        estudiante2 = usuarioRepository.save(estudiante2);

        estudianteSinTutor = Usuario.builder()
                .nombre("Estudiante Sin Tutor")
                .correo("estudiante3.test@bitacora.com")
                .contrasena("password123")
                .roles(new HashSet<>(Set.of(rolEstudiante)))
                .build();
        estudianteSinTutor = usuarioRepository.save(estudianteSinTutor);

        // Crear asignación existente
        asignacion = TutorEstudiante.builder()
                .tutorId(tutor.getId())
                .estudianteId(estudiante1.getId())
                .activo(true)
                .build();
        asignacion = tutorEstudianteRepository.save(asignacion);
    }

    @Test
    @WithMockUser(roles = "admin")
    void asignarTutor_nuevaAsignacion_retorna201() throws Exception {
        AsignarTutorRequest request = new AsignarTutorRequest();
        request.setTutorId(tutor.getId());
        request.setEstudianteId(estudiante2.getId());

        mockMvc.perform(post("/api/tutor-estudiante/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.tutorId", is(tutor.getId())))
                .andExpect(jsonPath("$.estudianteId", is(estudiante2.getId())))
                .andExpect(jsonPath("$.nombreTutor", is("Tutor de Prueba")))
                .andExpect(jsonPath("$.nombreEstudiante", is("Estudiante Dos")))
                .andExpect(jsonPath("$.activo", is(true)));
    }

    @Test
    @WithMockUser(roles = "admin")
    void asignarTutor_reemplazaTutorExistente_desactivaAnteriorYCreaNueva() throws Exception {
        // Crear nuevo tutor
        Usuario nuevoTutor = Usuario.builder()
                .nombre("Nuevo Tutor")
                .correo("nuevo.tutor@bitacora.com")
                .contrasena("password123")
                .roles(new HashSet<>(Set.of(rolTutor)))
                .build();
        nuevoTutor = usuarioRepository.save(nuevoTutor);

        AsignarTutorRequest request = new AsignarTutorRequest();
        request.setTutorId(nuevoTutor.getId());
        request.setEstudianteId(estudiante1.getId()); // Ya tiene tutor asignado

        mockMvc.perform(post("/api/tutor-estudiante/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tutorId", is(nuevoTutor.getId())))
                .andExpect(jsonPath("$.nombreTutor", is("Nuevo Tutor")));
    }

    @Test
    @WithMockUser(roles = "admin")
    void asignarTutor_tutorNoExiste_retorna404() throws Exception {
        AsignarTutorRequest request = new AsignarTutorRequest();
        request.setTutorId(99999);
        request.setEstudianteId(estudiante2.getId());

        mockMvc.perform(post("/api/tutor-estudiante/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "admin")
    void asignarTutor_estudianteNoExiste_retorna404() throws Exception {
        AsignarTutorRequest request = new AsignarTutorRequest();
        request.setTutorId(tutor.getId());
        request.setEstudianteId(99999);

        mockMvc.perform(post("/api/tutor-estudiante/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "tutor")
    void obtenerEstudiantesPorTutor_conEstudiantes_retornaLista() throws Exception {
        mockMvc.perform(get("/api/tutor-estudiante/tutor/{tutorId}/estudiantes", tutor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estudianteId", is(estudiante1.getId())))
                .andExpect(jsonPath("$[0].nombreEstudiante", is("Estudiante Uno")));
    }

    @Test
    @WithMockUser(roles = "tutor")
    void obtenerEstudiantesPorTutor_sinEstudiantes_retornaListaVacia() throws Exception {
        // Crear tutor sin estudiantes
        Usuario tutorSinEstudiantes = Usuario.builder()
                .nombre("Tutor Sin Estudiantes")
                .correo("tutor.solo@bitacora.com")
                .contrasena("password123")
                .roles(new HashSet<>(Set.of(rolTutor)))
                .build();
        tutorSinEstudiantes = usuarioRepository.save(tutorSinEstudiantes);

        mockMvc.perform(get("/api/tutor-estudiante/tutor/{tutorId}/estudiantes", tutorSinEstudiantes.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerTutorPorEstudiante_conTutorAsignado_retornaDTO() throws Exception {
        mockMvc.perform(get("/api/tutor-estudiante/estudiante/{estudianteId}/tutor", estudiante1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tutorId", is(tutor.getId())))
                .andExpect(jsonPath("$.nombreTutor", is("Tutor de Prueba")))
                .andExpect(jsonPath("$.correoTutor", is("tutor.test@bitacora.com")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void obtenerTutorPorEstudiante_sinTutorAsignado_retorna404() throws Exception {
        mockMvc.perform(get("/api/tutor-estudiante/estudiante/{estudianteId}/tutor", estudianteSinTutor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("TutorNoAsignado")));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void tieneTutorAsignado_cuandoTiene_retornaTrue() throws Exception {
        mockMvc.perform(get("/api/tutor-estudiante/estudiante/{estudianteId}/tiene-tutor", estudiante1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "estudiante")
    void tieneTutorAsignado_cuandoNoTiene_retornaFalse() throws Exception {
        mockMvc.perform(get("/api/tutor-estudiante/estudiante/{estudianteId}/tiene-tutor", estudianteSinTutor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @WithMockUser(roles = "admin")
    void desactivarAsignacion_cuandoExiste_retorna204() throws Exception {
        mockMvc.perform(delete("/api/tutor-estudiante/{id}", asignacion.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que el estudiante ya no tiene tutor activo
        mockMvc.perform(get("/api/tutor-estudiante/estudiante/{estudianteId}/tiene-tutor", estudiante1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @WithMockUser(roles = "admin")
    void desactivarAsignacion_cuandoNoExiste_retorna404() throws Exception {
        mockMvc.perform(delete("/api/tutor-estudiante/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void asignarTutor_sinAutenticacion_retorna401() throws Exception {
        AsignarTutorRequest request = new AsignarTutorRequest();
        request.setTutorId(tutor.getId());
        request.setEstudianteId(estudiante2.getId());

        mockMvc.perform(post("/api/tutor-estudiante/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenerTutorPorEstudiante_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/tutor-estudiante/estudiante/{estudianteId}/tutor", estudiante1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
