package com.medicamentos.repository;

import com.medicamentos.domain.model.AtencionArchivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtencionArchivoRepository extends JpaRepository<AtencionArchivo, Long> {
    List<AtencionArchivo> findByAtencionIdOrderByCreatedAtAsc(Long atencionId);
}
