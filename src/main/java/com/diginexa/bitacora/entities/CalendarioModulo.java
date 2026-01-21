package com.diginexa.bitacora.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad para el calendario de módulos con fechas límite.
 * Define cuándo debe completarse cada sección/módulo.
 */
@Entity
@Table(name = "calendario_modulos", schema = "teia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarioModulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seccion_codigo", nullable = false, unique = true, length = 100)
    private String seccionCodigo;

    @Column(name = "nombre_modulo", nullable = false, length = 200)
    private String nombreModulo;

    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
