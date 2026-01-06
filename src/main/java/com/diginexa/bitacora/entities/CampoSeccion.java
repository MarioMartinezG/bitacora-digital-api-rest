package com.diginexa.bitacora.entities;

import com.diginexa.bitacora.annotations.JsonbListConverter;
import com.diginexa.bitacora.annotations.JsonbMapConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

/**
 * @deprecated Desde versión 2.0. La estructura de campos ahora es definida por el frontend.
 * Esta entidad será eliminada en una versión futura.
 * @see com.diginexa.bitacora.entities.RespuestaSeccion
 */
@Deprecated(since = "2.0", forRemoval = true)
@Entity
@Table(name = "campos_seccion", schema = "teia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class CampoSeccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seccion_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Seccion seccion;

    @Column(nullable = false)
    private String label;

    @Column(name = "tipo_campo", nullable = false)
    private String tipoCampo;

    @Column(columnDefinition = "JSONB")
    @Convert(converter = JsonbListConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> opciones;

    @Column(name = "es_requerido", columnDefinition = "boolean")
    private Boolean esRequerido;

    @Column(nullable = false)
    private Integer orden;

    @Column(columnDefinition = "JSONB")
    @Convert(converter = JsonbMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> configuracion;

    private String placeholder;
}
