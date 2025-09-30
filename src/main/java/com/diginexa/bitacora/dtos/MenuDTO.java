package com.diginexa.bitacora.dtos;

import java.util.List;

public record MenuDTO(String label, List<MenuItemDTO> items) {}
