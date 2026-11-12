package com.medicamentos.repository;

import com.medicamentos.domain.model.ConciliacionMensual;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConciliacionMensualRepository extends JpaRepository<ConciliacionMensual, Long> {

    @EntityGraph(attributePaths = {"detalles", "detalles.medicamento"})
    Optional<ConciliacionMensual> findByPeriodo(String periodo);
}
