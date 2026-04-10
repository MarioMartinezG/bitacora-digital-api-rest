package com.diginexa.bitacora.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "alertas_sistema", schema = "teia")
public class AlertaSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "prioridad", nullable = false, length = 20)
    private String prioridad;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "usuario_referencia_id")
    private Integer usuarioReferenciaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_referencia_id", insertable = false, updatable = false)
    private Usuario usuarioReferencia;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_adicionales", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> datosAdicionales = Map.of();

    @Column(name = "resuelta", nullable = false)
    @Builder.Default
    private Boolean resuelta = false;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
