package com.diginexa.bitacora.unit;

import com.diginexa.bitacora.constants.ClaveConfiguracion;
import com.diginexa.bitacora.dtos.notificacion.ConfiguracionNotificacionDTO;
import com.diginexa.bitacora.entities.ConfiguracionNotificacion;
import com.diginexa.bitacora.exceptions.domain.ConfiguracionNotFoundException;
import com.diginexa.bitacora.repositories.ConfiguracionNotificacionRepository;
import com.diginexa.bitacora.services.ConfiguracionNotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguracionNotificacionServiceTest {

    @Mock
    private ConfiguracionNotificacionRepository configuracionRepository;

    @InjectMocks
    private ConfiguracionNotificacionService configuracionService;

    private ConfiguracionNotificacion configDiasAnticipacion;
    private ConfiguracionNotificacion configUmbral;
    private ConfiguracionNotificacion configEmailHabilitado;
    private ConfiguracionNotificacion configWebSocketHabilitado;

    @BeforeEach
    void setUp() {
        configDiasAnticipacion = ConfiguracionNotificacion.builder()
                .id(1L)
                .clave(ClaveConfiguracion.DIAS_ANTICIPACION_VENCIMIENTO)
                .valor("7,3,1")
                .descripcion("Días de anticipación para notificar vencimientos")
                .tipoDato("STRING")
                .build();

        configUmbral = ConfiguracionNotificacion.builder()
                .id(2L)
                .clave(ClaveConfiguracion.UMBRAL_PROGRESO_NOTIFICACION)
                .valor("80")
                .descripcion("Porcentaje de progreso para notificar")
                .tipoDato("INTEGER")
                .build();

        configEmailHabilitado = ConfiguracionNotificacion.builder()
                .id(3L)
                .clave(ClaveConfiguracion.EMAIL_HABILITADO)
                .valor("true")
                .descripcion("Habilitar notificaciones por email")
                .tipoDato("BOOLEAN")
                .build();

        configWebSocketHabilitado = ConfiguracionNotificacion.builder()
                .id(4L)
                .clave(ClaveConfiguracion.WEBSOCKET_HABILITADO)
                .valor("false")
                .descripcion("Habilitar notificaciones por WebSocket")
                .tipoDato("BOOLEAN")
                .build();
    }

    @Test
    void listarTodas_retornaTodasLasConfiguraciones() {
        // Arrange
        List<ConfiguracionNotificacion> configuraciones = Arrays.asList(
                configDiasAnticipacion, configUmbral, configEmailHabilitado, configWebSocketHabilitado);
        when(configuracionRepository.findAll()).thenReturn(configuraciones);

        // Act
        List<ConfiguracionNotificacionDTO> result = configuracionService.listarTodas();

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        verify(configuracionRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorClave_cuandoExiste_retornaConfiguracion() {
        // Arrange
        when(configuracionRepository.findByClave(ClaveConfiguracion.DIAS_ANTICIPACION_VENCIMIENTO))
                .thenReturn(Optional.of(configDiasAnticipacion));

        // Act
        ConfiguracionNotificacionDTO result = configuracionService
                .obtenerPorClave(ClaveConfiguracion.DIAS_ANTICIPACION_VENCIMIENTO);

        // Assert
        assertNotNull(result);
        assertEquals(ClaveConfiguracion.DIAS_ANTICIPACION_VENCIMIENTO, result.getClave());
        assertEquals("7,3,1", result.getValor());
        verify(configuracionRepository, times(1)).findByClave(ClaveConfiguracion.DIAS_ANTICIPACION_VENCIMIENTO);
    }

    @Test
    void obtenerPorClave_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        when(configuracionRepository.findByClave("CLAVE_INEXISTENTE"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ConfiguracionNotFoundException exception = assertThrows(ConfiguracionNotFoundException.class,
                () -> configuracionService.obtenerPorClave("CLAVE_INEXISTENTE"));

        assertTrue(exception.getMessage().contains("CLAVE_INEXISTENTE"));
    }

    @Test
    void obtenerValor_cuandoExiste_retornaValor() {
        // Arrange
        when(configuracionRepository.findByClave(ClaveConfiguracion.UMBRAL_PROGRESO_NOTIFICACION))
                .thenReturn(Optional.of(configUmbral));

        // Act
        String result = configuracionService.obtenerValor(ClaveConfiguracion.UMBRAL_PROGRESO_NOTIFICACION);

        // Assert
        assertEquals("80", result);
    }

    @Test
    void obtenerValor_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        when(configuracionRepository.findByClave("CLAVE_INEXISTENTE"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConfiguracionNotFoundException.class,
                () -> configuracionService.obtenerValor("CLAVE_INEXISTENTE"));
    }

    @Test
    void obtenerValorConDefault_cuandoExiste_retornaValor() {
        // Arrange
        when(configuracionRepository.findByClave(ClaveConfiguracion.UMBRAL_PROGRESO_NOTIFICACION))
                .thenReturn(Optional.of(configUmbral));

        // Act
        String result = configuracionService.obtenerValor(ClaveConfiguracion.UMBRAL_PROGRESO_NOTIFICACION, "50");

        // Assert
        assertEquals("80", result);
    }

    @Test
    void obtenerValorConDefault_cuandoNoExiste_retornaDefault() {
        // Arrange
        when(configuracionRepository.findByClave("CLAVE_INEXISTENTE"))
                .thenReturn(Optional.empty());

        // Act
        String result = configuracionService.obtenerValor("CLAVE_INEXISTENTE", "valor_por_defecto");

        // Assert
        assertEquals("valor_por_defecto", result);
    }

    @Test
    void actualizarValor_cuandoExiste_actualizaYRetornaDTO() {
        // Arrange
        when(configuracionRepository.findByClave(ClaveConfiguracion.UMBRAL_PROGRESO_NOTIFICACION))
                .thenReturn(Optional.of(configUmbral));
        when(configuracionRepository.save(any(ConfiguracionNotificacion.class))).thenAnswer(invocation -> {
            ConfiguracionNotificacion config = invocation.getArgument(0);
            return config;
        });

        // Act
        ConfiguracionNotificacionDTO result = configuracionService
                .actualizarValor(ClaveConfiguracion.UMBRAL_PROGRESO_NOTIFICACION, "90");

        // Assert
        assertNotNull(result);
        assertEquals("90", result.getValor());
        verify(configuracionRepository, times(1)).save(any(ConfiguracionNotificacion.class));
    }

    @Test
    void actualizarValor_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        when(configuracionRepository.findByClave("CLAVE_INEXISTENTE"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConfiguracionNotFoundException.class,
                () -> configuracionService.actualizarValor("CLAVE_INEXISTENTE", "nuevo_valor"));

        verify(configuracionRepository, never()).save(any(ConfiguracionNotificacion.class));
    }

    @Test
    void getDiasAnticipacionVencimiento_parseaCorrectamente() {
        // Arrange
        when(configuracionRepository.findByClave(ClaveConfiguracion.DIAS_ANTICIPACION_VENCIMIENTO))
                .thenReturn(Optional.of(configDiasAnticipacion));

        // Act
        List<Integer> result = configuracionService.getDiasAnticipacionVencimiento();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(7, result.get(0));
        assertEquals(3, result.get(1));
        assertEquals(1, result.get(2));
    }

    @Test
    void getDiasAnticipacionVencimiento_cuandoNoExiste_usaDefault() {
        // Arrange
        when(configuracionRepository.findByClave(ClaveConfiguracion.DIAS_ANTICIPACION_VENCIMIENTO))
                .thenReturn(Optional.empty());

        // Act
        List<Integer> result = configuracionService.getDiasAnticipacionVencimiento();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(7, result.get(0)); // Default "7,3,1"
    }

    @Test
    void getUmbralProgresoNotificacion_parseaCorrectamente() {
        // Arrange
        when(configuracionRepository.findByClave(ClaveConfiguracion.UMBRAL_PROGRESO_NOTIFICACION))
                .thenReturn(Optional.of(configUmbral));

        // Act
        int result = configuracionService.getUmbralProgresoNotificacion();

        // Assert
        assertEquals(80, result);
    }

    @Test
    void getUmbralProgresoNotificacion_cuandoNoExiste_usaDefault() {
        // Arrange
        when(configuracionRepository.findByClave(ClaveConfiguracion.UMBRAL_PROGRESO_NOTIFICACION))
                .thenReturn(Optional.empty());

        // Act
        int result = configuracionService.getUmbralProgresoNotificacion();

        // Assert
        assertEquals(80, result); // Default "80"
    }

    @Test
    void isEmailHabilitado_cuandoTrue_retornaTrue() {
        // Arrange
        when(configuracionRepository.findByClave(ClaveConfiguracion.EMAIL_HABILITADO))
                .thenReturn(Optional.of(configEmailHabilitado));

        // Act
        boolean result = configuracionService.isEmailHabilitado();

        // Assert
        assertTrue(result);
    }

    @Test
    void isEmailHabilitado_cuandoFalse_retornaFalse() {
        // Arrange
        ConfiguracionNotificacion configFalse = ConfiguracionNotificacion.builder()
                .clave(ClaveConfiguracion.EMAIL_HABILITADO)
                .valor("false")
                .build();
        when(configuracionRepository.findByClave(ClaveConfiguracion.EMAIL_HABILITADO))
                .thenReturn(Optional.of(configFalse));

        // Act
        boolean result = configuracionService.isEmailHabilitado();

        // Assert
        assertFalse(result);
    }

    @Test
    void isWebSocketHabilitado_cuandoFalse_retornaFalse() {
        // Arrange
        when(configuracionRepository.findByClave(ClaveConfiguracion.WEBSOCKET_HABILITADO))
                .thenReturn(Optional.of(configWebSocketHabilitado));

        // Act
        boolean result = configuracionService.isWebSocketHabilitado();

        // Assert
        assertFalse(result);
    }

    @Test
    void isWebSocketHabilitado_cuandoNoExiste_usaDefaultTrue() {
        // Arrange
        when(configuracionRepository.findByClave(ClaveConfiguracion.WEBSOCKET_HABILITADO))
                .thenReturn(Optional.empty());

        // Act
        boolean result = configuracionService.isWebSocketHabilitado();

        // Assert
        assertTrue(result); // Default "true"
    }
}
