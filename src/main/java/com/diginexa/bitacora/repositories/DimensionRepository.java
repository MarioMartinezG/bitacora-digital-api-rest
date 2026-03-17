package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DimensionRepository extends JpaRepository<Dimension, Long> {

    List<Dimension> findByActivoTrueOrderByNombreAsc();

    List<Dimension> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
