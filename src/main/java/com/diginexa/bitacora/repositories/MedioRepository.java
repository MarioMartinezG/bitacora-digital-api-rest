package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Medio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedioRepository extends JpaRepository<Medio, Long> {

    List<Medio> findByActivoTrueOrderByCategoriaAscLabelAsc();

    List<Medio> findAllByOrderByCategoriaAscLabelAsc();

    boolean existsByValue(String value);
}
