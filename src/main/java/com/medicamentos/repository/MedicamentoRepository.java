package com.medicamentos.repository;

import com.medicamentos.domain.model.Medicamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    Optional<Medicamento> findByCodigoSismed(String codigoSismed);

    boolean existsByCodigoSismed(String codigoSismed);

    long countByActivoTrue();

    @Query("""
            SELECT m FROM Medicamento m
            WHERE (:estado = 'todos'
                OR (:estado = 'activos' AND m.activo = true)
                OR (:estado = 'inactivos' AND m.activo = false))
              AND (:search = ''
                OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(m.registroSanitario, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(m.fabricante, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(m.codigoSismed, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Medicamento> findPage(
            @Param("search") String search,
            @Param("estado") String estado,
            Pageable pageable
    );
}
