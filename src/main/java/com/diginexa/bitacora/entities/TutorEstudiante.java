package com.diginexa.bitacora.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad para la relación de asignación entre tutores y estudiantes.
 * Un tutor puede tener múltiples estudiantes asignados.
 */
@Entity
@Table(name = "tutor_estudiante", schema = "teia",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tutor_id", "estudiante_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorEstudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutor_id", nullable = false)
    private Integer tutorId;

    @Column(name = "estudiante_id", nullable = false)
    private Integer estudianteId;

    @Column(name = "fecha_asignacion")
    @CreationTimestamp
    private LocalDateTime fechaAsignacion;

    @Builder.Default
    private Boolean activo = true;

    // Relaciones para facilitar consultas con joins
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", insertable = false, updatable = false)
    private Usuario tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", insertable = false, updatable = false)
    private Usuario estudiante;
}
