package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Momento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MomentoRepository extends JpaRepository<Momento, Long> {

    List<Momento> findByActivoTrue();

    List<Momento> findByActivoTrueOrderByFechaLimiteAsc();
}
