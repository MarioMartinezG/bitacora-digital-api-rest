package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.ProgresoSeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgresoSeccionRepository extends JpaRepository<ProgresoSeccion, Long> {

    Optional<ProgresoSeccion> findByUsuarioIdAndSeccionCodigo(Integer usuarioId, String seccionCodigo);

    List<ProgresoSeccion> findByUsuarioId(Integer usuarioId);

    List<ProgresoSeccion> findBySeccionCodigo(String seccionCodigo);

    boolean existsByUsuarioIdAndSeccionCodigo(Integer usuarioId, String seccionCodigo);

    void deleteByUsuarioIdAndSeccionCodigo(Integer usuarioId, String seccionCodigo);
}
