package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.SeccionConCamposDTO;
import com.diginexa.bitacora.services.SeccionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/secciones")
@RequiredArgsConstructor
@Slf4j
public class SeccionController {

    private final SeccionService seccionService;

    @GetMapping("/modulo/{moduloId}")
    public ResponseEntity<List<SeccionConCamposDTO>> getSeccionesByModulo(@PathVariable Long moduloId) {
        log.info("Solicitando secciones para módulo: {}", moduloId);
        List<SeccionConCamposDTO> secciones = seccionService.findSeccionesByModuloId(moduloId);
        return ResponseEntity.ok(secciones);
    }

    @GetMapping("/{seccionId}")
    public ResponseEntity<SeccionConCamposDTO> getSeccionConCampos(@PathVariable Long seccionId) {
        log.info("Solicitando sección con ID: {}", seccionId);
        SeccionConCamposDTO seccion = seccionService.findSeccionConCamposDTO(seccionId);
        return ResponseEntity.ok(seccion);
    }
}
