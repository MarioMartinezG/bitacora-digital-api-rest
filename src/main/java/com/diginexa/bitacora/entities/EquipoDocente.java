package com.diginexa.bitacora.entities;

import com.diginexa.bitacora.annotations.JsonbListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad para el CRUD de miembros del equipo docente.
 * Cada miembro es un registro separado asociado a un usuario.
 */
@Entity
@Table(name = "equipo_docente", schema = "teia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 150)
    private String correo;

    @Column(length = 100)
    private String rol;

    @Column(length = 50)
    private String atencion;

    @Column(columnDefinition = "JSONB")
    @Convert(converter = JsonbListConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> dias = new ArrayList<>();

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(length = 200)
    private String horario;

    @Column(length = 200)
    private String sitio;

    @Builder.Default
    private Integer orden = 0;

    @Column(name = "fecha_creacion")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}
