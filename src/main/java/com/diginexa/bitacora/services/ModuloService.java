package com.diginexa.bitacora.services;

import com.diginexa.bitacora.entities.Modulo;
import com.diginexa.bitacora.exceptions.domain.ModuloNotFoundException;
import com.diginexa.bitacora.repositories.ModuloRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuloService {

    private final ModuloRepository moduloRepository;

    public List<Modulo> findAll() {
        log.info("Buscando todos los módulos");
        return moduloRepository.findAllByOrderByOrdenAsc();
    }

    public Modulo findById(Long id) {
        log.info("Buscando módulo con ID: {}", id);
        return moduloRepository.findById(id)
                .orElseThrow(() -> new ModuloNotFoundException(id));
    }

    public Modulo findByIdWithSecciones(Long id) {
        log.info("Buscando módulo con secciones para ID: {}", id);
        return moduloRepository.findByIdWithSecciones(id)
                .orElseThrow(() -> new ModuloNotFoundException(id));
    }

    public Modulo save(Modulo modulo) {
        log.info("Guardando módulo: {}", modulo.getNombre());
        return moduloRepository.save(modulo);
    }

    public void deleteById(Long id) {
        log.info("Eliminando módulo con ID: {}", id);
        if (!moduloRepository.existsById(id)) {
            throw new ModuloNotFoundException(id);
        }
        moduloRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return moduloRepository.existsById(id);
    }
}
