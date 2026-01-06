package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @deprecated Desde versión 2.0. Las secciones ahora se identifican por código string.
 * @see com.diginexa.bitacora.repositories.RespuestaSeccionRepository
 */
@Deprecated(since = "2.0", forRemoval = true)
@Repository
public interface SeccionRepository extends JpaRepository<Seccion, Long> {
    List<Seccion> findByModuloIdOrderByOrdenAsc(Long moduloId);

    @Query("SELECT s FROM Seccion s LEFT JOIN FETCH s.campos WHERE s.id = :id")
    Optional<Seccion> findByIdWithCampos(@Param("id") Long id);

    @Query("SELECT s FROM Seccion s LEFT JOIN FETCH s.campos WHERE s.modulo.id = :moduloId ORDER BY s.orden ASC")
    List<Seccion> findByModuloIdWithCampos(@Param("moduloId") Long moduloId);
}
