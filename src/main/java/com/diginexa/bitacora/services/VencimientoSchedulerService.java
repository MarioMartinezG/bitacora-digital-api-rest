package com.diginexa.bitacora.services;

import com.diginexa.bitacora.entities.CalendarioModulo;
import com.diginexa.bitacora.entities.ProgresoSeccion;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.repositories.CalendarioModuloRepository;
import com.diginexa.bitacora.repositories.ProgresoSeccionRepository;
import com.diginexa.bitacora.repositories.TutorEstudianteRepository;
import com.diginexa.bitacora.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Servicio programado para verificar vencimientos y enviar notificaciones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VencimientoSchedulerService {

    private final CalendarioModuloRepository calendarioRepository;
    private final ProgresoSeccionRepository progresoRepository;
    private final TutorEstudianteRepository tutorEstudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracionNotificacionService configService;
    private final NotificacionEventPublisher eventPublisher;

    /**
     * Job programado que se ejecuta diariamente a las 8:00 AM.
     * Verifica los vencimientos y envía notificaciones según la configuración.
     */
    @Scheduled(cron = "${notificaciones.scheduler.cron:0 0 8 * * ?}")
    @Transactional(readOnly = true)
    public void verificarVencimientos() {
        log.info("Iniciando verificación de vencimientos programada");

        List<Integer> diasAnticipacion = configService.getDiasAnticipacionVencimiento();
        LocalDate hoy = LocalDate.now();

        // Obtener todos los calendarios activos
        List<CalendarioModulo> calendarios = calendarioRepository.findByActivoTrue();

        for (CalendarioModulo calendario : calendarios) {
            int diasRestantes = (int) ChronoUnit.DAYS.between(hoy, calendario.getFechaLimite());

            // Verificar si hoy corresponde a alguno de los días de anticipación configurados
            if (diasAnticipacion.contains(diasRestantes) || diasRestantes <= 0) {
                procesarVencimientosParaSeccion(calendario, diasRestantes);
            }
        }

        log.info("Verificación de vencimientos completada");
    }

    /**
     * Ejecuta la verificación de vencimientos manualmente (para testing o triggers manuales).
     */
    @Transactional(readOnly = true)
    public void ejecutarVerificacionManual() {
        log.info("Ejecutando verificación de vencimientos manual");
        verificarVencimientos();
    }

    private void procesarVencimientosParaSeccion(CalendarioModulo calendario, int diasRestantes) {
        String seccionCodigo = calendario.getSeccionCodigo();
        log.debug("Procesando vencimientos para sección: {} ({} días restantes)",
                seccionCodigo, diasRestantes);

        // Obtener todos los estudiantes
        List<Usuario> estudiantes = obtenerEstudiantes();

        for (Usuario estudiante : estudiantes) {
            String estadoProgreso = obtenerEstadoProgreso(estudiante.getId(), seccionCodigo);

            // Solo notificar si no está completado
            if (!"completado".equals(estadoProgreso)) {
                // Notificar al estudiante
                notificarEstudiante(estudiante, calendario, diasRestantes, estadoProgreso);

                // Notificar al tutor asignado
                notificarTutor(estudiante, calendario, diasRestantes, estadoProgreso);
            }
        }
    }

    private List<Usuario> obtenerEstudiantes() {
        // Buscar usuarios con rol ESTUDIANTE
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() != null && "ESTUDIANTE".equalsIgnoreCase(u.getRol().getNombre()))
                .toList();
    }

    private String obtenerEstadoProgreso(Integer usuarioId, String seccionCodigo) {
        return progresoRepository.findByUsuarioIdAndSeccionCodigo(usuarioId, seccionCodigo)
                .map(ProgresoSeccion::getEstado)
                .orElse("sin_avances");
    }

    private void notificarEstudiante(
            Usuario estudiante,
            CalendarioModulo calendario,
            int diasRestantes,
            String estadoProgreso) {

        eventPublisher.publicarVencimientoEstudiante(
                estudiante.getId(),
                calendario.getSeccionCodigo(),
                calendario.getNombreModulo(),
                calendario.getFechaLimite(),
                diasRestantes,
                estadoProgreso
        );
    }

    private void notificarTutor(
            Usuario estudiante,
            CalendarioModulo calendario,
            int diasRestantes,
            String estadoProgreso) {

        Optional<Usuario> tutorOpt = tutorEstudianteRepository.findTutorByEstudianteId(estudiante.getId());

        tutorOpt.ifPresent(tutor -> {
            eventPublisher.publicarVencimientoTutor(
                    tutor.getId(),
                    estudiante.getId(),
                    estudiante.getNombre(),
                    calendario.getSeccionCodigo(),
                    calendario.getNombreModulo(),
                    calendario.getFechaLimite(),
                    diasRestantes,
                    estadoProgreso
            );
        });
    }
}
