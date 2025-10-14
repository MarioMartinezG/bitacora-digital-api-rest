package com.diginexa.bitacora.unit;

import com.diginexa.bitacora.dtos.SeccionConCamposDTO;
import com.diginexa.bitacora.entities.CampoSeccion;
import com.diginexa.bitacora.entities.Modulo;
import com.diginexa.bitacora.entities.Seccion;
import com.diginexa.bitacora.exceptions.domain.ModuloNotFoundException;
import com.diginexa.bitacora.exceptions.domain.SeccionNotFoundException;
import com.diginexa.bitacora.repositories.ModuloRepository;
import com.diginexa.bitacora.repositories.SeccionRepository;
import com.diginexa.bitacora.services.SeccionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeccionServiceTest {

    @Mock
    private SeccionRepository seccionRepository;

    @Mock
    private ModuloRepository moduloRepository;

    @InjectMocks
    private SeccionService seccionService;

    private Modulo modulo;
    private Seccion seccion1;
    private Seccion seccion2;
    private CampoSeccion campo1;

    @BeforeEach
    void setUp() {
        modulo = Modulo.builder()
                .id(2L)
                .nombre("Módulo 2: Factores situacionales")
                .build();

        campo1 = CampoSeccion.builder()
                .id(1)
                .label("¿El curso hace parte de un programa de pregrado, o posgrado?")
                .tipoCampo("seleccion")
                .opciones(Arrays.asList("Pregrado", "Posgrado"))
                .esRequerido(true)
                .orden(1)
                .build();

        seccion1 = Seccion.builder()
                .id(4)
                .modulo(modulo)
                .nombre("Contexto Específico")
                .tipoSeccion("formulario")
                .orden(1)
                .campos(Collections.singletonList(campo1))
                .build();

        seccion2 = Seccion.builder()
                .id(5)
                .modulo(modulo)
                .nombre("Características de los estudiantes")
                .tipoSeccion("formulario")
                .orden(2)
                .campos(List.of())
                .build();

        campo1.setSeccion(seccion1);
    }

    @Test
    void findByModuloId_WhenModuloExists_ShouldReturnSecciones() {
        // Arrange
        when(moduloRepository.existsById(2L)).thenReturn(true);
        when(seccionRepository.findByModuloIdWithCampos(2L)).thenReturn(Arrays.asList(seccion1, seccion2));

        // Act
        List<Seccion> result = seccionService.findByModuloId(2L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Contexto Específico", result.get(0).getNombre());
        verify(moduloRepository, times(1)).existsById(2L);
        verify(seccionRepository, times(1)).findByModuloIdWithCampos(2L);
    }

    @Test
    void findByModuloId_WhenModuloNotExists_ShouldThrowModuloNotFoundException() {
        // Arrange
        when(moduloRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        ModuloNotFoundException exception = assertThrows(ModuloNotFoundException.class,
                () -> seccionService.findByModuloId(99L));

        assertEquals("Módulo no encontrado con ID: 99", exception.getMessage());
        verify(moduloRepository, times(1)).existsById(99L);
        verify(seccionRepository, never()).findByModuloIdWithCampos(anyLong());
    }

    @Test
    void findByIdWithCampos_WhenSeccionExists_ShouldReturnSeccionWithCampos() {
        // Arrange
        when(seccionRepository.findByIdWithCampos(4L)).thenReturn(Optional.of(seccion1));

        // Act
        Seccion result = seccionService.findByIdWithCampos(4L);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getId());
        assertEquals("Contexto Específico", result.getNombre());
        assertFalse(result.getCampos().isEmpty());
        assertEquals(1, result.getCampos().size());
        verify(seccionRepository, times(1)).findByIdWithCampos(4L);
    }

    @Test
    void findByIdWithCampos_WhenSeccionNotExists_ShouldThrowSeccionNotFoundException() {
        // Arrange
        when(seccionRepository.findByIdWithCampos(99L)).thenReturn(Optional.empty());

        // Act & Assert
        SeccionNotFoundException exception = assertThrows(SeccionNotFoundException.class,
                () -> seccionService.findByIdWithCampos(99L));

        assertEquals("Sección no encontrada con ID: 99", exception.getMessage());
        verify(seccionRepository, times(1)).findByIdWithCampos(99L);
    }

    @Test
    void findSeccionesByModuloId_ShouldReturnSeccionesDTO() {
        // Arrange
        when(moduloRepository.existsById(2L)).thenReturn(true);
        when(seccionRepository.findByModuloIdWithCampos(2L)).thenReturn(Arrays.asList(seccion1, seccion2));

        // Act
        List<SeccionConCamposDTO> result = seccionService.findSeccionesByModuloId(2L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        SeccionConCamposDTO primeraSeccion = result.get(0);
        assertEquals(4, primeraSeccion.getId());
        assertEquals("Contexto Específico", primeraSeccion.getNombre());
        assertFalse(primeraSeccion.getCampos().isEmpty());

        SeccionConCamposDTO segundaSeccion = result.get(1);
        assertEquals(5, segundaSeccion.getId());
        assertEquals("Características de los estudiantes", segundaSeccion.getNombre());
        assertTrue(segundaSeccion.getCampos().isEmpty());
    }

    @Test
    void findSeccionConCamposDTO_ShouldReturnSeccionDTO() {
        // Arrange
        when(seccionRepository.findByIdWithCampos(4L)).thenReturn(Optional.of(seccion1));

        // Act
        SeccionConCamposDTO result = seccionService.findSeccionConCamposDTO(4L);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getId());
        assertEquals("Contexto Específico", result.getNombre());
        assertEquals("formulario", result.getTipoSeccion());

        assertFalse(result.getCampos().isEmpty());
        SeccionConCamposDTO.CampoSeccionDTO primerCampo = result.getCampos().get(0);
        assertEquals(1, primerCampo.getId());
        assertEquals("¿El curso hace parte de un programa de pregrado, o posgrado?", primerCampo.getLabel());
        assertEquals("seleccion", primerCampo.getTipoCampo());
        assertTrue(primerCampo.getEsRequerido());
    }
}