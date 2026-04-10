package com.diginexa.bitacora.entities;

import com.diginexa.bitacora.annotations.JsonbListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad para el CRUD de temas y subtemas del contenido de la asignatura.
 * Cada tema es un registro con sus subtemas almacenados en JSONB.
 */
@Entity
@Table(name = "temas_contenido", schema = "teia",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "numero_tema"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemaContenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "numero_tema", nullable = false)
    private Integer numeroTema;

    @Column(name = "nombre_tema", nullable = false, length = 500)
    private String nombreTema;

    @Column(columnDefinition = "JSONB")
    @Convert(converter = JsonbListConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> subtemas = new ArrayList<>();

    @Column(name = "fecha_creacion")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
