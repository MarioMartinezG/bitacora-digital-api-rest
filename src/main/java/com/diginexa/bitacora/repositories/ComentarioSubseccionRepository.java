package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.ComentarioSubseccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioSubseccionRepository extends JpaRepository<ComentarioSubseccion, Long> {

    List<ComentarioSubseccion> findByEstudianteIdAndSeccionCodigoAndSubseccionCodigoOrderByFechaCreacionDesc(
            Integer estudianteId, String seccionCodigo, String subseccionCodigo);

    List<ComentarioSubseccion> findByEstudianteIdAndSeccionCodigoOrderByFechaCreacionDesc(
            Integer estudianteId, String seccionCodigo);

    long countByEstudianteIdAndSeccionCodigoAndSubseccionCodigo(
            Integer estudianteId, String seccionCodigo, String subseccionCodigo);
}
