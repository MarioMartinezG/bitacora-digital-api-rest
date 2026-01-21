package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.SolicitudSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudSesionRepository extends JpaRepository<SolicitudSesion, Long> {

    List<SolicitudSesion> findByEstudianteIdOrderByFechaSolicitudDesc(Integer estudianteId);

    List<SolicitudSesion> findByTutorIdOrderByFechaSolicitudDesc(Integer tutorId);

    List<SolicitudSesion> findByTutorIdAndEstadoOrderByFechaSolicitudDesc(Integer tutorId, String estado);

    List<SolicitudSesion> findByEstudianteIdAndEstadoOrderByFechaSolicitudDesc(Integer estudianteId, String estado);

    List<SolicitudSesion> findByEstado(String estado);

    boolean existsByEstudianteIdAndTutorIdAndEstado(Integer estudianteId, Integer tutorId, String estado);
}
