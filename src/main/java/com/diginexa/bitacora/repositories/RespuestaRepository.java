package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Respuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    @Query("SELECT r FROM Respuesta r WHERE r.usuarioId = :usuarioId AND r.seccion.id = :seccionId")
    List<Respuesta> findByUsuarioIdAndSeccionId(@Param("usuarioId") Integer usuarioId,
                                                @Param("seccionId") Long seccionId);

    @Query("SELECT r FROM Respuesta r WHERE r.usuarioId = :usuarioId AND r.seccion.modulo.id = :moduloId")
    List<Respuesta> findByUsuarioIdAndModuloId(@Param("usuarioId") Integer usuarioId,
                                               @Param("moduloId") Long moduloId);

    @Query("SELECT r FROM Respuesta r WHERE r.usuarioId = :usuarioId AND r.campo.id = :campoId")
    Optional<Respuesta> findByUsuarioIdAndCampoId(@Param("usuarioId") Integer usuarioId,
                                                  @Param("campoId") Long campoId);

    @Query("SELECT r FROM Respuesta r WHERE r.usuarioId = :usuarioId AND r.campo.id IN :campoIds")
    List<Respuesta> findByUsuarioIdAndCampoIdIn(@Param("usuarioId") Integer usuarioId,
                                                @Param("campoIds") List<Long> campoIds);

    @Modifying
    @Query("DELETE FROM Respuesta r WHERE r.usuarioId = :usuarioId AND r.campo.id = :campoId")
    void deleteByUsuarioIdAndCampoId(@Param("usuarioId") Integer usuarioId,
                                     @Param("campoId") Long campoId);

    @Query("SELECT COUNT(r) > 0 FROM Respuesta r WHERE r.usuarioId = :usuarioId AND r.campo.id = :campoId")
    boolean existsByUsuarioIdAndCampoId(@Param("usuarioId") Integer usuarioId,
                                        @Param("campoId") Long campoId);
}
