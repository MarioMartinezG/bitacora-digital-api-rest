package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {

    @Query("SELECT DISTINCT mi FROM MenuItem mi " +
            "JOIN mi.roles r " +
            "WHERE r.id = :roleId AND mi.menu.id = :menuId " +
            "ORDER BY mi.orden")
    List<MenuItem> findByRoleIdAndMenuId(@Param("roleId") Integer roleId, @Param("menuId") Integer menuId);

    @Query("SELECT COUNT(mi) > 0 FROM MenuItem mi WHERE mi.label = :label AND mi.menu.id = :menuId")
    boolean existsByLabelAndMenuId(@Param("label") String label, @Param("menuId") Integer menuId);
}
