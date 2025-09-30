package com.diginexa.bitacora.repositories;

import com.diginexa.bitacora.entities.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    @Query("SELECT DISTINCT m FROM Menu m JOIN m.roles r WHERE r.id = :roleId ORDER BY m.orden ASC")
    List<Menu> findByRole(@Param("roleId") Long roleId);

    boolean existsByLabel(String label);
}
