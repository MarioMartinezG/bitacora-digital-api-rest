package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.AlertaSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaSistemaRepository extends JpaRepository<AlertaSistema, Integer> {

    List<AlertaSistema> findByResueltaFalseOrderByFechaCreacionDesc();

    List<AlertaSistema> findByTipoAndResueltaFalse(String tipo);

    long countByResueltaFalse();
}
