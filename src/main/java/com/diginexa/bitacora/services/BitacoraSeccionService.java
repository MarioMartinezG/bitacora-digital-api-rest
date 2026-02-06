package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.GuardarSeccionRequest;
import com.diginexa.bitacora.dtos.bitacora.ProgresoUsuarioDTO;
import com.diginexa.bitacora.dtos.bitacora.RespuestaSeccionDTO;
import com.diginexa.bitacora.entities.RespuestaSeccion;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.repositories.RespuestaSeccionRepository;
import com.diginexa.bitacora.repositories.TutorEstudianteRepository;
import com.diginexa.bitacora.repositories.UsuarioRepository;
import com.diginexa.bitacora.validation.SeccionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BitacoraSeccionService {

    private final RespuestaSeccionRepository respuestaRepository;
    private final SeccionValidator validator;
    private final ProgresoService progresoService;
    private final NotificacionEventPublisher eventPublisher;
    private final ConfiguracionNotificacionService configuracionService;
    private final TutorEstudianteRepository tutorEstudianteRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public RespuestaSeccionDTO obtenerRespuesta(Integer usuarioId, String seccionCodigo) {
        log.debug("Obteniendo respuesta para usuario {} y sección {}", usuarioId, seccionCodigo);

        Optional<RespuestaSeccion> respuesta = respuestaRepository
            .findByUsuarioIdAndSeccionCodigo(usuarioId, seccionCodigo);

        return respuesta.map(this::convertToDTO)
            .orElse(RespuestaSeccionDTO.builder()
                .usuarioId(usuarioId)
                .seccionCodigo(seccionCodigo)
                .datos(new HashMap<>())
                .estadoAvance("sin_avances")
                .build());
    }

    public RespuestaSeccionDTO guardarRespuesta(GuardarSeccionRequest request) {
        log.info("Guardando respuesta para usuario {} en sección {}",
                 request.getUsuarioId(), request.getSeccionCodigo());

        // Obtener progreso anterior para comparar después del guardado
        ProgresoUsuarioDTO progresoAnterior = progresoService.obtenerProgresoCompleto(request.getUsuarioId());
        int porcentajeAnterior = progresoAnterior.getPorcentajeTotal();

        // Validar datos según tipo de sección (validación básica)
        List<String> errores = validator.validarSinExcepcion(
            request.getSeccionCodigo(),
            request.getDatos()
        );

        if (!errores.isEmpty()) {
            log.warn("Validación con advertencias: {}", errores);
            // No lanzamos excepción, solo logueamos - permitimos guardado parcial
        }

        // Buscar o crear respuesta
        RespuestaSeccion respuesta = respuestaRepository
            .findByUsuarioIdAndSeccionCodigo(request.getUsuarioId(), request.getSeccionCodigo())
            .orElse(RespuestaSeccion.builder()
                .usuarioId(request.getUsuarioId())
                .seccionCodigo(request.getSeccionCodigo())
                .build());

        // Actualizar datos
        respuesta.setDatos(request.getDatos());

        // Calcular estado si no se proporciona
        String estado = request.getEstadoAvance();
        if (estado == null || estado.isEmpty()) {
            estado = calcularEstadoAvance(request.getSeccionCodigo(), request.getDatos());
        }
        respuesta.setEstadoAvance(estado);

        // Guardar
        RespuestaSeccion guardada = respuestaRepository.save(respuesta);
        log.info("Respuesta guardada con ID: {}", guardada.getId());

        // Actualizar progreso
        progresoService.actualizarEstadoSeccion(
            request.getUsuarioId(),
            request.getSeccionCodigo(),
            estado,
            request.getProgresoPorcentaje()
        );

        // Verificar si se alcanzó el umbral de progreso para notificar al tutor
        verificarYNotificarUmbral(request.getUsuarioId(), porcentajeAnterior);

        return convertToDTO(guardada);
    }

    /**
     * Verifica si el estudiante alcanzó el umbral de progreso configurado
     * y notifica a su tutor asignado.
     */
    private void verificarYNotificarUmbral(Integer estudianteId, int porcentajeAnterior) {
        try {
            // Obtener umbral configurado
            int umbral = configuracionService.getUmbralProgresoNotificacion();

            // Obtener progreso actual después del guardado
            ProgresoUsuarioDTO progresoActual = progresoService.obtenerProgresoCompleto(estudianteId);
            int porcentajeActual = progresoActual.getPorcentajeTotal();

            log.debug("Verificando umbral: anterior={}%, actual={}%, umbral={}%",
                     porcentajeAnterior, porcentajeActual, umbral);

            // Verificar si cruzó el umbral (estaba debajo y ahora está arriba o igual)
            if (porcentajeAnterior < umbral && porcentajeActual >= umbral) {
                log.info("Estudiante {} alcanzó el umbral de {}% (progreso actual: {}%)",
                        estudianteId, umbral, porcentajeActual);

                // Buscar tutor asignado
                Optional<Usuario> tutorOpt = tutorEstudianteRepository.findTutorByEstudianteId(estudianteId);

                if (tutorOpt.isPresent()) {
                    Usuario tutor = tutorOpt.get();

                    // Obtener nombre del estudiante
                    String nombreEstudiante = usuarioRepository.findById(estudianteId)
                        .map(Usuario::getNombre)
                        .orElse("Estudiante #" + estudianteId);

                    // Publicar evento de umbral alcanzado
                    eventPublisher.publicarUmbralAlcanzado(
                        tutor.getId(),
                        estudianteId,
                        nombreEstudiante,
                        "progreso-general",  // Sección especial para progreso general
                        "Progreso General del Curso",
                        porcentajeActual
                    );

                    log.info("Notificación de umbral enviada al tutor {} para estudiante {}",
                            tutor.getId(), estudianteId);
                } else {
                    log.warn("Estudiante {} no tiene tutor asignado, no se enviará notificación de umbral",
                            estudianteId);
                }
            }
        } catch (Exception e) {
            // No fallar el guardado si hay error en la notificación
            log.error("Error al verificar/notificar umbral para estudiante {}: {}",
                     estudianteId, e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, RespuestaSeccionDTO> obtenerTodasRespuestas(Integer usuarioId) {
        log.debug("Obteniendo todas las respuestas para usuario {}", usuarioId);

        List<RespuestaSeccion> respuestas = respuestaRepository.findByUsuarioId(usuarioId);

        Map<String, RespuestaSeccionDTO> resultado = new HashMap<>();
        for (RespuestaSeccion r : respuestas) {
            resultado.put(r.getSeccionCodigo(), convertToDTO(r));
        }

        return resultado;
    }

    public void eliminarRespuesta(Integer usuarioId, String seccionCodigo) {
        log.info("Eliminando respuesta para usuario {} en sección {}", usuarioId, seccionCodigo);
        respuestaRepository.deleteByUsuarioIdAndSeccionCodigo(usuarioId, seccionCodigo);
    }

    private String calcularEstadoAvance(String seccionCodigo, Map<String, Object> datos) {
        if (datos == null || datos.isEmpty()) {
            return "sin_avances";
        }

        long camposLlenos = datos.values().stream()
            .filter(v -> v != null && !v.toString().trim().isEmpty())
            .count();

        if (camposLlenos == 0) {
            return "sin_avances";
        } else if (camposLlenos < datos.size()) {
            return "en_desarrollo";
        } else {
            return "completado";
        }
    }

    private RespuestaSeccionDTO convertToDTO(RespuestaSeccion entidad) {
        // Obtener datos de progreso para incluir estadoProfesor y porcentaje
        var progreso = progresoService.obtenerProgresoSeccion(
            entidad.getUsuarioId(),
            entidad.getSeccionCodigo()
        );

        return RespuestaSeccionDTO.builder()
            .id(entidad.getId())
            .usuarioId(entidad.getUsuarioId())
            .seccionCodigo(entidad.getSeccionCodigo())
            .datos(entidad.getDatos())
            .estadoAvance(entidad.getEstadoAvance())
            .estadoProfesor(progreso.getEstadoProfesor())
            .progresoPorcentaje(progreso.getPorcentaje())
            .fechaActualizacion(entidad.getFechaActualizacion())
            .build();
    }
}
