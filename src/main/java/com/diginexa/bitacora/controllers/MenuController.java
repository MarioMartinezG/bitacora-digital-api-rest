package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.MenuDTO;
import com.diginexa.bitacora.services.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/{role}")
    public ResponseEntity<List<MenuDTO>> getMenu(@PathVariable Long role) {
        return ResponseEntity.ok(menuService.getMenuByRole(role));
    }
}