package com.diginexa.bitacora.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad para solicitudes de sesión entre estudiantes y tutores.
 * Un estudiante puede solicitar una sesión con su tutor asignado.
 */
@Entity
@Table(name = "solicitudes_sesion", schema = "teia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estudiante_id", nullable = false)
    private Integer estudianteId;

    @Column(name = "tutor_id", nullable = false)
    private Integer tutorId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String motivo;

    @Column(length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Column(name = "fecha_solicitud")
    @CreationTimestamp
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Column(name = "notas_tutor", columnDefinition = "TEXT")
    private String notasTutor;

    // Relaciones opcionales para facilitar consultas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", insertable = false, updatable = false)
    private Usuario estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", insertable = false, updatable = false)
    private Usuario tutor;
}
