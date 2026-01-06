package com.diginexa.bitacora.entities;

import com.diginexa.bitacora.annotations.JsonbMapConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

/**
 * @deprecated Desde versión 2.0. Usar identificadores string de sección en lugar de entidades.
 * Esta entidad será eliminada en una versión futura.
 * @see com.diginexa.bitacora.entities.RespuestaSeccion
 * @see com.diginexa.bitacora.constants.SeccionCodigos
 */
@Deprecated(since = "2.0", forRemoval = true)
@Entity
@Table(name = "secciones", schema = "teia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Modulo modulo;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo_seccion", nullable = false)
    private String tipoSeccion;

    @Column(columnDefinition = "JSONB")
    @Convert(converter = JsonbMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> estructuraJson;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "tiene_estado")
    private Boolean tieneEstado;

    @Column(name = "es_obligatorio")
    private Boolean esObligatorio;

    @Column(columnDefinition = "JSONB")
    @Convert(converter = JsonbMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> configuracion;

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    @ToString.Exclude
    private List<CampoSeccion> campos;
}
