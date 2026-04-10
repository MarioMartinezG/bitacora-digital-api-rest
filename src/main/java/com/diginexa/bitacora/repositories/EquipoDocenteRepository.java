package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.EquipoDocente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoDocenteRepository extends JpaRepository<EquipoDocente, Long> {

    List<EquipoDocente> findByUsuarioIdOrderByOrdenAsc(Integer usuarioId);

    int countByUsuarioId(Integer usuarioId);

    void deleteByUsuarioId(Integer usuarioId);
}
