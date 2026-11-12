package com.medicamentos.repository;

import com.medicamentos.domain.model.RolVistaPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RolVistaPermisoRepository extends JpaRepository<RolVistaPermiso, Integer> {

    @Query("SELECT p FROM RolVistaPermiso p JOIN FETCH p.vista WHERE p.rol.id = :rolId")
    List<RolVistaPermiso> findByRolId(@Param("rolId") Integer rolId);

    Optional<RolVistaPermiso> findByRolIdAndVistaId(Integer rolId, Integer vistaId);

    @Modifying
    @Query("DELETE FROM RolVistaPermiso r WHERE r.rol.id = :rolId")
    void deleteByRolId(@Param("rolId") Integer rolId);
}
