package com.medicamentos.repository;

import com.medicamentos.domain.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByNroDocumento(String nroDocumento);

    boolean existsByNroDocumento(String nroDocumento);

    long countByActivoTrue();
}
