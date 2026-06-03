package com.medicamentos.repository;

import com.medicamentos.domain.model.Atencion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AtencionRepository extends JpaRepository<Atencion, Long> {

    List<Atencion> findByPacienteId(Long pacienteId);

    List<Atencion> findByFechaEvaluacionBetween(LocalDate desde, LocalDate hasta);

    @Query("""
            SELECT a FROM Atencion a
            JOIN a.paciente p
            WHERE a.fechaEvaluacion BETWEEN :desde AND :hasta
              AND (:search = ''
                OR LOWER(p.nombresApellidos) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.nroDocumento) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.motivo) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Atencion> findPageByFecha(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("search") String search,
            Pageable pageable
    );

    long countByFechaEvaluacionBetween(LocalDate desde, LocalDate hasta);

    List<Atencion> findTop5ByOrderByFechaEvaluacionDescIdDesc();

    @Query("SELECT a.fechaEvaluacion, COUNT(a) FROM Atencion a WHERE a.fechaEvaluacion BETWEEN :desde AND :hasta GROUP BY a.fechaEvaluacion ORDER BY a.fechaEvaluacion")
    List<Object[]> countGroupedByDay(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
