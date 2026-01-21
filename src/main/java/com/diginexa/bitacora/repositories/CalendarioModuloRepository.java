package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.CalendarioModulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarioModuloRepository extends JpaRepository<CalendarioModulo, Long> {

    Optional<CalendarioModulo> findBySeccionCodigo(String seccionCodigo);

    List<CalendarioModulo> findByActivoTrue();

    List<CalendarioModulo> findByActivoTrueOrderByFechaLimiteAsc();

    @Query("SELECT c FROM CalendarioModulo c WHERE c.activo = true AND c.fechaLimite BETWEEN :desde AND :hasta ORDER BY c.fechaLimite ASC")
    List<CalendarioModulo> findByFechaLimiteBetween(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("SELECT c FROM CalendarioModulo c WHERE c.activo = true AND c.fechaLimite <= :fecha ORDER BY c.fechaLimite ASC")
    List<CalendarioModulo> findByFechaLimiteAntesDe(@Param("fecha") LocalDate fecha);

    boolean existsBySeccionCodigo(String seccionCodigo);
}
