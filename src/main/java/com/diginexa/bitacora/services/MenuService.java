package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.MenuDTO;
import com.diginexa.bitacora.dtos.MenuItemDTO;
import com.diginexa.bitacora.entities.Menu;
import com.diginexa.bitacora.entities.MenuItem;
import com.diginexa.bitacora.entities.Rol;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.exceptions.domain.MenuNotFoundException;
import com.diginexa.bitacora.exceptions.domain.RoleNotFoundException;
import com.diginexa.bitacora.exceptions.validation.DuplicateMenuException;
import com.diginexa.bitacora.exceptions.validation.DuplicateMenuItemException;
import com.diginexa.bitacora.repositories.MenuItemRepository;
import com.diginexa.bitacora.repositories.MenuRepository;
import com.diginexa.bitacora.repositories.RolRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final RolRepository rolRepository;
    private final UserService userService;

    public List<MenuDTO> getMenuByRole(Integer roleId) {
        // Validar que el rol existe
        Rol roleValidation = rolRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Rol con id " + roleId + " no existe"));

        // Obtener menús para el rol (usando la tabla pivote menu_roles)
        List<Menu> menus = menuRepository.findByRoleId(roleId);
        if (menus.isEmpty()) {
            throw new MenuNotFoundException("No se encontraron menús para el rol " + roleValidation.getNombre());
        }

        return buildMenuDTOs(menus, roleId);
    }

    @Transactional
    public Menu createMenu(Menu menu) {
        if (menuRepository.existsByLabel(menu.getLabel())) {
            throw new DuplicateMenuException("El menú '" + menu.getLabel() + "' ya existe");
        }
        return menuRepository.save(menu);
    }

    @Transactional
    public MenuItem createMenuItem(MenuItem item, Integer menuId) {
        if (menuItemRepository.existsByLabelAndMenuId(item.getLabel(), menuId)) {
            throw new DuplicateMenuItemException(
                    "El ítem de menú '" + item.getLabel() + "' ya existe en el menú con id " + menuId
            );
        }

        // Validar que el menú existe
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuNotFoundException("Menú con id " + menuId + " no encontrado"));

        item.setMenu(menu);
        return menuItemRepository.save(item);
    }

    public List<MenuDTO> getMenuForCurrentUser() {
        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Obtener el usuario completo con su rol
        Usuario usuario = userService.findByCorreo(username);

        // Obtener el ID del rol del usuario
        Integer roleId = Math.toIntExact(usuario.getRol().getId());

        // Usar el metodo existente para obtener el menú por rol
        return getMenuByRole(roleId);
    }

    private List<MenuDTO> buildMenuDTOs(List<Menu> menus, Integer roleId) {
        return menus.stream()
                .map(menu -> {
                    // Obtener items del menú que están permitidos para este rol
                    List<MenuItem> items = menuItemRepository.findByRoleIdAndMenuId(roleId, menu.getId());

                    List<MenuItemDTO> itemDTOs = items.stream()
                            .map(this::convertToMenuItemDTO)
                            .toList();

                    return convertToMenuDTO(menu, itemDTOs);
                })
                .toList();
    }

    private MenuDTO convertToMenuDTO(Menu menu, List<MenuItemDTO> items) {
        return MenuDTO.builder()
                .label(menu.getLabel())
                .icon(menu.getIcon())
                .routerLink(menu.getRouterLink())
                .items(items)
                .build();
    }

    private MenuItemDTO convertToMenuItemDTO(MenuItem item) {
        return MenuItemDTO.builder()
                .label(item.getLabel())
                .icon(item.getIcon())
                .routerLink(item.getRouterLink())
                .build();
    }
}