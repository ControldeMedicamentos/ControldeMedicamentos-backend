package com.medicamentos.repository;

import com.medicamentos.domain.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    Optional<Medicamento> findByCodigoSismed(String codigoSismed);

    boolean existsByCodigoSismed(String codigoSismed);

    long countByActivoTrue();
}
