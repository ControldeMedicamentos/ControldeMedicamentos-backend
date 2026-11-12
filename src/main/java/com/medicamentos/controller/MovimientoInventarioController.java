package com.medicamentos.controller;

import com.medicamentos.dto.request.MovimientoInventarioCreateDTO;
import com.medicamentos.dto.response.MovimientoInventarioDTO;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.MovimientoInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inventory-movements")
@RequiredArgsConstructor
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoInventarioService;
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('VISTA_INVENTARIO:LEER')")
    public List<MovimientoInventarioDTO> findAll() {
        return movimientoInventarioService.findAll();
    }

    @GetMapping("/period/{periodo}")
    @PreAuthorize("hasAuthority('VISTA_INVENTARIO:LEER')")
    public List<MovimientoInventarioDTO> findByPeriodo(@PathVariable String periodo) {
        return movimientoInventarioService.findByPeriodo(periodo);
    }

    @GetMapping("/medicine/{medicamentoId}")
    @PreAuthorize("hasAuthority('VISTA_INVENTARIO:LEER')")
    public List<MovimientoInventarioDTO> findByMedicamento(@PathVariable Long medicamentoId) {
        return movimientoInventarioService.findByMedicamento(medicamentoId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VISTA_INVENTARIO:ESCRIBIR')")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoInventarioDTO create(@Valid @RequestBody MovimientoInventarioCreateDTO request) {
        MovimientoInventarioDTO result = movimientoInventarioService.create(request);
        auditLogService.log("CREAR_MOVIMIENTO_INVENTARIO", "Inventario",
                "Movimiento de inventario registrado", "MOVIMIENTO_INVENTARIO", result.id().toString());
        return result;
    }
}
