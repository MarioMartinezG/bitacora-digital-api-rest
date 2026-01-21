package com.diginexa.bitacora.services;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import com.diginexa.bitacora.dtos.notificacion.NotificacionDTO;
import com.diginexa.bitacora.dtos.notificacion.NotificacionResumenDTO;
import com.diginexa.bitacora.entities.Notificacion;
import com.diginexa.bitacora.exceptions.domain.NotificacionNotFoundException;
import com.diginexa.bitacora.repositories.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificacionService {

    private final NotificacionRepository repository;

    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerNotificaciones(Integer usuarioId) {
        log.debug("Obteniendo notificaciones para usuario {}", usuarioId);
        return repository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerNoLeidas(Integer usuarioId) {
        log.debug("Obteniendo notificaciones no leídas para usuario {}", usuarioId);
        return repository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerPorTipo(Integer usuarioId, String tipo) {
        return repository.findByUsuarioIdAndTipoOrderByFechaCreacionDesc(usuarioId, tipo)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NotificacionResumenDTO obtenerResumen(Integer usuarioId) {
        Long totalNoLeidas = repository.contarNoLeidas(usuarioId);

        List<Notificacion> noLeidas = repository
                .findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(usuarioId);

        long criticas = noLeidas.stream()
                .filter(n -> PrioridadNotificacion.CRITICO.name().equals(n.getPrioridad()))
                .count();
        long alertas = noLeidas.stream()
                .filter(n -> PrioridadNotificacion.ALERTA.name().equals(n.getPrioridad()))
                .count();
        long info = noLeidas.stream()
                .filter(n -> PrioridadNotificacion.INFO.name().equals(n.getPrioridad()))
                .count();

        return NotificacionResumenDTO.builder()
                .usuarioId(usuarioId)
                .totalNoLeidas(totalNoLeidas)
                .totalCriticas(criticas)
                .totalAlertas(alertas)
                .totalInfo(info)
                .build();
    }

    @Transactional(readOnly = true)
    public NotificacionDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new NotificacionNotFoundException(id));
    }

    public NotificacionDTO marcarComoLeida(Long id) {
        Notificacion notificacion = repository.findById(id)
                .orElseThrow(() -> new NotificacionNotFoundException(id));

        notificacion.setLeida(true);
        notificacion.setFechaLectura(LocalDateTime.now());

        log.info("Notificación {} marcada como leída", id);
        return convertToDTO(repository.save(notificacion));
    }

    public void marcarTodasComoLeidas(Integer usuarioId) {
        repository.marcarTodasComoLeidas(usuarioId, LocalDateTime.now());
        log.info("Todas las notificaciones del usuario {} marcadas como leídas", usuarioId);
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new NotificacionNotFoundException(id);
        }
        repository.deleteById(id);
        log.info("Notificación {} eliminada", id);
    }

    /**
     * Crea una nueva notificación.
     * Usado principalmente por los event listeners.
     */
    public Notificacion crearNotificacion(
            Integer usuarioId,
            TipoNotificacion tipo,
            PrioridadNotificacion prioridad,
            String titulo,
            String mensaje,
            Map<String, Object> datosAdicionales) {

        Notificacion notificacion = Notificacion.builder()
                .usuarioId(usuarioId)
                .tipo(tipo.name())
                .prioridad(prioridad.name())
                .titulo(titulo)
                .mensaje(mensaje)
                .datosAdicionales(datosAdicionales)
                .build();

        Notificacion guardada = repository.save(notificacion);
        log.info("Notificación creada: tipo={}, usuario={}, prioridad={}",
                tipo, usuarioId, prioridad);

        return guardada;
    }

    public void marcarComoEntregadaEmail(Long id) {
        repository.marcarComoEntregadaEmail(id);
    }

    public void marcarComoEntregadaWebsocket(Long id) {
        repository.marcarComoEntregadaWebsocket(id);
    }

    private NotificacionDTO convertToDTO(Notificacion entity) {
        String severity = "info";
        try {
            PrioridadNotificacion prioridad = PrioridadNotificacion.valueOf(entity.getPrioridad());
            severity = prioridad.getSeverity();
        } catch (IllegalArgumentException ignored) {
            // Usar severity por defecto si no se puede parsear
        }

        return NotificacionDTO.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .tipo(entity.getTipo())
                .prioridad(entity.getPrioridad())
                .severity(severity)
                .titulo(entity.getTitulo())
                .mensaje(entity.getMensaje())
                .datosAdicionales(entity.getDatosAdicionales())
                .leida(entity.getLeida())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaLectura(entity.getFechaLectura())
                .build();
    }
}
