package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @deprecated Desde versión 2.0. Los módulos ya no se usan como identificadores.
 * @see com.diginexa.bitacora.repositories.RespuestaSeccionRepository
 */
@Deprecated(since = "2.0", forRemoval = true)
@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Long> {
    List<Modulo> findAllByOrderByOrdenAsc();

    @Query("SELECT m FROM Modulo m LEFT JOIN FETCH m.secciones WHERE m.id = :id")
    Optional<Modulo> findByIdWithSecciones(@Param("id") Long id);
}
