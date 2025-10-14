package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.CampoSeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampoSeccionRepository extends JpaRepository<CampoSeccion, Long> {

    List<CampoSeccion> findBySeccionIdOrderByOrdenAsc(Long seccionId);

    @Query("SELECT c FROM CampoSeccion c WHERE c.seccion.modulo.id = :moduloId ORDER BY c.seccion.orden ASC, c.orden ASC")
    List<CampoSeccion> findByModuloId(@Param("moduloId") Long moduloId);

    @Query("SELECT c FROM CampoSeccion c JOIN FETCH c.seccion WHERE c.id = :id")
    Optional<CampoSeccion> findByIdWithSeccion(@Param("id") Long id);

    List<CampoSeccion> findByIdIn(List<Long> campoIds);

    @Query("SELECT c FROM CampoSeccion c WHERE c.seccion.id = :seccionId")
    List<CampoSeccion> findBySeccionId(@Param("seccionId") Long seccionId);

    @Query("SELECT COUNT(c) FROM CampoSeccion c WHERE c.seccion.id = :seccionId")
    Long countBySeccionId(@Param("seccionId") Long seccionId);
}
