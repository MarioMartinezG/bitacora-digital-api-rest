package com.diginexa.bitacora.services;

import com.diginexa.bitacora.entities.Momento;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.repositories.MomentoRepository;
import com.diginexa.bitacora.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Servicio programado que verifica diariamente si algún estudiante
 * tiene un progreso tan bajo que le resulta imposible completar
 * la bitácora antes del vencimiento del último momento.
 * Cuando se detecta esa situación, notifica a todos los coordinadores.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertaRiesgoCoordinadorService {

    /**
     * Porcentaje máximo de avance que un estudiante puede lograr en un día.
     * Con 7 secciones y ~2 semanas de trabajo, 6.25 % diario es el ritmo
     * óptimo estimado (100 / 16 días hábiles ≈ 6.25).
     */
    private static final double TASA_AVANCE_DIARIA = 6.25;

    private final MomentoRepository momentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProgresoService progresoService;
    private final NotificacionEventPublisher eventPublisher;

    /**
     * Se ejecuta diariamente a las 8:05 AM (5 minutos después del scheduler de vencimientos).
     */
    @Scheduled(cron = "${notificaciones.scheduler.riesgo.cron:0 5 8 * * ?}")
    @Transactional(readOnly = true)
    public void verificarEstudiantesEnRiesgo() {
        log.info("Iniciando verificación de estudiantes en riesgo");

        // Obtener el último momento activo (fecha límite más tardía = fecha de entrega final)
        Optional<Momento> ultimoMomentoOpt = momentoRepository.findByActivoTrue()
                .stream()
                .max(Comparator.comparing(Momento::getFechaLimite));

        if (ultimoMomentoOpt.isEmpty()) {
            log.info("No hay momentos activos configurados; se omite la verificación de riesgo");
            return;
        }

        Momento ultimoMomento = ultimoMomentoOpt.get();
        LocalDate hoy = LocalDate.now();
        int diasRestantes = (int) ChronoUnit.DAYS.between(hoy, ultimoMomento.getFechaLimite());

        if (diasRestantes <= 0) {
            log.info("El último momento ya venció; se omite la verificación de riesgo");
            return;
        }

        List<Usuario> coordinadores = obtenerCoordinadores();
        if (coordinadores.isEmpty()) {
            log.warn("No se encontraron coordinadores para notificar");
            return;
        }

        List<Usuario> estudiantes = obtenerEstudiantes();

        for (Usuario estudiante : estudiantes) {
            int progresoActual = progresoService
                    .obtenerProgresoCompleto(estudiante.getId())
                    .getPorcentajeTotal();

            if (esImpossibleCompletar(progresoActual, diasRestantes)) {
                notificarCoordinadores(
                        coordinadores,
                        estudiante,
                        progresoActual,
                        diasRestantes,
                        ultimoMomento.getFechaLimite()
                );
            }
        }

        log.info("Verificación de estudiantes en riesgo completada");
    }

    /**
     * Determina si con el progreso actual y los días restantes es matemáticamente
     * imposible completar la bitácora al 100 %.
     *
     * @param progresoActual  porcentaje actual (0–100)
     * @param diasRestantes   días hasta el vencimiento del último momento
     * @return true si no puede llegar al 100 % aunque trabaje al ritmo óptimo
     */
    private boolean esImpossibleCompletar(int progresoActual, int diasRestantes) {
        double maxAlcanzable = progresoActual + (diasRestantes * TASA_AVANCE_DIARIA);
        return maxAlcanzable < 100.0;
    }

    private void notificarCoordinadores(
            List<Usuario> coordinadores,
            Usuario estudiante,
            int progresoActual,
            int diasRestantes,
            LocalDate fechaLimite) {

        String nombreEstudiante = estudiante.getNombre();
        log.info("Estudiante en riesgo: {} ({}%) — {} días restantes",
                nombreEstudiante, progresoActual, diasRestantes);

        for (Usuario coordinador : coordinadores) {
            eventPublisher.publicarEstudianteEnRiesgo(
                    coordinador.getId(),
                    estudiante.getId(),
                    nombreEstudiante,
                    progresoActual,
                    diasRestantes,
                    fechaLimite
            );
        }
    }

    private List<Usuario> obtenerEstudiantes() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles() != null &&
                        u.getRoles().stream().anyMatch(r -> "ESTUDIANTE".equalsIgnoreCase(r.getNombre())))
                .toList();
    }

    private List<Usuario> obtenerCoordinadores() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles() != null &&
                        u.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getNombre())))
                .toList();
    }
}
