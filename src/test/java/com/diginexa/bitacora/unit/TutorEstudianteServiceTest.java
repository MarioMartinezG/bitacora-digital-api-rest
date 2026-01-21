package com.diginexa.bitacora.unit;

import com.diginexa.bitacora.dtos.notificacion.AsignarTutorRequest;
import com.diginexa.bitacora.dtos.notificacion.TutorEstudianteDTO;
import com.diginexa.bitacora.entities.Rol;
import com.diginexa.bitacora.entities.TutorEstudiante;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.exceptions.domain.TutorNoAsignadoException;
import com.diginexa.bitacora.repositories.TutorEstudianteRepository;
import com.diginexa.bitacora.repositories.UsuarioRepository;
import com.diginexa.bitacora.services.TutorEstudianteService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorEstudianteServiceTest {

    @Mock
    private TutorEstudianteRepository tutorEstudianteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TutorEstudianteService tutorEstudianteService;

    private Usuario tutor;
    private Usuario estudiante1;
    private Usuario estudiante2;
    private TutorEstudiante asignacion1;
    private TutorEstudiante asignacion2;
    private Rol rolTutor;
    private Rol rolEstudiante;

    @BeforeEach
    void setUp() {
        rolTutor = Rol.builder().id(2L).nombre("tutor").build();
        rolEstudiante = Rol.builder().id(1L).nombre("estudiante").build();

        tutor = Usuario.builder()
                .id(100)
                .nombre("Juan Pérez")
                .correo("juan.perez@test.com")
                .contrasena("password")
                .rol(rolTutor)
                .build();

        estudiante1 = Usuario.builder()
                .id(1)
                .nombre("María García")
                .correo("maria.garcia@test.com")
                .contrasena("password")
                .rol(rolEstudiante)
                .build();

        estudiante2 = Usuario.builder()
                .id(2)
                .nombre("Carlos López")
                .correo("carlos.lopez@test.com")
                .contrasena("password")
                .rol(rolEstudiante)
                .build();

        asignacion1 = TutorEstudiante.builder()
                .id(1L)
                .tutorId(tutor.getId())
                .estudianteId(estudiante1.getId())
                .activo(true)
                .fechaAsignacion(LocalDateTime.now().minusDays(30))
                .build();

        asignacion2 = TutorEstudiante.builder()
                .id(2L)
                .tutorId(tutor.getId())
                .estudianteId(estudiante2.getId())
                .activo(true)
                .fechaAsignacion(LocalDateTime.now().minusDays(15))
                .build();
    }

    @Test
    void asignarTutor_nuevaAsignacion_creaYRetornaDTO() {
        // Arrange
        AsignarTutorRequest request = new AsignarTutorRequest();
        request.setTutorId(tutor.getId());
        request.setEstudianteId(estudiante1.getId());

        when(usuarioRepository.findById(tutor.getId())).thenReturn(Optional.of(tutor));
        when(usuarioRepository.findById(estudiante1.getId())).thenReturn(Optional.of(estudiante1));
        when(tutorEstudianteRepository.findByEstudianteIdAndActivoTrue(estudiante1.getId()))
                .thenReturn(Optional.empty());
        when(tutorEstudianteRepository.save(any(TutorEstudiante.class))).thenAnswer(invocation -> {
            TutorEstudiante te = invocation.getArgument(0);
            te.setId(10L);
            te.setFechaAsignacion(LocalDateTime.now());
            return te;
        });

        // Act
        TutorEstudianteDTO result = tutorEstudianteService.asignarTutor(request);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(tutor.getId(), result.getTutorId());
        assertEquals(estudiante1.getId(), result.getEstudianteId());
        assertEquals("Juan Pérez", result.getNombreTutor());
        assertEquals("María García", result.getNombreEstudiante());
        verify(tutorEstudianteRepository, times(1)).save(any(TutorEstudiante.class));
    }

    @Test
    void asignarTutor_conAsignacionExistente_desactivaAnteriorYCreaNueva() {
        // Arrange
        Usuario nuevoTutor = Usuario.builder()
                .id(200)
                .nombre("Nuevo Tutor")
                .correo("nuevo.tutor@test.com")
                .rol(rolTutor)
                .build();

        AsignarTutorRequest request = new AsignarTutorRequest();
        request.setTutorId(nuevoTutor.getId());
        request.setEstudianteId(estudiante1.getId());

        when(usuarioRepository.findById(nuevoTutor.getId())).thenReturn(Optional.of(nuevoTutor));
        when(usuarioRepository.findById(estudiante1.getId())).thenReturn(Optional.of(estudiante1));
        when(tutorEstudianteRepository.findByEstudianteIdAndActivoTrue(estudiante1.getId()))
                .thenReturn(Optional.of(asignacion1));
        when(tutorEstudianteRepository.save(any(TutorEstudiante.class))).thenAnswer(invocation -> {
            TutorEstudiante te = invocation.getArgument(0);
            if (te.getId() == null) {
                te.setId(11L);
                te.setFechaAsignacion(LocalDateTime.now());
            }
            return te;
        });

        // Act
        TutorEstudianteDTO result = tutorEstudianteService.asignarTutor(request);

        // Assert
        assertNotNull(result);
        assertEquals(nuevoTutor.getId(), result.getTutorId());
        // Verificar que se guardó 2 veces: una para desactivar la anterior, otra para la nueva
        verify(tutorEstudianteRepository, times(2)).save(any(TutorEstudiante.class));
    }

    @Test
    void asignarTutor_tutorNoExiste_lanzaExcepcion() {
        // Arrange
        AsignarTutorRequest request = new AsignarTutorRequest();
        request.setTutorId(999);
        request.setEstudianteId(estudiante1.getId());

        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> tutorEstudianteService.asignarTutor(request));

        assertTrue(exception.getMessage().contains("Tutor"));
        verify(tutorEstudianteRepository, never()).save(any(TutorEstudiante.class));
    }

    @Test
    void asignarTutor_estudianteNoExiste_lanzaExcepcion() {
        // Arrange
        AsignarTutorRequest request = new AsignarTutorRequest();
        request.setTutorId(tutor.getId());
        request.setEstudianteId(999);

        when(usuarioRepository.findById(tutor.getId())).thenReturn(Optional.of(tutor));
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> tutorEstudianteService.asignarTutor(request));

        assertTrue(exception.getMessage().contains("Estudiante"));
        verify(tutorEstudianteRepository, never()).save(any(TutorEstudiante.class));
    }

    @Test
    void obtenerEstudiantesPorTutor_conEstudiantes_retornaLista() {
        // Arrange
        List<TutorEstudiante> asignaciones = Arrays.asList(asignacion1, asignacion2);
        when(tutorEstudianteRepository.findByTutorIdAndActivoTrue(tutor.getId())).thenReturn(asignaciones);
        when(usuarioRepository.findById(tutor.getId())).thenReturn(Optional.of(tutor));
        when(usuarioRepository.findById(estudiante1.getId())).thenReturn(Optional.of(estudiante1));
        when(usuarioRepository.findById(estudiante2.getId())).thenReturn(Optional.of(estudiante2));

        // Act
        List<TutorEstudianteDTO> result = tutorEstudianteService.obtenerEstudiantesPorTutor(tutor.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(tutorEstudianteRepository, times(1)).findByTutorIdAndActivoTrue(tutor.getId());
    }

    @Test
    void obtenerEstudiantesPorTutor_sinEstudiantes_retornaListaVacia() {
        // Arrange
        when(tutorEstudianteRepository.findByTutorIdAndActivoTrue(tutor.getId()))
                .thenReturn(Collections.emptyList());

        // Act
        List<TutorEstudianteDTO> result = tutorEstudianteService.obtenerEstudiantesPorTutor(tutor.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void obtenerTutorPorEstudiante_cuandoExiste_retornaDTO() {
        // Arrange
        when(tutorEstudianteRepository.findByEstudianteIdAndActivoTrue(estudiante1.getId()))
                .thenReturn(Optional.of(asignacion1));
        when(usuarioRepository.findById(tutor.getId())).thenReturn(Optional.of(tutor));
        when(usuarioRepository.findById(estudiante1.getId())).thenReturn(Optional.of(estudiante1));

        // Act
        TutorEstudianteDTO result = tutorEstudianteService.obtenerTutorPorEstudiante(estudiante1.getId());

        // Assert
        assertNotNull(result);
        assertEquals(tutor.getId(), result.getTutorId());
        assertEquals("Juan Pérez", result.getNombreTutor());
        assertEquals("juan.perez@test.com", result.getCorreoTutor());
    }

    @Test
    void obtenerTutorPorEstudiante_sinTutorAsignado_lanzaExcepcion() {
        // Arrange
        when(tutorEstudianteRepository.findByEstudianteIdAndActivoTrue(estudiante1.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        TutorNoAsignadoException exception = assertThrows(TutorNoAsignadoException.class,
                () -> tutorEstudianteService.obtenerTutorPorEstudiante(estudiante1.getId()));

        assertTrue(exception.getMessage().contains(String.valueOf(estudiante1.getId())));
    }

    @Test
    void obtenerTutorEntityPorEstudiante_cuandoExiste_retornaOptionalConUsuario() {
        // Arrange
        when(tutorEstudianteRepository.findTutorByEstudianteId(estudiante1.getId()))
                .thenReturn(Optional.of(tutor));

        // Act
        Optional<Usuario> result = tutorEstudianteService.obtenerTutorEntityPorEstudiante(estudiante1.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(tutor.getId(), result.get().getId());
    }

    @Test
    void obtenerTutorEntityPorEstudiante_sinTutor_retornaOptionalVacio() {
        // Arrange
        when(tutorEstudianteRepository.findTutorByEstudianteId(estudiante1.getId()))
                .thenReturn(Optional.empty());

        // Act
        Optional<Usuario> result = tutorEstudianteService.obtenerTutorEntityPorEstudiante(estudiante1.getId());

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void tieneTutorAsignado_cuandoTiene_retornaTrue() {
        // Arrange
        when(tutorEstudianteRepository.existsByEstudianteIdAndActivoTrue(estudiante1.getId()))
                .thenReturn(true);

        // Act
        boolean result = tutorEstudianteService.tieneTutorAsignado(estudiante1.getId());

        // Assert
        assertTrue(result);
    }

    @Test
    void tieneTutorAsignado_cuandoNoTiene_retornaFalse() {
        // Arrange
        when(tutorEstudianteRepository.existsByEstudianteIdAndActivoTrue(estudiante1.getId()))
                .thenReturn(false);

        // Act
        boolean result = tutorEstudianteService.tieneTutorAsignado(estudiante1.getId());

        // Assert
        assertFalse(result);
    }

    @Test
    void desactivarAsignacion_cuandoExiste_desactiva() {
        // Arrange
        when(tutorEstudianteRepository.findById(1L)).thenReturn(Optional.of(asignacion1));
        when(tutorEstudianteRepository.save(any(TutorEstudiante.class))).thenAnswer(invocation -> {
            TutorEstudiante te = invocation.getArgument(0);
            return te;
        });

        // Act
        tutorEstudianteService.desactivarAsignacion(1L);

        // Assert
        verify(tutorEstudianteRepository, times(1)).save(argThat(te -> !te.getActivo()));
    }

    @Test
    void desactivarAsignacion_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        when(tutorEstudianteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> tutorEstudianteService.desactivarAsignacion(99L));

        assertTrue(exception.getMessage().contains("99"));
        verify(tutorEstudianteRepository, never()).save(any(TutorEstudiante.class));
    }
}
