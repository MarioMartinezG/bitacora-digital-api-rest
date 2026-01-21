package com.diginexa.bitacora.entities;

import com.diginexa.bitacora.annotations.JsonbMapConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entidad para el sistema de notificaciones.
 * Almacena notificaciones para estudiantes, tutores y coordinadores.
 */
@Entity
@Table(name = "notificaciones", schema = "teia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, length = 20)
    private String prioridad;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Column(name = "datos_adicionales", columnDefinition = "JSONB")
    @Convert(converter = JsonbMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private Map<String, Object> datosAdicionales = new HashMap<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean leida = false;

    @Column(name = "fecha_creacion")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @Column(name = "entregada_email")
    @Builder.Default
    private Boolean entregadaEmail = false;

    @Column(name = "entregada_websocket")
    @Builder.Default
    private Boolean entregadaWebsocket = false;
}
