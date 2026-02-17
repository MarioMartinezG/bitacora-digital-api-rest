package com.diginexa.bitacora.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "momento_secciones", schema = "teia",
       uniqueConstraints = @UniqueConstraint(columnNames = {"momento_id", "seccion_codigo"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomentoSeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "momento_id", nullable = false)
    private Long momentoId;

    @Column(name = "seccion_codigo", nullable = false, length = 100)
    private String seccionCodigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "momento_id", insertable = false, updatable = false)
    private Momento momento;
}
