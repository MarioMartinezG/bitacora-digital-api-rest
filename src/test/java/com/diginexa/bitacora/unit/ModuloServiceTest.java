package com.diginexa.bitacora.unit;

import com.diginexa.bitacora.entities.Modulo;
import com.diginexa.bitacora.exceptions.domain.ModuloNotFoundException;
import com.diginexa.bitacora.repositories.ModuloRepository;
import com.diginexa.bitacora.services.ModuloService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuloServiceTest {

    @Mock
    private ModuloRepository moduloRepository;

    @InjectMocks
    private ModuloService moduloService;

    private Modulo modulo1;
    private Modulo modulo2;

    @BeforeEach
    void setUp() {
        modulo1 = Modulo.builder()
                .id(1L)
                .nombre("Módulo 1: Caracteriza tu asignatura")
                .descripcion("Identificación de la asignatura")
                .orden(1)
                .fechaInicio(LocalDate.of(2025, 5, 7))
                .fechaFin(LocalDate.of(2025, 5, 11))
                .build();

        modulo2 = Modulo.builder()
                .id(2L)
                .nombre("Módulo 2: Factores situacionales")
                .descripcion("Contexto específico")
                .orden(2)
                .fechaInicio(LocalDate.of(2025, 5, 12))
                .fechaFin(LocalDate.of(2025, 5, 16))
                .build();
    }

    @Test
    void findAll_ShouldReturnAllModulos() {
        // Arrange
        List<Modulo> expectedModulos = Arrays.asList(modulo1, modulo2);
        when(moduloRepository.findAllByOrderByOrdenAsc()).thenReturn(expectedModulos);

        // Act
        List<Modulo> result = moduloService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Módulo 1: Caracteriza tu asignatura", result.get(0).getNombre());
        verify(moduloRepository, times(1)).findAllByOrderByOrdenAsc();
    }

    @Test
    void findById_WhenModuloExists_ShouldReturnModulo() {
        // Arrange
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo1));

        // Act
        Modulo result = moduloService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Módulo 1: Caracteriza tu asignatura", result.getNombre());
        verify(moduloRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenModuloNotExists_ShouldThrowModuloNotFoundException() {
        // Arrange
        when(moduloRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ModuloNotFoundException exception = assertThrows(ModuloNotFoundException.class,
                () -> moduloService.findById(99L));

        assertEquals("Módulo no encontrado con ID: 99", exception.getMessage());
        verify(moduloRepository, times(1)).findById(99L);
    }

    @Test
    void findByIdWithSecciones_WhenModuloExists_ShouldReturnModuloWithSecciones() {
        // Arrange
        when(moduloRepository.findByIdWithSecciones(1L)).thenReturn(Optional.of(modulo1));

        // Act
        Modulo result = moduloService.findByIdWithSecciones(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(moduloRepository, times(1)).findByIdWithSecciones(1L);
    }

    @Test
    void save_ShouldReturnSavedModulo() {
        // Arrange
        Modulo newModulo = Modulo.builder()
                .nombre("Nuevo Módulo")
                .descripcion("Descripción")
                .orden(3)
                .build();

        when(moduloRepository.save(any(Modulo.class))).thenReturn(newModulo);

        // Act
        Modulo result = moduloService.save(newModulo);

        // Assert
        assertNotNull(result);
        assertEquals("Nuevo Módulo", result.getNombre());
        verify(moduloRepository, times(1)).save(newModulo);
    }

    @Test
    void deleteById_WhenModuloExists_ShouldDeleteModulo() {
        // Arrange
        when(moduloRepository.existsById(1L)).thenReturn(true);
        doNothing().when(moduloRepository).deleteById(1L);

        // Act
        moduloService.deleteById(1L);

        // Assert
        verify(moduloRepository, times(1)).existsById(1L);
        verify(moduloRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_WhenModuloNotExists_ShouldThrowModuloNotFoundException() {
        // Arrange
        when(moduloRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        ModuloNotFoundException exception = assertThrows(ModuloNotFoundException.class,
                () -> moduloService.deleteById(99L));

        assertEquals("Módulo no encontrado con ID: 99", exception.getMessage());
        verify(moduloRepository, times(1)).existsById(99L);
        verify(moduloRepository, never()).deleteById(anyLong());
    }
}