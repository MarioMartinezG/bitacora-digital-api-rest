package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.EstadoTutorSubseccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoTutorSubseccionRepository extends JpaRepository<EstadoTutorSubseccion, Long> {

    List<EstadoTutorSubseccion> findByEstudianteIdAndSeccionCodigo(Integer estudianteId, String seccionCodigo);

    Optional<EstadoTutorSubseccion> findByEstudianteIdAndSeccionCodigoAndSubseccionCodigo(
            Integer estudianteId, String seccionCodigo, String subseccionCodigo);

    List<EstadoTutorSubseccion> findByEstudianteId(Integer estudianteId);
}
