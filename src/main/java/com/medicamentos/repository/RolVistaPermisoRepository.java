package com.medicamentos.repository;

import com.medicamentos.domain.model.RolVistaPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolVistaPermisoRepository extends JpaRepository<RolVistaPermiso, Integer> {

    List<RolVistaPermiso> findByRolId(Integer rolId);

    @Modifying
    @Query("DELETE FROM RolVistaPermiso r WHERE r.rol.id = :rolId")
    void deleteByRolId(@Param("rolId") Integer rolId);
}
