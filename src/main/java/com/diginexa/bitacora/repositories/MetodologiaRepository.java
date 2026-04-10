package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Metodologia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodologiaRepository extends JpaRepository<Metodologia, Long> {

    List<Metodologia> findByActivoTrueOrderByLabelAsc();

    List<Metodologia> findAllByOrderByLabelAsc();

    boolean existsByValue(String value);
}
