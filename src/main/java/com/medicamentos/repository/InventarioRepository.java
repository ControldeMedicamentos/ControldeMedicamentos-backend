package com.medicamentos.repository;

import com.medicamentos.domain.model.Inventario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByMedicamentoId(Long medicamentoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventario> findByMedicamentoIdOrderByFechaVencimientoAscIdAsc(Long medicamentoId);

    List<Inventario> findByMedicamentoCodigoSismed(String codigoSismed);

    @Query("SELECT i FROM Inventario i WHERE i.stockActual <= i.stockMinimo")
    List<Inventario> findLowStock();

    @Query("SELECT i FROM Inventario i WHERE i.fechaVencimiento < :hoy AND i.stockActual > 0 ORDER BY i.fechaVencimiento ASC")
    List<Inventario> findVencidosPendientes(@Param("hoy") LocalDate hoy);
}
