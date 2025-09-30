package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.MenuDTO;
import com.diginexa.bitacora.dtos.MenuItemDTO;
import com.diginexa.bitacora.entities.Menu;
import com.diginexa.bitacora.entities.MenuItem;
import com.diginexa.bitacora.entities.Rol;
import com.diginexa.bitacora.exceptions.domain.MenuItemNotFoundException;
import com.diginexa.bitacora.exceptions.domain.MenuNotFoundException;
import com.diginexa.bitacora.exceptions.domain.RoleNotFoundException;
import com.diginexa.bitacora.exceptions.validation.DuplicateMenuException;
import com.diginexa.bitacora.exceptions.validation.DuplicateMenuItemException;
import com.diginexa.bitacora.repositories.MenuItemRepository;
import com.diginexa.bitacora.repositories.MenuRepository;
import com.diginexa.bitacora.repositories.RolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final RolRepository rolRepository;

    public MenuService(MenuRepository menuRepository, MenuItemRepository menuItemRepository, RolRepository rolRepository) {
        this.menuRepository = menuRepository;
        this.menuItemRepository = menuItemRepository;
        this.rolRepository = rolRepository;
    }

    public List<MenuDTO> getMenuByRole(Long role) {
        Rol roleValidation = rolRepository.findById(role)
                .orElseThrow(() -> new RoleNotFoundException("Rol con id " + role + " no existe"));

        List<Menu> menus = menuRepository.findByRole(role);
        if (menus.isEmpty()) {
            throw new MenuNotFoundException("No se encontraron menús para el rol " + roleValidation.getNombre());
        }

        return buildMenuDTOs(menus, role);

        /*
        return menus.stream()
                .map(menu -> new MenuDTO(
                        menu.getLabel(),
                        menuItemRepository.findByRoleIdAndMenu(role, menu.getId())
                                .stream()
                                .map(i -> new MenuItemDTO(i.getLabel(), i.getIcon(), i.getRouterLink()))
                                .toList()
                ))
                .toList();
         */
    }

    public Menu createMenu(Menu menu) {
        if (menuRepository.existsByLabel(menu.getLabel())) {
            throw new DuplicateMenuException("El menú '" + menu.getLabel() + "' ya existe");
        }
        return menuRepository.save(menu);
    }

    public MenuItem createMenuItem(MenuItem item, Long menuId) {
        if (menuItemRepository.existsByLabelAndMenu(item.getLabel(), menuId)) {
            throw new DuplicateMenuItemException(
                    "El submenú '" + item.getLabel() + "' ya existe en el menú con id " + menuId
            );
        }
        return menuItemRepository.save(item);
    }

    private List<MenuDTO> buildMenuDTOs(List<Menu> menus, Long roleId) {
        return menus.stream()
                .map(menu -> {
                    List<MenuItem> items = menuItemRepository.findByRoleIdAndMenu(roleId, menu.getId());

                    if (items.isEmpty()) {
                        throw new MenuItemNotFoundException("El menú '" + menu.getLabel() + "' no tiene submenús para este rol");
                    }

                    return new MenuDTO(
                            menu.getLabel(),
                            items.stream()
                                    .map(i -> new MenuItemDTO(i.getLabel(), i.getIcon(), i.getRouterLink()))
                                    .toList()
                    );
                })
                .toList();
    }

}