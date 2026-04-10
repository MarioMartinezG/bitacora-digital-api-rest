package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);

    List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(Integer usuarioId);

    List<Notificacion> findByUsuarioIdAndTipoOrderByFechaCreacionDesc(Integer usuarioId, String tipo);

    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.usuarioId = :usuarioId AND n.leida = false")
    Long contarNoLeidas(@Param("usuarioId") Integer usuarioId);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true, n.fechaLectura = :fecha WHERE n.id = :id")
    void marcarComoLeida(@Param("id") Long id, @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true, n.fechaLectura = :fecha WHERE n.usuarioId = :usuarioId AND n.leida = false")
    void marcarTodasComoLeidas(@Param("usuarioId") Integer usuarioId, @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query("UPDATE Notificacion n SET n.entregadaEmail = true WHERE n.id = :id")
    void marcarComoEntregadaEmail(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Notificacion n SET n.entregadaWebsocket = true WHERE n.id = :id")
    void marcarComoEntregadaWebsocket(@Param("id") Long id);

    List<Notificacion> findByUsuarioIdAndPrioridadOrderByFechaCreacionDesc(Integer usuarioId, String prioridad);
}
