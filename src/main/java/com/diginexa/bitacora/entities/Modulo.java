package com.diginexa.bitacora.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modulos", schema = "teia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "total_secciones")
    private Integer totalSecciones;

    @Column(name = "tipo_estructura")
    private String tipoEstructura;

    @Column(columnDefinition = "TEXT")
    private String instrucciones;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @OneToMany(mappedBy = "modulo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    @ToString.Exclude
    @JsonIgnore
    private List<Seccion> secciones;
}
