package com.diginexa.bitacora.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tecnicas", schema = "teia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tecnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String label;

    @Column(nullable = false, unique = true, length = 100)
    private String value;

    @Column(nullable = false, length = 30)
    private String grupo; // ALUMNO_NO_INTERVIENE | ALUMNO_PARTICIPA

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
