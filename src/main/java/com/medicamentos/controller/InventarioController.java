package com.medicamentos.controller;

import com.medicamentos.dto.request.AjusteInventarioDTO;
import com.medicamentos.dto.request.InventarioCreateDTO;
import com.medicamentos.dto.response.InventarioDTO;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('VISTA_INVENTARIO:LEER')")
    public List<InventarioDTO> findAll() { return inventarioService.findAll(); }

    @GetMapping("/medicine/{medicamentoId}")
    @PreAuthorize("hasAuthority('VISTA_INVENTARIO:LEER')")
    public List<InventarioDTO> findByMedicamento(@PathVariable Long medicamentoId) {
        return inventarioService.findByMedicamento(medicamentoId);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('VISTA_INVENTARIO:LEER')")
    public List<InventarioDTO> findLowStock() { return inventarioService.findLowStock(); }

    @GetMapping("/expired-pending")
    @PreAuthorize("hasAuthority('VISTA_INVENTARIO:LEER')")
    public List<InventarioDTO> getVencidosPendientes() { return inventarioService.findVencidosPendientes(); }

    @PostMapping
    @PreAuthorize("hasAuthority('VISTA_INVENTARIO:ESCRIBIR')")
    @ResponseStatus(HttpStatus.CREATED)
    public InventarioDTO create(@Valid @RequestBody InventarioCreateDTO request, Authentication authentication) {
        InventarioDTO result = inventarioService.create(request, authentication.getName());
        auditLogService.log("INGRESO_INVENTARIO", "Inventario",
                "Lote ingresado — Stock: " + result.stockActual() + " uds.",
                "INVENTARIO", result.id().toString());
        return result;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VISTA_INVENTARIO:MODIFICAR')")
    public InventarioDTO update(@PathVariable Long id, @Valid @RequestBody InventarioCreateDTO request) {
        InventarioDTO result = inventarioService.update(id, request);
        auditLogService.log("ACTUALIZAR_LOTE", "Inventario", "Datos del lote actualizados",
                "INVENTARIO", id.toString());
        return result;
    }

    @PostMapping("/adjustments")
    @PreAuthorize("hasAuthority('VISTA_INVENTARIO:MODIFICAR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ajustar(@Valid @RequestBody AjusteInventarioDTO request, Authentication authentication) {
        inventarioService.ajustar(request, authentication.getName());
        auditLogService.log("AJUSTE_INVENTARIO", "Inventario",
                "Ajuste tipo " + request.tipoAjuste() + " — " + request.cantidad() + " uds.",
                "INVENTARIO", request.inventarioId().toString());
    }
}
