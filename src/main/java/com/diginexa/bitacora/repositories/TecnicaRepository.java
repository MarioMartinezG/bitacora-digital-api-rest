package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Tecnica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TecnicaRepository extends JpaRepository<Tecnica, Long> {

    List<Tecnica> findByActivoTrueOrderByGrupoAscLabelAsc();

    List<Tecnica> findAllByOrderByGrupoAscLabelAsc();

    boolean existsByValue(String value);
}
