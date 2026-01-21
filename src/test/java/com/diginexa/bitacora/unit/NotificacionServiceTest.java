package com.diginexa.bitacora.unit;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import com.diginexa.bitacora.dtos.notificacion.NotificacionDTO;
import com.diginexa.bitacora.dtos.notificacion.NotificacionResumenDTO;
import com.diginexa.bitacora.entities.Notificacion;
import com.diginexa.bitacora.exceptions.domain.NotificacionNotFoundException;
import com.diginexa.bitacora.repositories.NotificacionRepository;
import com.diginexa.bitacora.services.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionService notificacionService;

    private Notificacion notificacionCritica;
    private Notificacion notificacionAlerta;
    private Notificacion notificacionInfo;
    private static final Integer USUARIO_ID = 1;

    @BeforeEach
    void setUp() {
        notificacionCritica = Notificacion.builder()
                .id(1L)
                .usuarioId(USUARIO_ID)
                .tipo(TipoNotificacion.VENCIMIENTO_PROXIMO.name())
                .prioridad(PrioridadNotificacion.CRITICO.name())
                .titulo("Plazo vencido")
                .mensaje("El módulo X ha vencido")
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .datosAdicionales(new HashMap<>())
                .build();

        notificacionAlerta = Notificacion.builder()
                .id(2L)
                .usuarioId(USUARIO_ID)
                .tipo(TipoNotificacion.VENCIMIENTO_PROXIMO.name())
                .prioridad(PrioridadNotificacion.ALERTA.name())
                .titulo("3 días restantes")
                .mensaje("Quedan 3 días para completar el módulo")
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .datosAdicionales(new HashMap<>())
                .build();

        notificacionInfo = Notificacion.builder()
                .id(3L)
                .usuarioId(USUARIO_ID)
                .tipo(TipoNotificacion.SOLICITUD_SESION.name())
                .prioridad(PrioridadNotificacion.INFO.name())
                .titulo("Nueva solicitud")
                .mensaje("Tienes una nueva solicitud de sesión")
                .leida(true)
                .fechaCreacion(LocalDateTime.now().minusDays(1))
                .fechaLectura(LocalDateTime.now())
                .datosAdicionales(new HashMap<>())
                .build();
    }

    @Test
    void obtenerNotificaciones_conDatos_retornaLista() {
        // Arrange
        List<Notificacion> notificaciones = Arrays.asList(notificacionCritica, notificacionAlerta, notificacionInfo);
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(USUARIO_ID))
                .thenReturn(notificaciones);

        // Act
        List<NotificacionDTO> result = notificacionService.obtenerNotificaciones(USUARIO_ID);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Plazo vencido", result.get(0).getTitulo());
        verify(notificacionRepository, times(1)).findByUsuarioIdOrderByFechaCreacionDesc(USUARIO_ID);
    }

    @Test
    void obtenerNotificaciones_sinDatos_retornaListaVacia() {
        // Arrange
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(USUARIO_ID))
                .thenReturn(Collections.emptyList());

        // Act
        List<NotificacionDTO> result = notificacionService.obtenerNotificaciones(USUARIO_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificacionRepository, times(1)).findByUsuarioIdOrderByFechaCreacionDesc(USUARIO_ID);
    }

    @Test
    void obtenerNoLeidas_conDatos_retornaNotificacionesNoLeidas() {
        // Arrange
        List<Notificacion> noLeidas = Arrays.asList(notificacionCritica, notificacionAlerta);
        when(notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(USUARIO_ID))
                .thenReturn(noLeidas);

        // Act
        List<NotificacionDTO> result = notificacionService.obtenerNoLeidas(USUARIO_ID);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertFalse(result.get(0).getLeida());
        verify(notificacionRepository, times(1)).findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(USUARIO_ID);
    }

    @Test
    void obtenerPorTipo_filtraPorTipo() {
        // Arrange
        String tipo = TipoNotificacion.VENCIMIENTO_PROXIMO.name();
        List<Notificacion> notificaciones = Arrays.asList(notificacionCritica, notificacionAlerta);
        when(notificacionRepository.findByUsuarioIdAndTipoOrderByFechaCreacionDesc(USUARIO_ID, tipo))
                .thenReturn(notificaciones);

        // Act
        List<NotificacionDTO> result = notificacionService.obtenerPorTipo(USUARIO_ID, tipo);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(n -> n.getTipo().equals(tipo)));
        verify(notificacionRepository, times(1)).findByUsuarioIdAndTipoOrderByFechaCreacionDesc(USUARIO_ID, tipo);
    }

    @Test
    void obtenerResumen_calculaConteosCorrectamente() {
        // Arrange
        List<Notificacion> noLeidas = Arrays.asList(notificacionCritica, notificacionAlerta);
        when(notificacionRepository.contarNoLeidas(USUARIO_ID)).thenReturn(2L);
        when(notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(USUARIO_ID))
                .thenReturn(noLeidas);

        // Act
        NotificacionResumenDTO result = notificacionService.obtenerResumen(USUARIO_ID);

        // Assert
        assertNotNull(result);
        assertEquals(USUARIO_ID, result.getUsuarioId());
        assertEquals(2L, result.getTotalNoLeidas());
        assertEquals(1L, result.getTotalCriticas());
        assertEquals(1L, result.getTotalAlertas());
        assertEquals(0L, result.getTotalInfo());
    }

    @Test
    void obtenerPorId_cuandoExiste_retornaNotificacion() {
        // Arrange
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacionCritica));

        // Act
        NotificacionDTO result = notificacionService.obtenerPorId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Plazo vencido", result.getTitulo());
        verify(notificacionRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorId_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        NotificacionNotFoundException exception = assertThrows(NotificacionNotFoundException.class,
                () -> notificacionService.obtenerPorId(99L));

        assertTrue(exception.getMessage().contains("99"));
        verify(notificacionRepository, times(1)).findById(99L);
    }

    @Test
    void marcarComoLeida_cuandoExiste_marcaYRetornaDTO() {
        // Arrange
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacionCritica));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setLeida(true);
            n.setFechaLectura(LocalDateTime.now());
            return n;
        });

        // Act
        NotificacionDTO result = notificacionService.marcarComoLeida(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getLeida());
        assertNotNull(result.getFechaLectura());
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    void marcarComoLeida_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotificacionNotFoundException.class,
                () -> notificacionService.marcarComoLeida(99L));

        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    void marcarTodasComoLeidas_llamaAlRepositorio() {
        // Arrange
        doNothing().when(notificacionRepository).marcarTodasComoLeidas(eq(USUARIO_ID), any(LocalDateTime.class));

        // Act
        notificacionService.marcarTodasComoLeidas(USUARIO_ID);

        // Assert
        verify(notificacionRepository, times(1)).marcarTodasComoLeidas(eq(USUARIO_ID), any(LocalDateTime.class));
    }

    @Test
    void eliminar_cuandoExiste_eliminaNotificacion() {
        // Arrange
        when(notificacionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(notificacionRepository).deleteById(1L);

        // Act
        notificacionService.eliminar(1L);

        // Assert
        verify(notificacionRepository, times(1)).existsById(1L);
        verify(notificacionRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        when(notificacionRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(NotificacionNotFoundException.class,
                () -> notificacionService.eliminar(99L));

        verify(notificacionRepository, never()).deleteById(anyLong());
    }

    @Test
    void crearNotificacion_guardaYRetornaEntidad() {
        // Arrange
        Map<String, Object> datosAdicionales = Map.of("seccionCodigo", "SEC-001");
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(10L);
            n.setFechaCreacion(LocalDateTime.now());
            return n;
        });

        // Act
        Notificacion result = notificacionService.crearNotificacion(
                USUARIO_ID,
                TipoNotificacion.VENCIMIENTO_PROXIMO,
                PrioridadNotificacion.CRITICO,
                "Título de prueba",
                "Mensaje de prueba",
                datosAdicionales
        );

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(USUARIO_ID, result.getUsuarioId());
        assertEquals(TipoNotificacion.VENCIMIENTO_PROXIMO.name(), result.getTipo());
        assertEquals(PrioridadNotificacion.CRITICO.name(), result.getPrioridad());
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    void marcarComoEntregadaEmail_llamaAlRepositorio() {
        // Arrange
        doNothing().when(notificacionRepository).marcarComoEntregadaEmail(1L);

        // Act
        notificacionService.marcarComoEntregadaEmail(1L);

        // Assert
        verify(notificacionRepository, times(1)).marcarComoEntregadaEmail(1L);
    }

    @Test
    void marcarComoEntregadaWebsocket_llamaAlRepositorio() {
        // Arrange
        doNothing().when(notificacionRepository).marcarComoEntregadaWebsocket(1L);

        // Act
        notificacionService.marcarComoEntregadaWebsocket(1L);

        // Assert
        verify(notificacionRepository, times(1)).marcarComoEntregadaWebsocket(1L);
    }

    @Test
    void convertToDTO_asignaSeverityCorrectamente() {
        // Arrange
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacionCritica));

        // Act
        NotificacionDTO result = notificacionService.obtenerPorId(1L);

        // Assert
        assertEquals("error", result.getSeverity()); // CRITICO -> error
    }
}
