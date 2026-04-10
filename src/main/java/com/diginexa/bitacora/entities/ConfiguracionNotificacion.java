package com.diginexa.bitacora.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad para configuración paramétrica del sistema de notificaciones.
 * Permite ajustar umbrales, días de anticipación y otros parámetros sin redesplegar.
 */
@Entity
@Table(name = "configuracion_notificaciones", schema = "teia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionNotificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String clave;

    @Column(nullable = false, length = 255)
    private String valor;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo_dato", length = 20)
    @Builder.Default
    private String tipoDato = "STRING";
}
