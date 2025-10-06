package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer> {

    @Query("SELECT DISTINCT m FROM Menu m " +
            "JOIN m.roles r " +
            "WHERE r.id = :roleId " +
            "ORDER BY m.orden")
    List<Menu> findByRoleId(@Param("roleId") Integer roleId);

    @Query("SELECT COUNT(m) > 0 FROM Menu m WHERE m.label = :label")
    boolean existsByLabel(@Param("label") String label);
}
