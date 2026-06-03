package com.medicamentos.repository;

import com.medicamentos.domain.model.PacienteArchivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacienteArchivoRepository extends JpaRepository<PacienteArchivo, Long> {
    List<PacienteArchivo> findByPacienteIdOrderByCreatedAtAsc(Long pacienteId);
}
