package com.diginexa.bitacora.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios_subseccion", schema = "teia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComentarioSubseccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutor_id", nullable = false)
    private Integer tutorId;

    @Column(name = "estudiante_id", nullable = false)
    private Integer estudianteId;

    @Column(name = "seccion_codigo", nullable = false, length = 100)
    private String seccionCodigo;

    @Column(name = "subseccion_codigo", nullable = false, length = 100)
    private String subseccionCodigo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comentario;

    @Column(name = "fecha_creacion")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", insertable = false, updatable = false)
    private Usuario tutor;
}
