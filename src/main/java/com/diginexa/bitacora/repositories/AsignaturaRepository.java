package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignaturaRepository extends JpaRepository<Asignatura, Integer> {

    Optional<Asignatura> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Asignatura> findByActivaTrue();

    @Query("SELECT a FROM Asignatura a JOIN a.estudiantes e WHERE e.id = :estudianteId")
    List<Asignatura> findByEstudianteId(@Param("estudianteId") Integer estudianteId);

    @Query("SELECT a FROM Asignatura a JOIN a.tutores t WHERE t.id = :tutorId")
    List<Asignatura> findByTutorId(@Param("tutorId") Integer tutorId);

    @Query("SELECT COUNT(a) FROM Asignatura a WHERE a.activa = true")
    long countActivas();
}
