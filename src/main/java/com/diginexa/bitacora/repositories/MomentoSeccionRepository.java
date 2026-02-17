package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.MomentoSeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MomentoSeccionRepository extends JpaRepository<MomentoSeccion, Long> {

    List<MomentoSeccion> findByMomentoId(Long momentoId);

    List<MomentoSeccion> findBySeccionCodigo(String seccionCodigo);

    void deleteByMomentoId(Long momentoId);
}
