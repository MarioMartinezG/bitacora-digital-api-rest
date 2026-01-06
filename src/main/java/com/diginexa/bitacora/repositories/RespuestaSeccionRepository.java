package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.RespuestaSeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RespuestaSeccionRepository extends JpaRepository<RespuestaSeccion, Long> {

    Optional<RespuestaSeccion> findByUsuarioIdAndSeccionCodigo(Integer usuarioId, String seccionCodigo);

    List<RespuestaSeccion> findByUsuarioId(Integer usuarioId);

    List<RespuestaSeccion> findBySeccionCodigo(String seccionCodigo);

    boolean existsByUsuarioIdAndSeccionCodigo(Integer usuarioId, String seccionCodigo);

    void deleteByUsuarioIdAndSeccionCodigo(Integer usuarioId, String seccionCodigo);
}
