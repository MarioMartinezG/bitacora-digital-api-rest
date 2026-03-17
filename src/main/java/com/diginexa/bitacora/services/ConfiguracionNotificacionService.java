package com.diginexa.bitacora.services;

import com.diginexa.bitacora.constants.ClaveConfiguracion;
import com.diginexa.bitacora.dtos.notificacion.ConfiguracionNotificacionDTO;
import com.diginexa.bitacora.entities.ConfiguracionNotificacion;
import com.diginexa.bitacora.exceptions.domain.ConfiguracionNotFoundException;
import com.diginexa.bitacora.repositories.ConfiguracionNotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConfiguracionNotificacionService {

    private final ConfiguracionNotificacionRepository repository;

    @Transactional(readOnly = true)
    public List<ConfiguracionNotificacionDTO> listarTodas() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConfiguracionNotificacionDTO obtenerPorClave(String clave) {
        return repository.findByClave(clave)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ConfiguracionNotFoundException(clave));
    }

    @Transactional(readOnly = true)
    public String obtenerValor(String clave) {
        return repository.findByClave(clave)
                .map(ConfiguracionNotificacion::getValor)
                .orElseThrow(() -> new ConfiguracionNotFoundException(clave));
    }

    @Transactional(readOnly = true)
    public String obtenerValor(String clave, String valorPorDefecto) {
        return repository.findByClave(clave)
                .map(ConfiguracionNotificacion::getValor)
                .orElse(valorPorDefecto);
    }

    public ConfiguracionNotificacionDTO actualizarValor(String clave, String nuevoValor) {
        ConfiguracionNotificacion config = repository.findByClave(clave)
                .orElseThrow(() -> new ConfiguracionNotFoundException(clave));

        config.setValor(nuevoValor);
        ConfiguracionNotificacion guardada = repository.save(config);

        log.info("Configuración '{}' actualizada a '{}'", clave, nuevoValor);
        return convertToDTO(guardada);
    }

    // Métodos de conveniencia para configuraciones específicas

    @Transactional(readOnly = true)
    public List<Integer> getDiasAnticipacionVencimiento() {
        String valor = obtenerValor(ClaveConfiguracion.DIAS_ANTICIPACION_VENCIMIENTO, "7,3,1");
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int getUmbralProgresoNotificacion() {
        String valor = obtenerValor(ClaveConfiguracion.UMBRAL_PROGRESO_NOTIFICACION, "80");
        return Integer.parseInt(valor);
    }

    @Transactional(readOnly = true)
    public boolean isEmailHabilitado() {
        String valor = obtenerValor(ClaveConfiguracion.EMAIL_HABILITADO, "true");
        return Boolean.parseBoolean(valor);
    }

    @Transactional(readOnly = true)
    public boolean isWebSocketHabilitado() {
        String valor = obtenerValor(ClaveConfiguracion.WEBSOCKET_HABILITADO, "true");
        return Boolean.parseBoolean(valor);
    }

    @Transactional(readOnly = true)
    public List<Integer> getDiasDemoraCoordinador() {
        String valor = obtenerValor(ClaveConfiguracion.DIAS_DEMORA_COORDINADOR, "7,3,1");
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Integer> getUmbralesCompletitudCoordinador() {
        String valor = obtenerValor(ClaveConfiguracion.UMBRALES_COMPLETITUD_COORDINADOR, "25,50,75");
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private ConfiguracionNotificacionDTO convertToDTO(ConfiguracionNotificacion entity) {
        return ConfiguracionNotificacionDTO.builder()
                .id(entity.getId())
                .clave(entity.getClave())
                .valor(entity.getValor())
                .descripcion(entity.getDescripcion())
                .tipoDato(entity.getTipoDato())
                .build();
    }
}
