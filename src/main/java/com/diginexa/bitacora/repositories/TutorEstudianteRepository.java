package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.TutorEstudiante;
import com.diginexa.bitacora.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorEstudianteRepository extends JpaRepository<TutorEstudiante, Long> {

    Optional<TutorEstudiante> findByEstudianteIdAndActivoTrue(Integer estudianteId);

    List<TutorEstudiante> findByTutorIdAndActivoTrue(Integer tutorId);

    @Query("SELECT te.estudiante FROM TutorEstudiante te WHERE te.tutorId = :tutorId AND te.activo = true")
    List<Usuario> findEstudiantesByTutorId(@Param("tutorId") Integer tutorId);

    @Query("SELECT te.tutor FROM TutorEstudiante te WHERE te.estudianteId = :estudianteId AND te.activo = true")
    Optional<Usuario> findTutorByEstudianteId(@Param("estudianteId") Integer estudianteId);

    boolean existsByTutorIdAndEstudianteIdAndActivoTrue(Integer tutorId, Integer estudianteId);

    boolean existsByEstudianteIdAndActivoTrue(Integer estudianteId);

    @Query("SELECT te FROM TutorEstudiante te WHERE te.tutorId = :tutorId AND te.estudianteId = :estudianteId")
    Optional<TutorEstudiante> findByTutorIdAndEstudianteId(
            @Param("tutorId") Integer tutorId,
            @Param("estudianteId") Integer estudianteId);
}
