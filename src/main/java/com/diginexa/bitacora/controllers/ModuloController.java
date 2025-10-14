package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.entities.Modulo;
import com.diginexa.bitacora.services.ModuloService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Deprecated
@RestController
@RequestMapping("/api/modulos")
@RequiredArgsConstructor
@Slf4j
public class ModuloController {

    private final ModuloService moduloService;

    @GetMapping
    public ResponseEntity<List<Modulo>> getAllModulos() {
        log.info("Solicitando todos los módulos");
        List<Modulo> modulos = moduloService.findAll();
        return ResponseEntity.ok(modulos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Modulo> getModuloById(@PathVariable Long id) {
        log.info("Solicitando módulo con ID: {}", id);
        Modulo modulo = moduloService.findById(id);
        return ResponseEntity.ok(modulo);
    }

    @GetMapping("/{id}/secciones")
    public ResponseEntity<Modulo> getModuloWithSecciones(@PathVariable Long id) {
        log.info("Solicitando módulo con secciones para ID: {}", id);
        Modulo modulo = moduloService.findByIdWithSecciones(id);
        log.info("Secciones encontradas para el modulo: {} {}", id, modulo);
        return ResponseEntity.ok(modulo);
    }

    @PostMapping
    public ResponseEntity<Modulo> createModulo(@RequestBody @Valid Modulo modulo) {
        log.info("Creando nuevo módulo: {}", modulo.getNombre());
        Modulo savedModulo = moduloService.save(modulo);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedModulo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Modulo> updateModulo(@PathVariable Long id, @RequestBody @Valid Modulo modulo) {
        log.info("Actualizando módulo con ID: {}", id);
        modulo.setId(id);
        Modulo updatedModulo = moduloService.save(modulo);
        return ResponseEntity.ok(updatedModulo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModulo(@PathVariable Long id) {
        log.info("Eliminando módulo con ID: {}", id);
        moduloService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
