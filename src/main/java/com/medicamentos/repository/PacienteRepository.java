package com.medicamentos.repository;

import com.medicamentos.domain.model.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByNroDocumento(String nroDocumento);

    boolean existsByNroDocumento(String nroDocumento);

    long countByActivoTrue();

    @Query("""
            SELECT p FROM Paciente p
            WHERE (:estado = 'todos'
                OR (:estado = 'activos' AND p.activo = true)
                OR (:estado = 'inactivos' AND p.activo = false))
              AND (:search = ''
                OR LOWER(p.nombresApellidos) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.nroDocumento) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(p.carreraArea, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Paciente> findPage(
            @Param("search") String search,
            @Param("estado") String estado,
            Pageable pageable
    );
}
