package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.EquipoDocenteDTO;
import com.diginexa.bitacora.entities.EquipoDocente;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.repositories.EquipoDocenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EquipoDocenteService {

    private final EquipoDocenteRepository repository;

    @Transactional(readOnly = true)
    public List<EquipoDocenteDTO> listarPorUsuario(Integer usuarioId) {
        log.debug("Listando equipo docente para usuario {}", usuarioId);
        return repository.findByUsuarioIdOrderByOrdenAsc(usuarioId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public EquipoDocenteDTO agregar(Integer usuarioId, EquipoDocenteDTO dto) {
        log.info("Agregando miembro al equipo docente para usuario {}", usuarioId);

        int nuevoOrden = repository.countByUsuarioId(usuarioId);

        EquipoDocente entidad = EquipoDocente.builder()
            .usuarioId(usuarioId)
            .nombre(dto.getNombre())
            .correo(dto.getCorreo())
            .rol(dto.getRol())
            .atencion(dto.getAtencion())
            .dias(dto.getDias())
            .horaInicio(dto.getHoraInicio())
            .horaFin(dto.getHoraFin())
            .horario(dto.getHorario())
            .sitio(dto.getSitio())
            .orden(dto.getOrden() != null ? dto.getOrden() : nuevoOrden)
            .build();

        EquipoDocente guardado = repository.save(entidad);
        log.info("Miembro agregado con ID: {}", guardado.getId());

        return convertToDTO(guardado);
    }

    public EquipoDocenteDTO actualizar(Long id, EquipoDocenteDTO dto) {
        log.info("Actualizando miembro de equipo {}", id);

        EquipoDocente entidad = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Miembro de equipo no encontrado: " + id));

        entidad.setNombre(dto.getNombre());
        entidad.setCorreo(dto.getCorreo());
        entidad.setRol(dto.getRol());
        entidad.setAtencion(dto.getAtencion());
        entidad.setDias(dto.getDias());
        entidad.setHoraInicio(dto.getHoraInicio());
        entidad.setHoraFin(dto.getHoraFin());
        entidad.setHorario(dto.getHorario());
        entidad.setSitio(dto.getSitio());
        if (dto.getOrden() != null) {
            entidad.setOrden(dto.getOrden());
        }

        EquipoDocente guardado = repository.save(entidad);
        return convertToDTO(guardado);
    }

    public void eliminar(Long id) {
        log.info("Eliminando miembro de equipo {}", id);

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Miembro de equipo no encontrado: " + id);
        }

        repository.deleteById(id);
    }

    public List<EquipoDocenteDTO> reordenar(Integer usuarioId, List<Long> ordenIds) {
        log.info("Reordenando equipo docente para usuario {}", usuarioId);

        List<EquipoDocente> equipo = repository.findByUsuarioIdOrderByOrdenAsc(usuarioId);

        for (int i = 0; i < ordenIds.size(); i++) {
            Long id = ordenIds.get(i);
            for (EquipoDocente miembro : equipo) {
                if (miembro.getId().equals(id)) {
                    miembro.setOrden(i);
                    repository.save(miembro);
                    break;
                }
            }
        }

        return listarPorUsuario(usuarioId);
    }

    private EquipoDocenteDTO convertToDTO(EquipoDocente entidad) {
        return EquipoDocenteDTO.builder()
            .id(entidad.getId())
            .nombre(entidad.getNombre())
            .correo(entidad.getCorreo())
            .rol(entidad.getRol())
            .atencion(entidad.getAtencion())
            .dias(entidad.getDias())
            .horaInicio(entidad.getHoraInicio())
            .horaFin(entidad.getHoraFin())
            .horario(entidad.getHorario())
            .sitio(entidad.getSitio())
            .orden(entidad.getOrden())
            .build();
    }
}
