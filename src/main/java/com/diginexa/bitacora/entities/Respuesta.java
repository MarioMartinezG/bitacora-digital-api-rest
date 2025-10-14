package com.diginexa.bitacora.entities;

import com.diginexa.bitacora.annotations.JsonbMapConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "respuestas", schema = "teia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Respuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seccion_id")
    @ToString.Exclude
    private Seccion seccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campo_id")
    @ToString.Exclude
    private CampoSeccion campo;

    @Column(name = "respuesta_texto", columnDefinition = "TEXT")
    private String respuestaTexto;

    @Column(columnDefinition = "JSONB")
    @Convert(converter = JsonbMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> respuestaJson;

    @Column(name = "estado_avance")
    private String estadoAvance;

    @Column(name = "fecha_creacion")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
