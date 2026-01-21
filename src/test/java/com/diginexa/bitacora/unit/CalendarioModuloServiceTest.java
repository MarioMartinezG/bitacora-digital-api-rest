package com.diginexa.bitacora.unit;

import com.diginexa.bitacora.dtos.notificacion.CalendarioModuloDTO;
import com.diginexa.bitacora.entities.CalendarioModulo;
import com.diginexa.bitacora.exceptions.domain.CalendarioModuloNotFoundException;
import com.diginexa.bitacora.repositories.CalendarioModuloRepository;
import com.diginexa.bitacora.services.CalendarioModuloService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarioModuloServiceTest {

    @Mock
    private CalendarioModuloRepository calendarioRepository;

    @InjectMocks
    private CalendarioModuloService calendarioService;

    private CalendarioModulo calendarioProximo;
    private CalendarioModulo calendarioNormal;
    private CalendarioModulo calendarioVencido;

    @BeforeEach
    void setUp() {
        calendarioProximo = CalendarioModulo.builder()
                .id(1L)
                .seccionCodigo("SEC-001")
                .nombreModulo("Módulo 1: Caracterización")
                .fechaLimite(LocalDate.now().plusDays(2))
                .descripcion("Fecha límite para completar el módulo 1")
                .activo(true)
                .fechaCreacion(LocalDateTime.now().minusDays(30))
                .build();

        calendarioNormal = CalendarioModulo.builder()
                .id(2L)
                .seccionCodigo("SEC-002")
                .nombreModulo("Módulo 2: Factores situacionales")
                .fechaLimite(LocalDate.now().plusDays(10))
                .descripcion("Fecha límite para completar el módulo 2")
                .activo(true)
                .fechaCreacion(LocalDateTime.now().minusDays(20))
                .build();

        calendarioVencido = CalendarioModulo.builder()
                .id(3L)
                .seccionCodigo("SEC-003")
                .nombreModulo("Módulo 3: Objetivos")
                .fechaLimite(LocalDate.now().minusDays(5))
                .descripcion("Fecha límite para completar el módulo 3")
                .activo(true)
                .fechaCreacion(LocalDateTime.now().minusDays(40))
                .build();
    }

    @Test
    void listarTodos_retornaCalendariosActivos() {
        // Arrange
        List<CalendarioModulo> calendarios = Arrays.asList(calendarioProximo, calendarioNormal);
        when(calendarioRepository.findByActivoTrueOrderByFechaLimiteAsc()).thenReturn(calendarios);

        // Act
        List<CalendarioModuloDTO> result = calendarioService.listarTodos();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(calendarioRepository, times(1)).findByActivoTrueOrderByFechaLimiteAsc();
    }

    @Test
    void listarTodos_sinCalendarios_retornaListaVacia() {
        // Arrange
        when(calendarioRepository.findByActivoTrueOrderByFechaLimiteAsc()).thenReturn(Collections.emptyList());

        // Act
        List<CalendarioModuloDTO> result = calendarioService.listarTodos();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void obtenerPorSeccion_cuandoExiste_retornaCalendario() {
        // Arrange
        when(calendarioRepository.findBySeccionCodigo("SEC-001"))
                .thenReturn(Optional.of(calendarioProximo));

        // Act
        CalendarioModuloDTO result = calendarioService.obtenerPorSeccion("SEC-001");

        // Assert
        assertNotNull(result);
        assertEquals("SEC-001", result.getSeccionCodigo());
        assertEquals("Módulo 1: Caracterización", result.getNombreModulo());
        verify(calendarioRepository, times(1)).findBySeccionCodigo("SEC-001");
    }

    @Test
    void obtenerPorSeccion_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        when(calendarioRepository.findBySeccionCodigo("SEC-999"))
                .thenReturn(Optional.empty());

        // Act & Assert
        CalendarioModuloNotFoundException exception = assertThrows(CalendarioModuloNotFoundException.class,
                () -> calendarioService.obtenerPorSeccion("SEC-999"));

        assertTrue(exception.getMessage().contains("SEC-999"));
    }

    @Test
    void obtenerPorId_cuandoExiste_retornaCalendario() {
        // Arrange
        when(calendarioRepository.findById(1L)).thenReturn(Optional.of(calendarioProximo));

        // Act
        CalendarioModuloDTO result = calendarioService.obtenerPorId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(calendarioRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorId_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        when(calendarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CalendarioModuloNotFoundException.class,
                () -> calendarioService.obtenerPorId(99L));
    }

    @Test
    void crear_conDatosValidos_creaYRetornaDTO() {
        // Arrange
        CalendarioModuloDTO dto = CalendarioModuloDTO.builder()
                .seccionCodigo("SEC-NEW")
                .nombreModulo("Nuevo Módulo")
                .fechaLimite(LocalDate.now().plusDays(15))
                .descripcion("Descripción del nuevo módulo")
                .activo(true)
                .build();

        when(calendarioRepository.existsBySeccionCodigo("SEC-NEW")).thenReturn(false);
        when(calendarioRepository.save(any(CalendarioModulo.class))).thenAnswer(invocation -> {
            CalendarioModulo cm = invocation.getArgument(0);
            cm.setId(10L);
            cm.setFechaCreacion(LocalDateTime.now());
            return cm;
        });

        // Act
        CalendarioModuloDTO result = calendarioService.crear(dto);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("SEC-NEW", result.getSeccionCodigo());
        verify(calendarioRepository, times(1)).save(any(CalendarioModulo.class));
    }

    @Test
    void crear_conSeccionDuplicada_lanzaExcepcion() {
        // Arrange
        CalendarioModuloDTO dto = CalendarioModuloDTO.builder()
                .seccionCodigo("SEC-001")
                .nombreModulo("Módulo duplicado")
                .fechaLimite(LocalDate.now().plusDays(10))
                .build();

        when(calendarioRepository.existsBySeccionCodigo("SEC-001")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> calendarioService.crear(dto));

        assertTrue(exception.getMessage().contains("SEC-001"));
        verify(calendarioRepository, never()).save(any(CalendarioModulo.class));
    }

    @Test
    void actualizar_cuandoExiste_actualizaCampos() {
        // Arrange
        CalendarioModuloDTO dto = CalendarioModuloDTO.builder()
                .nombreModulo("Módulo Actualizado")
                .fechaLimite(LocalDate.now().plusDays(20))
                .descripcion("Nueva descripción")
                .activo(false)
                .build();

        when(calendarioRepository.findById(1L)).thenReturn(Optional.of(calendarioProximo));
        when(calendarioRepository.save(any(CalendarioModulo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CalendarioModuloDTO result = calendarioService.actualizar(1L, dto);

        // Assert
        assertNotNull(result);
        assertEquals("Módulo Actualizado", result.getNombreModulo());
        assertEquals("Nueva descripción", result.getDescripcion());
        assertFalse(result.getActivo());
        verify(calendarioRepository, times(1)).save(any(CalendarioModulo.class));
    }

    @Test
    void actualizar_conCamposNulos_noSobrescribe() {
        // Arrange
        CalendarioModuloDTO dto = CalendarioModuloDTO.builder()
                .nombreModulo("Solo actualizo nombre")
                .build();

        when(calendarioRepository.findById(1L)).thenReturn(Optional.of(calendarioProximo));
        when(calendarioRepository.save(any(CalendarioModulo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CalendarioModuloDTO result = calendarioService.actualizar(1L, dto);

        // Assert
        assertNotNull(result);
        assertEquals("Solo actualizo nombre", result.getNombreModulo());
        // La fecha límite original se mantiene
        assertEquals(calendarioProximo.getFechaLimite(), result.getFechaLimite());
    }

    @Test
    void actualizar_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        CalendarioModuloDTO dto = CalendarioModuloDTO.builder()
                .nombreModulo("No importa")
                .build();

        when(calendarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CalendarioModuloNotFoundException.class,
                () -> calendarioService.actualizar(99L, dto));

        verify(calendarioRepository, never()).save(any(CalendarioModulo.class));
    }

    @Test
    void eliminar_cuandoExiste_eliminaCalendario() {
        // Arrange
        when(calendarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(calendarioRepository).deleteById(1L);

        // Act
        calendarioService.eliminar(1L);

        // Assert
        verify(calendarioRepository, times(1)).existsById(1L);
        verify(calendarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuandoNoExiste_lanzaExcepcion() {
        // Arrange
        when(calendarioRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(CalendarioModuloNotFoundException.class,
                () -> calendarioService.eliminar(99L));

        verify(calendarioRepository, never()).deleteById(anyLong());
    }

    @Test
    void obtenerProximosVencimientos_retornaCalendariosEnRango() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        LocalDate hasta = hoy.plusDays(7);
        List<CalendarioModulo> calendarios = Arrays.asList(calendarioProximo);

        when(calendarioRepository.findByFechaLimiteBetween(hoy, hasta)).thenReturn(calendarios);

        // Act
        List<CalendarioModuloDTO> result = calendarioService.obtenerProximosVencimientos(7);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(calendarioRepository, times(1)).findByFechaLimiteBetween(hoy, hasta);
    }

    @Test
    void convertToDTO_calculaDiasRestantesCorrectamente() {
        // Arrange
        when(calendarioRepository.findById(1L)).thenReturn(Optional.of(calendarioProximo));

        // Act
        CalendarioModuloDTO result = calendarioService.obtenerPorId(1L);

        // Assert
        assertEquals(2, result.getDiasRestantes());
    }

    @Test
    void convertToDTO_estadoVencimiento_PROXIMO_cuandoMenosDe3Dias() {
        // Arrange - calendarioProximo tiene fechaLimite en 2 días
        when(calendarioRepository.findById(1L)).thenReturn(Optional.of(calendarioProximo));

        // Act
        CalendarioModuloDTO result = calendarioService.obtenerPorId(1L);

        // Assert
        assertEquals("PROXIMO", result.getEstadoVencimiento());
    }

    @Test
    void convertToDTO_estadoVencimiento_NORMAL_cuandoMasDe3Dias() {
        // Arrange - calendarioNormal tiene fechaLimite en 10 días
        when(calendarioRepository.findById(2L)).thenReturn(Optional.of(calendarioNormal));

        // Act
        CalendarioModuloDTO result = calendarioService.obtenerPorId(2L);

        // Assert
        assertEquals("NORMAL", result.getEstadoVencimiento());
    }

    @Test
    void convertToDTO_estadoVencimiento_VENCIDO_cuandoDiasNegativos() {
        // Arrange - calendarioVencido tiene fechaLimite hace 5 días
        when(calendarioRepository.findById(3L)).thenReturn(Optional.of(calendarioVencido));

        // Act
        CalendarioModuloDTO result = calendarioService.obtenerPorId(3L);

        // Assert
        assertEquals("VENCIDO", result.getEstadoVencimiento());
        assertTrue(result.getDiasRestantes() < 0);
    }
}
