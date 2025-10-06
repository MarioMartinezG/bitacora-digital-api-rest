package com.diginexa.bitacora.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "menu_items", schema = "teia")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "label", nullable = false, length = 150)
    private String label;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "router_link", length = 200)
    private String routerLink;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    // Relación muchos a uno con Menu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    // Relación muchos a muchos con Roles
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "menu_item_roles",
            schema = "teia",
            joinColumns = @JoinColumn(name = "menu_item_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private List<Rol> roles = new ArrayList<>();
}

