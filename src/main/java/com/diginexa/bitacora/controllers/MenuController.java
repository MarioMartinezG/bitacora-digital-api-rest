package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.MenuDTO;
import com.diginexa.bitacora.services.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('estudiante') or hasRole('tutor') or hasRole('admin')") // Protege el endpoint
    public ResponseEntity<List<MenuDTO>> getMenuByRole(@PathVariable Integer roleId) {
        List<MenuDTO> menu = menuService.getMenuByRole(roleId);
        return ResponseEntity.ok(menu);
    }

    // Alternativa: Obtener menú del usuario autenticado
    @GetMapping("/my-menu")
    public ResponseEntity<List<MenuDTO>> getMyMenu() {
        List<MenuDTO> menu = menuService.getMenuForCurrentUser();
        return ResponseEntity.ok(menu);
    }
}