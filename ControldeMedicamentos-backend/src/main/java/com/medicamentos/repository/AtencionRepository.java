package com.medicamentos.repository;

import com.medicamentos.domain.model.Atencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AtencionRepository extends JpaRepository<Atencion, Long> {

    List<Atencion> findByPacienteId(Long pacienteId);

    List<Atencion> findByFechaEvaluacionBetween(LocalDate desde, LocalDate hasta);
}
