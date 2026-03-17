package com.diginexa.bitacora.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "medios", schema = "teia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String label;

    @Column(nullable = false, unique = true, length = 100)
    private String value;

    @Column(nullable = false, length = 20)
    private String categoria; // ESCRITOS | ORALES | PRACTICOS

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
