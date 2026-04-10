package com.diginexa.bitacora.entities;

import com.diginexa.bitacora.annotations.JsonbMapConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entidad para almacenar respuestas de formularios de secciones.
 * Cada sección tiene un único registro por usuario con todas las respuestas en JSONB.
 */
@Entity
@Table(name = "respuestas_seccion", schema = "teia",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "seccion_codigo"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaSeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "seccion_codigo", nullable = false, length = 100)
    private String seccionCodigo;

    @Column(columnDefinition = "JSONB")
    @Convert(converter = JsonbMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private Map<String, Object> datos = new HashMap<>();

    @Column(name = "estado_avance", length = 20)
    @Builder.Default
    private String estadoAvance = "sin_avances";

    @Column(name = "fecha_creacion")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
