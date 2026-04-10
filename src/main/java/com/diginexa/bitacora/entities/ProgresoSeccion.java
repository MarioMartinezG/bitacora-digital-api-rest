package com.diginexa.bitacora.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad para tracking del progreso por sección.
 * Usa códigos string en lugar de IDs numéricos para identificar secciones.
 */
@Entity
@Table(name = "progreso_secciones", schema = "teia",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "seccion_codigo"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgresoSeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "seccion_codigo", nullable = false, length = 100)
    private String seccionCodigo;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "sin_avances";

    @Column(name = "porcentaje_completado")
    @Builder.Default
    private Integer porcentajeCompletado = 0;

    /**
     * Estado asignado por el profesor/tutor.
     * Si está presente, tiene prioridad sobre el estado calculado automáticamente.
     * Permite al profesor marcar una sección como "en_desarrollo" aunque esté al 100%
     * si hay ajustes pendientes.
     */
    @Column(name = "estado_profesor", length = 20)
    private String estadoProfesor;

    /**
     * Indica si el tutor ya revisó esta sección del estudiante.
     * Por defecto false hasta que el tutor la marque como revisada.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean revisado = false;

    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
