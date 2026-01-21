package com.diginexa.bitacora.unit;

import com.diginexa.bitacora.constants.EstadoSolicitudSesion;
import com.diginexa.bitacora.dtos.notificacion.CrearSolicitudSesionRequest;
import com.diginexa.bitacora.dtos.notificacion.ResponderSolicitudRequest;
import com.diginexa.bitacora.dtos.notificacion.SolicitudSesionDTO;
import com.diginexa.bitacora.entities.Rol;
import com.diginexa.bitacora.entities.SolicitudSesion;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.exceptions.domain.SolicitudSesionNotFoundException;
import com.diginexa.bitacora.exceptions.domain.TutorNoAsignadoException;
import com.diginexa.bitacora.exceptions.validation.ForbiddenException;
import com.diginexa.bitacora.repositories.SolicitudSesionRepository;
import com.diginexa.bitacora.repositories.TutorEstudianteRepository;
import com.diginexa.bitacora.repositories.UsuarioRepository;
import com.diginexa.bitacora.services.NotificacionEventPublisher;
import com.diginexa.bitacora.services.SolicitudSesionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudSesionServiceTest {

    @Mock
    private SolicitudSesionRepository solicitudRepository;

    @Mock
    private TutorEstudianteRepository tutorEstudianteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private NotificacionEventPublisher eventPublisher;

    @InjectMocks
    private SolicitudSesionService solicitudService;

    private Usuario estudiante;
    private Usuario tutor;
    private SolicitudSesion solicitudPendiente;
    private SolicitudSesion solicitudAceptada;
    private Rol rolEstudiante;
    private Rol rolTutor;

    @BeforeEach
    void setUp() {
        rolEstudiante = Rol.builder().id(1L).nombre("estudiante").build();
        rolTutor = Rol.builder().id(2L).nombre("tutor").build();

        estudiante = Usuario.builder()
                .id(1)
                .nombre("María García")
                .correo("maria.garcia@test.com")
                .contrasena("password")
                .rol(rolEstudiante)
                .build();

        tutor = Usuario.builder()
                .id(100)
                .nombre("Juan Pérez")
                .correo("juan.perez@test.com")
                .contrasena("password")
                .rol(rolTutor)
                .build();

        solicitudPendiente = SolicitudSesion.builder()
                .id(1L)
                .estudianteId(estudiante.getId())
                .tutorId(tutor.getId())
                .motivo("Necesito ayuda con el módulo 3")
                .estado(EstadoSolicitudSesion.PENDIENTE.name())
                .fechaSolicitud(LocalDateTime.now().minusDays(1))
                .build();

        solicitudAceptada = SolicitudSesion.builder()
                .id(2L)
                .estudianteId(estudiante.getId())
                .tutorId(tutor.getId())
                .motivo("Revisión de avance")
                .estado(EstadoSolicitudSesion.ACEPTADA.name())
                .fechaSolicitud(LocalDateTime.now().minusDays(5))
                .fechaRespuesta(LocalDateTime.now().minusDays(4))
                .notasTutor("Agendada para el viernes a las 10am")
                .build();
    }

    @Test
    void crearSolicitud_conDatosValidos_creaYNotifica() {
        // Arrange
        CrearSolicitudSesionRequest request = new CrearSolicitudSesionRequest();
        request.setEstudianteId(estudiante.getId());
        request.setMotivo("Necesito ayuda con el módulo 3");

        when(usuarioRepository.findById(estudiante.getId())).thenReturn(Optional.of(estudiante));
        when(tutorEstudianteRepository.findTutorByEstudianteId(estudiante.getId()))
                .thenReturn(Optional.of(tutor));
        when(solicitudRepository.existsByEstudianteIdAndTutorIdAndEstado(
                estudiante.getId(), tutor.getId(), EstadoSolicitudSesion.PENDIENTE.name()))
                .thenReturn(false);
        when(solicitudRepository.save(any(SolicitudSesion.class))).thenAnswer(invocation -> {
            SolicitudSesion s = invocation.getArgument(0);
            s.setId(10L);
            s.setFechaSolicitud(LocalDateTime.now());
            return s;
        });
        doNothing().when(eventPublisher).publicarSolicitudSesion(
                anyInt(), anyLong(), anyInt(), anyString(), anyString());

        // Act
        SolicitudSesionDTO result = solicitudService.crearSolicitud(request);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(estudiante.getId(), result.getEstudianteId());
        assertEquals(tutor.getId(), result.getTutorId());
        assertEquals("María García", result.getNombreEstudiante());
        assertEquals("Juan Pérez", result.getNombreTutor());
        verify(solicitudRepository, times(1)).save(any(SolicitudSesion.class));
        verify(eventPublisher, times(1)).publicarSolicitudSesion(
                eq(tutor.getId()), eq(10L), eq(estudiante.getId()), eq("María García"), anyString());
    }

    @Test
    void crearSolicitud_estudianteNoExiste_lanzaExcepcion() {
        // Arrange
        CrearSolicitudSesionRequest request = new CrearSolicitudSesionRequest();
        request.setEstudianteId(999);
        request.setMotivo("Motivo");

        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> solicitudService.crearSolicitud(request));

        assertTrue(exception.getMessage().contains("Estudiante"));
        verify(solicitudRepository, never()).save(any(SolicitudSesion.class));
    }

    @Test
    void crearSolicitud_sinTutorAsignado_lanzaExcepcion() {
        // Arrange
        CrearSolicitudSesionRequest request = new CrearSolicitudSesionRequest();
        request.setEstudianteId(estudiante.getId());
        request.setMotivo("Motivo");

        when(usuarioRepository.findById(estudiante.getId())).thenReturn(Optional.of(estudiante));
        when(tutorEstudianteRepository.findTutorByEstudianteId(estudiante.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        TutorNoAsignadoException exception = assertThrows(TutorNoAsignadoException.class,
                () -> solicitudService.crearSolicitud(request));

        assertTrue(exception.getMessage().contains(String.valueOf(estudiante.getId())));
        verify(solicitudRepository, never()).save(any(SolicitudSesion.class));
    }

    @Test
    void crearSolicitud_conSolicitudPendienteExistente_lanzaExcepcion() {
        // Arrange
        CrearSolicitudSesionRequest request = new CrearSolicitudSesionRequest();
        request.setEstudianteId(estudiante.getId());
        request.setMotivo("Motivo");

        when(usuarioRepository.findById(estudiante.getId())).thenReturn(Optional.of(estudiante));
        when(tutorEstudianteRepository.findTutorByEstudianteId(estudiante.getId()))
                .thenReturn(Optional.of(tutor));
        when(solicitudRepository.existsByEstudianteIdAndTutorIdAndEstado(
                estudiante.getId(), tutor.getId(), EstadoSolicitudSesion.PENDIENTE.name()))
                .thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> solicitudService.crearSolicitud(request));

        assertTrue(exception.getMessage().contains("pendiente"));
        verify(solicitudRepository, never()).save(any(SolicitudSesion.class));
    }

    @Test
    void obtenerPorEstudiante_conSolicitudes_retornaLista() {
        // Arrange
        List<SolicitudSesion> solicitudes = Arrays.asList(solicitudPendiente, solicitudAceptada);
        when(solicitudRepository.findByEstudianteIdOrderByFechaSolicitudDesc(estudiante.getId()))
                .thenReturn(solicitudes);
        when(usuarioRepository.findById(estudiante.getId())).thenReturn(Optional.of(estudiante));
        when(usuarioRepository.findById(tutor.getId())).thenReturn(Optional.of(tutor));

        // Act
        List<SolicitudSesionDTO> result = solicitudService.obtenerPorEstudiante(estudiante.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(solicitudRepository, times(1)).findByEstudianteIdOrderByFechaSolicitudDesc(estudiante.getId());
    }

    @Test
    void obtenerPorEstudiante_sinSolicitudes_retornaListaVacia() {
        // Arrange
        when(solicitudRepository.findByEstudianteIdOrderByFechaSolicitudDesc(estudiante.getId()))
                .thenReturn(Collections.emptyList());

        // Act
        List<SolicitudSesionDTO> result = solicitudService.obtenerPorEstudiante(estudiante.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void obtenerPorTutor_conSolicitudes_retornaLista() {
        // Arrange
        List<SolicitudSesion> solicitudes = Arrays.asList(solicitudPendiente);
        when(solicitudRepository.findByTutorIdOrderByFechaSolicitudDesc(tutor.getId()))
                .thenReturn(solicitudes);
        when(usuarioRepository.findById(estudiante.getId())).thenReturn(Optional.of(estudiante));
        when(usuarioRepository.findById(tutor.getId())).thenReturn(Optional.of(tutor));

        // Act
        List<SolicitudSesionDTO> result = solicitudService.obtenerPorTutor(tutor.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void obtenerPendientesPorTutor_filtraSoloPendientes() {
        // Arrange
        List<SolicitudSesion> solicitudes = Arrays.asList(solicitudPendiente);
        when(solicitudRepository.findByTutorIdAndEstadoOrderByFechaSolicitudDesc(
                tutor.getId(), EstadoSolicitudSesion.PENDIENTE.name()))
                .thenReturn(solicitudes);
        when(usuarioRepository.findById(estudiante.getId())).thenReturn(Optional.of(estudiante));
        when(usuarioRepository.findById(tutor.getId())).thenReturn(Optional.of(tutor));

        // Act
        List<SolicitudSesionDTO> result = solicitudService.obtenerPendientesPorTutor(tutor.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(EstadoSolicitudSesion.PENDIENTE.name(), result.get(0).getEstado());
    }

    @Test
    void obtenerPorId_cuandoExiste_retornaDTO() {
        // Arrange
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudPendiente));
        when(usuarioRepository.findById(estudiante.getId())).thenReturn(Optional.of(estudiante));
        when(usuarioRepository.findById(tutor.getId())).thenReturn(Optional.of(tutor));

        // Act
        SolicitudSesionDTO result = solicitudService.obtenerPorId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Necesito ayuda con el módulo 3", result.getMotivo());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SolicitudSesionNotFoundException.class,
                () -> solicitudService.obtenerPorId(99L));
    }

    @Test
    void responderSolicitud_aceptar_actualizaEstado() {
        // Arrange
        ResponderSolicitudRequest request = new ResponderSolicitudRequest();
        request.setEstado(EstadoSolicitudSesion.ACEPTADA.name());
        request.setNotasTutor("Nos vemos el viernes a las 10am");

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudPendiente));
        when(solicitudRepository.save(any(SolicitudSesion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.findById(estudiante.getId())).thenReturn(Optional.of(estudiante));
        when(usuarioRepository.findById(tutor.getId())).thenReturn(Optional.of(tutor));

        // Act
        SolicitudSesionDTO result = solicitudService.responderSolicitud(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(EstadoSolicitudSesion.ACEPTADA.name(), result.getEstado());
        assertEquals("Nos vemos el viernes a las 10am", result.getNotasTutor());
        assertNotNull(result.getFechaRespuesta());
    }

    @Test
    void responderSolicitud_rechazar_actualizaEstado() {
        // Arrange
        ResponderSolicitudRequest request = new ResponderSolicitudRequest();
        request.setEstado(EstadoSolicitudSesion.RECHAZADA.name());
        request.setNotasTutor("No tengo disponibilidad esta semana");

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudPendiente));
        when(solicitudRepository.save(any(SolicitudSesion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.findById(estudiante.getId())).thenReturn(Optional.of(estudiante));
        when(usuarioRepository.findById(tutor.getId())).thenReturn(Optional.of(tutor));

        // Act
        SolicitudSesionDTO result = solicitudService.responderSolicitud(1L, request);

        // Assert
        assertEquals(EstadoSolicitudSesion.RECHAZADA.name(), result.getEstado());
    }

    @Test
    void responderSolicitud_completar_actualizaEstado() {
        // Arrange
        ResponderSolicitudRequest request = new ResponderSolicitudRequest();
        request.setEstado(EstadoSolicitudSesion.COMPLETADA.name());

        when(solicitudRepository.findById(2L)).thenReturn(Optional.of(solicitudAceptada));
        when(solicitudRepository.save(any(SolicitudSesion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.findById(estudiante.getId())).thenReturn(Optional.of(estudiante));
        when(usuarioRepository.findById(tutor.getId())).thenReturn(Optional.of(tutor));

        // Act
        SolicitudSesionDTO result = solicitudService.responderSolicitud(2L, request);

        // Assert
        assertEquals(EstadoSolicitudSesion.COMPLETADA.name(), result.getEstado());
    }

    @Test
    void responderSolicitud_estadoInvalido_lanzaExcepcion() {
        // Arrange
        ResponderSolicitudRequest request = new ResponderSolicitudRequest();
        request.setEstado("ESTADO_INVALIDO");

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudPendiente));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> solicitudService.responderSolicitud(1L, request));

        assertTrue(exception.getMessage().contains("no válido"));
        verify(solicitudRepository, never()).save(any(SolicitudSesion.class));
    }

    @Test
    void responderSolicitud_estadoPendiente_lanzaExcepcion() {
        // Arrange
        ResponderSolicitudRequest request = new ResponderSolicitudRequest();
        request.setEstado(EstadoSolicitudSesion.PENDIENTE.name());

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudPendiente));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> solicitudService.responderSolicitud(1L, request));

        assertTrue(exception.getMessage().contains("no válido"));
    }

    @Test
    void responderSolicitud_solicitudNoExiste_lanzaExcepcion() {
        // Arrange
        ResponderSolicitudRequest request = new ResponderSolicitudRequest();
        request.setEstado(EstadoSolicitudSesion.ACEPTADA.name());

        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SolicitudSesionNotFoundException.class,
                () -> solicitudService.responderSolicitud(99L, request));
    }

    @Test
    void cancelarSolicitud_cuandoPendienteYEsEstudiante_elimina() {
        // Arrange
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudPendiente));
        doNothing().when(solicitudRepository).deleteById(1L);

        // Act
        solicitudService.cancelarSolicitud(1L, estudiante.getId());

        // Assert
        verify(solicitudRepository, times(1)).deleteById(1L);
    }

    @Test
    void cancelarSolicitud_cuandoNoEsEstudiante_lanzaExcepcion() {
        // Arrange
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudPendiente));

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> solicitudService.cancelarSolicitud(1L, 999));

        assertTrue(exception.getMessage().contains("permiso"));
        verify(solicitudRepository, never()).deleteById(anyLong());
    }

    @Test
    void cancelarSolicitud_cuandoNoEsPendiente_lanzaExcepcion() {
        // Arrange
        when(solicitudRepository.findById(2L)).thenReturn(Optional.of(solicitudAceptada));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> solicitudService.cancelarSolicitud(2L, estudiante.getId()));

        assertTrue(exception.getMessage().contains("pendientes"));
        verify(solicitudRepository, never()).deleteById(anyLong());
    }

    @Test
    void cancelarSolicitud_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SolicitudSesionNotFoundException.class,
                () -> solicitudService.cancelarSolicitud(99L, estudiante.getId()));
    }
}
