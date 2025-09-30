package com.diginexa.bitacora.unit;

import com.diginexa.bitacora.dtos.MenuDTO;
import com.diginexa.bitacora.dtos.MenuItemDTO;
import com.diginexa.bitacora.entities.Menu;
import com.diginexa.bitacora.entities.MenuItem;
import com.diginexa.bitacora.repositories.MenuItemRepository;
import com.diginexa.bitacora.repositories.MenuRepository;
import com.diginexa.bitacora.services.MenuService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class MenuServiceUnitTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private MenuService menuService;

    public MenuServiceUnitTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnMenuStructureForFrontend() {
        // Arrange
        Long roleId = 1L;
        Menu menu = buildMenu();
        MenuItem item = buildMenuItems(menu);

        when(menuRepository.findByRole(roleId))
                .thenReturn(Collections.singletonList(menu));
        when(menuItemRepository.findByRoleIdAndMenu(roleId, menu.getId()))
                .thenReturn(Collections.singletonList(item));

        // Act
        List<MenuDTO> result = menuService.getMenuByRole(roleId);

        // Assert
        assertThat(result).isNotNull().hasSize(1);

        MenuDTO dto = result.getFirst();

        assertThat(dto.label()).isEqualTo("Bitácora Digital");
        assertThat(dto.icon()).isEqualTo("pi pi-fw pi-id-card");
        assertThat(dto.routerLink()).isEqualTo("/home");
        assertThat(dto.items()).isNotNull().hasSize(1);

        MenuItemDTO child = dto.items().getFirst();
        assertThat(child.label()).isEqualTo("Caracteriza tu Asignatura");
        assertThat(child.routerLink()).isEqualTo("/home/bitacora/caracteriza-asignatura");
    }

    private Menu buildMenu() {
        Menu menu = new Menu();
        menu.setId(1L);
        menu.setLabel("Bitácora Digital");
        menu.setIcon("pi pi-fw pi-id-card");
        menu.setRouterLink("/home");
        return menu;
    }

    private MenuItem buildMenuItems(Menu menu) {
        MenuItem item = new MenuItem();
        item.setId(2L);
        item.setMenu(menu);
        item.setLabel("Caracteriza tu Asignatura");
        item.setIcon("pi pi-fw pi-id-card");
        item.setRouterLink("/home/bitacora/caracteriza-asignatura");
        return item;
    }
}