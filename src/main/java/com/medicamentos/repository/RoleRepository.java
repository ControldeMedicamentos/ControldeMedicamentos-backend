package com.medicamentos.repository;

import com.medicamentos.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    List<Role> findByEsSistemaTrue();

    List<Role> findByEsSistemaFalse();

    boolean existsByName(String name);

    Optional<Role> findByName(String name);
}
