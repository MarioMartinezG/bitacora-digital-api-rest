package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    @Query("SELECT mi FROM MenuItem mi JOIN mi.roles r WHERE r.id = :roleId AND mi.menu.id = :menuId ORDER BY mi.orden ASC")
    List<MenuItem> findByRoleIdAndMenu(@Param("roleId") Long roleId,
                                       @Param("menuId") Long menuId);

    @Query("SELECT CASE WHEN COUNT(mi) > 0 THEN TRUE ELSE FALSE END FROM MenuItem mi WHERE mi.label = :label AND mi.menu.id = :menuId")
    boolean existsByLabelAndMenu(@Param("label") String label,
                                 @Param("menuId") Long menuId);
}
