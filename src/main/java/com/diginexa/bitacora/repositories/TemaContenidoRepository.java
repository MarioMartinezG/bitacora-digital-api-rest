package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.TemaContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemaContenidoRepository extends JpaRepository<TemaContenido, Long> {

    List<TemaContenido> findByUsuarioIdOrderByNumeroTemaAsc(Integer usuarioId);

    Optional<TemaContenido> findByUsuarioIdAndNumeroTema(Integer usuarioId, Integer numeroTema);

    int countByUsuarioId(Integer usuarioId);

    void deleteByUsuarioId(Integer usuarioId);
}
