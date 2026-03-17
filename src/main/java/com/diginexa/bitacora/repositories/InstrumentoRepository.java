package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Instrumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstrumentoRepository extends JpaRepository<Instrumento, Long> {

    List<Instrumento> findByActivoTrueOrderByLabelAsc();

    List<Instrumento> findAllByOrderByLabelAsc();

    boolean existsByValue(String value);
}
