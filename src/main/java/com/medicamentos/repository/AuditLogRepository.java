package com.medicamentos.repository;

import com.medicamentos.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query(value = """
            SELECT * FROM audit_logs
            WHERE (CAST(:accion AS TEXT) IS NULL OR accion = :accion)
            AND (CAST(:modulo AS TEXT) IS NULL OR modulo = :modulo)
            AND (CAST(:email AS TEXT) IS NULL OR user_email ILIKE CONCAT('%', :email, '%'))
            AND (CAST(:desde AS TIMESTAMP) IS NULL OR timestamp >= CAST(:desde AS TIMESTAMP))
            AND (CAST(:hasta AS TIMESTAMP) IS NULL OR timestamp <= CAST(:hasta AS TIMESTAMP))
            ORDER BY timestamp DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM audit_logs
            WHERE (CAST(:accion AS TEXT) IS NULL OR accion = :accion)
            AND (CAST(:modulo AS TEXT) IS NULL OR modulo = :modulo)
            AND (CAST(:email AS TEXT) IS NULL OR user_email ILIKE CONCAT('%', :email, '%'))
            AND (CAST(:desde AS TIMESTAMP) IS NULL OR timestamp >= CAST(:desde AS TIMESTAMP))
            AND (CAST(:hasta AS TIMESTAMP) IS NULL OR timestamp <= CAST(:hasta AS TIMESTAMP))
            """,
            nativeQuery = true)
    Page<AuditLog> findFiltered(
            @Param("accion") String accion,
            @Param("modulo") String modulo,
            @Param("email") String email,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );

    List<AuditLog> findTop10ByOrderByTimestampDesc();
}
