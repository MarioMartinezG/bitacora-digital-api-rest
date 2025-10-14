package com.diginexa.bitacora.unit;

import com.diginexa.bitacora.dtos.RespuestaRequest;
import com.diginexa.bitacora.entities.CampoSeccion;
import com.diginexa.bitacora.entities.Respuesta;
import com.diginexa.bitacora.entities.Seccion;
import com.diginexa.bitacora.exceptions.domain.CampoNotFoundException;
import com.diginexa.bitacora.exceptions.domain.SeccionNotFoundException;
import com.diginexa.bitacora.exceptions.validation.RespuestaValidationException;
import com.diginexa.bitacora.repositories.CampoSeccionRepository;
import com.diginexa.bitacora.repositories.RespuestaRepository;
import com.diginexa.bitacora.repositories.SeccionRepository;
import com.diginexa.bitacora.services.RespuestaService;
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
class RespuestaServiceTest {

    @Mock
    private RespuestaRepository respuestaRepository;

    @Mock
    private SeccionRepository seccionRepository;

    @Mock
    private CampoSeccionRepository campoSeccionRepository;

    @InjectMocks
    private RespuestaService respuestaService;

    private Seccion seccion;
    private CampoSeccion campo;
    private RespuestaRequest respuestaRequest;
    private Respuesta respuestaExistente;

    @BeforeEach
    void setUp() {
        seccion = Seccion.builder()
                .id(4)
                .nombre("Contexto Específico")
                .build();

        campo = CampoSeccion.builder()
                .id(1)
                .label("¿El curso hace parte de un programa de pregrado, o posgrado?")
                .tipoCampo("seleccion")
                .seccion(seccion)
                .build();

        respuestaRequest = RespuestaRequest.builder()
                .usuarioId(123)
                .seccionId(4L)
                .campoId(1L)
                .respuestaTexto("Pregrado")
                .estadoAvance("completado")
                .build();

        respuestaExistente = Respuesta.builder()
                .id(100L)
                .usuarioId(123)
                .seccion(seccion)
                .campo(campo)
                .respuestaTexto("Respuesta anterior")
                .estadoAvance("en_desarrollo")
                .fechaCreacion(LocalDateTime.now().minusDays(1))
                .fechaActualizacion(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void guardarEnLote_WithValidRequests_ShouldSaveAllResponses() {
        // Arrange
        List<RespuestaRequest> requests = Arrays.asList(respuestaRequest);

        when(seccionRepository.findById(4L)).thenReturn(Optional.of(seccion));
        when(campoSeccionRepository.findById(1L)).thenReturn(Optional.of(campo));
        when(respuestaRepository.findByUsuarioIdAndCampoId(123, 1L)).thenReturn(Optional.empty());
        when(respuestaRepository.save(any(Respuesta.class))).thenReturn(respuestaExistente);

        // Act
        List<Respuesta> result = respuestaService.guardarEnLote(requests);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(seccionRepository, times(1)).findById(4L);
        verify(campoSeccionRepository, times(1)).findById(1L);
        verify(respuestaRepository, times(1)).save(any(Respuesta.class));
    }

    @Test
    void guardarEnLote_WithEmptyList_ShouldThrowValidationException() {
        // Arrange
        List<RespuestaRequest> emptyRequests = Arrays.asList();

        // Act & Assert
        RespuestaValidationException exception = assertThrows(RespuestaValidationException.class,
                () -> respuestaService.guardarEnLote(emptyRequests));

        assertEquals("La lista de respuestas no puede estar vacía", exception.getMessage());
        verify(respuestaRepository, never()).save(any(Respuesta.class));
    }

    @Test
    void guardarRespuestaIndividual_WhenNewResponse_ShouldCreateNewResponse() {
        // Arrange
        when(seccionRepository.findById(4L)).thenReturn(Optional.of(seccion));
        when(campoSeccionRepository.findById(1L)).thenReturn(Optional.of(campo));
        when(respuestaRepository.findByUsuarioIdAndCampoId(123, 1L)).thenReturn(Optional.empty());
        when(respuestaRepository.save(any(Respuesta.class))).thenReturn(respuestaExistente);

        // Act
        Respuesta result = respuestaService.guardarRespuestaIndividual(respuestaRequest);

        // Assert
        assertNotNull(result);
        verify(seccionRepository, times(1)).findById(4L);
        verify(campoSeccionRepository, times(1)).findById(1L);
        verify(respuestaRepository, times(1)).save(any(Respuesta.class));
    }

    @Test
    void guardarRespuestaIndividual_WhenExistingResponse_ShouldUpdateResponse() {
        // Arrange
        when(respuestaRepository.findByUsuarioIdAndCampoId(123, 1L)).thenReturn(Optional.of(respuestaExistente));
        when(respuestaRepository.save(any(Respuesta.class))).thenReturn(respuestaExistente);

        // Act
        Respuesta result = respuestaService.guardarRespuestaIndividual(respuestaRequest);

        // Assert
        assertNotNull(result);
        verify(seccionRepository, never()).findById(anyLong());
        verify(campoSeccionRepository, never()).findById(anyLong());
        verify(respuestaRepository, times(1)).save(respuestaExistente);
    }

    @Test
    void guardarRespuestaIndividual_WithNullUsuarioId_ShouldThrowValidationException() {
        // Arrange
        RespuestaRequest invalidRequest = RespuestaRequest.builder()
                .usuarioId(null)
                .seccionId(4L)
                .campoId(1L)
                .respuestaTexto("Pregrado")
                .estadoAvance("completado")
                .build();

        // Act & Assert
        RespuestaValidationException exception = assertThrows(RespuestaValidationException.class,
                () -> respuestaService.guardarRespuestaIndividual(invalidRequest));

        assertEquals("El ID de usuario es requerido", exception.getMessage());
    }

    @Test
    void guardarRespuestaIndividual_WhenSeccionNotFound_ShouldThrowSeccionNotFoundException() {
        // Arrange
        when(respuestaRepository.findByUsuarioIdAndCampoId(123, 1L)).thenReturn(Optional.empty());
        when(seccionRepository.findById(99L)).thenReturn(Optional.empty());

        respuestaRequest.setSeccionId(99L);

        // Act & Assert
        SeccionNotFoundException exception = assertThrows(SeccionNotFoundException.class,
                () -> respuestaService.guardarRespuestaIndividual(respuestaRequest));

        assertEquals("Sección no encontrada con ID: 99", exception.getMessage());
    }

    @Test
    void guardarRespuestaIndividual_WhenCampoNotFound_ShouldThrowCampoNotFoundException() {
        // Arrange
        when(respuestaRepository.findByUsuarioIdAndCampoId(123, 99L)).thenReturn(Optional.empty());
        when(seccionRepository.findById(4L)).thenReturn(Optional.of(seccion));
        when(campoSeccionRepository.findById(99L)).thenReturn(Optional.empty());

        respuestaRequest.setCampoId(99L);

        // Act & Assert
        CampoNotFoundException exception = assertThrows(CampoNotFoundException.class,
                () -> respuestaService.guardarRespuestaIndividual(respuestaRequest));

        assertEquals("Campo no encontrado con ID: 99", exception.getMessage());
    }

    @Test
    void findByUsuarioIdAndModuloId_WithValidParameters_ShouldReturnResponses() {
        // Arrange
        List<Respuesta> expectedResponses = Collections.singletonList(respuestaExistente);
        when(respuestaRepository.findByUsuarioIdAndModuloId(123, 2L)).thenReturn(expectedResponses);

        // Act
        List<Respuesta> result = respuestaService.findByUsuarioIdAndModuloId(123, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(respuestaRepository, times(1)).findByUsuarioIdAndModuloId(123, 2L);
    }

    @Test
    void findByUsuarioIdAndModuloId_WithNullUsuarioId_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> respuestaService.findByUsuarioIdAndModuloId(null, 2L));

        assertEquals("El ID de usuario no puede ser nulo", exception.getMessage());
    }

    @Test
    void eliminarRespuesta_WithValidParameters_ShouldDeleteResponse() {
        // Arrange
        when(respuestaRepository.existsByUsuarioIdAndCampoId(123, 1L)).thenReturn(true);
        doNothing().when(respuestaRepository).deleteByUsuarioIdAndCampoId(123, 1L);

        // Act
        respuestaService.eliminarRespuesta(123, 1L);

        // Assert
        verify(respuestaRepository, times(1)).existsByUsuarioIdAndCampoId(123, 1L);
        verify(respuestaRepository, times(1)).deleteByUsuarioIdAndCampoId(123, 1L);
    }

    @Test
    void eliminarRespuesta_WithNullParameters_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> respuestaService.eliminarRespuesta(null, null));

        assertEquals("Usuario ID y Campo ID son requeridos", exception.getMessage());
    }
}
