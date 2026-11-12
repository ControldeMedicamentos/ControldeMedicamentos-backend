package com.medicamentos.repository;

import com.medicamentos.domain.model.Vista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VistaRepository extends JpaRepository<Vista, Integer> {

    List<Vista> findByActivoTrueOrderByOrdenAscNombreAsc();

    boolean existsByCodigo(String codigo);

    Optional<Vista> findByCodigo(String codigo);
}
