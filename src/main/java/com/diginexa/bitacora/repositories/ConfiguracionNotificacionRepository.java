package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.ConfiguracionNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionNotificacionRepository extends JpaRepository<ConfiguracionNotificacion, Long> {

    Optional<ConfiguracionNotificacion> findByClave(String clave);

    boolean existsByClave(String clave);
}
