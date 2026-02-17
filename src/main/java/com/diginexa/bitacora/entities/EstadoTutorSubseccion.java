package com.diginexa.bitacora.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "estado_tutor_subseccion", schema = "teia",
       uniqueConstraints = @UniqueConstraint(columnNames = {"estudiante_id", "seccion_codigo", "subseccion_codigo"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoTutorSubseccion {

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

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
