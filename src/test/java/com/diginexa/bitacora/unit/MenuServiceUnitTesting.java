package com.diginexa.bitacora.unit;

import com.diginexa.bitacora.dtos.MenuDTO;
import com.diginexa.bitacora.dtos.MenuItemDTO;
import com.diginexa.bitacora.entities.Menu;
import com.diginexa.bitacora.entities.MenuItem;
import com.diginexa.bitacora.entities.Rol;
import com.diginexa.bitacora.exceptions.domain.MenuNotFoundException;
import com.diginexa.bitacora.repositories.MenuItemRepository;
import com.diginexa.bitacora.repositories.MenuRepository;
import com.diginexa.bitacora.repositories.RolRepository;
import com.diginexa.bitacora.services.MenuService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuServiceUnitTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private MenuService menuService;

    public MenuServiceUnitTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateMenuSuccessfully() {
        // Arrange
        Menu menu = buildMenu();
        Menu savedMenu = buildMenu();
        savedMenu.setId(1);

        when(menuRepository.existsByLabel(menu.getLabel()))
                .thenReturn(false);
        when(menuRepository.save(menu))
                .thenReturn(savedMenu);

        // Act
        Menu result = menuService.createMenu(menu);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getLabel()).isEqualTo("Bitácora Digital");

        verify(menuRepository).existsByLabel(menu.getLabel());
        verify(menuRepository).save(menu);
    }

    @Test
    void shouldReturnMenuStructureForFrontend() {
        // Arrange
        Integer roleId = 1;
        Rol rol = buildRol();
        Menu menu = buildMenu();
        MenuItem item = buildMenuItem(menu);

        // Agregar rol a las relaciones (simulando las tablas pivote)
        menu.getRoles().add(rol);
        item.getRoles().add(rol);

        when(rolRepository.findById(roleId))
                .thenReturn(Optional.of(rol));
        when(menuRepository.findByRoleId(roleId))
                .thenReturn(Collections.singletonList(menu));
        when(menuItemRepository.findByRoleIdAndMenuId(roleId, menu.getId()))
                .thenReturn(Collections.singletonList(item));

        // Act
        List<MenuDTO> result = menuService.getMenuByRole(roleId);

        // Assert
        assertThat(result).isNotNull().hasSize(1);

        MenuDTO dto = result.getFirst();
        assertThat(dto.getLabel()).isEqualTo("Bitácora Digital");
        assertThat(dto.getIcon()).isEqualTo("pi pi-fw pi-id-card");
        assertThat(dto.getRouterLink()).isEqualTo("/home");
        assertThat(dto.getItems()).isNotNull().hasSize(1);

        MenuItemDTO child = dto.getItems().getFirst();
        assertThat(child.getLabel()).isEqualTo("Caracteriza tu Asignatura");
        assertThat(child.getRouterLink()).isEqualTo("/home/bitacora/caracteriza-asignatura");
    }

    @Test
    void shouldThrowMenuNotFoundExceptionWhenNoMenusForRole() {
        // Arrange
        Integer roleId = 2;
        Rol rol = buildRol(2L, "tutor");

        when(rolRepository.findById(roleId))
                .thenReturn(Optional.of(rol));
        when(menuRepository.findByRoleId(roleId))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> menuService.getMenuByRole(roleId))
                .isInstanceOf(MenuNotFoundException.class)
                .hasMessage("No se encontraron menús para el rol USER");
    }

    private Rol buildRol() {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("estudiante");
        return rol;
    }

    private Rol buildRol(Long id, String nombre) {
        return new Rol(id, nombre);
    }

    private Menu buildMenu() {
        Menu menu = new Menu();
        menu.setId(1);
        menu.setLabel("Bitácora Digital");
        menu.setIcon("pi pi-fw pi-id-card");
        menu.setRouterLink("/home");
        menu.setOrden(1);
        menu.setRoles(new ArrayList<>()); // Inicializar la lista
        return menu;
    }

    private MenuItem buildMenuItem(Menu menu) {
        MenuItem item = new MenuItem();
        item.setId(2);
        item.setMenu(menu);
        item.setLabel("Caracteriza tu Asignatura");
        item.setIcon("pi pi-fw pi-book");
        item.setRouterLink("/home/bitacora/caracteriza-asignatura");
        item.setOrden(1);
        item.setRoles(new ArrayList<>()); // Inicializar la lista
        return item;
    }
}