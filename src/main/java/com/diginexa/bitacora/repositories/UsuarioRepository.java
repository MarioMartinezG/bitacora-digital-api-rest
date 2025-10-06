package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);

    @Query("SELECT u FROM Usuario u WHERE SUBSTRING(u.correo, 1, LOCATE('@', u.correo) - 1) = :username")
    Optional<Usuario> findByUsername(@Param("username") String username);
}
