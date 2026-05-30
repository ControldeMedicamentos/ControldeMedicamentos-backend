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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final AuditLogService auditLogService;

    @GetMapping
    public List<InventarioDTO> findAll() { return inventarioService.findAll(); }

    @GetMapping("/medicine/{medicamentoId}")
    public List<InventarioDTO> findByMedicamento(@PathVariable Long medicamentoId) {
        return inventarioService.findByMedicamento(medicamentoId);
    }

    @GetMapping("/low-stock")
    public List<InventarioDTO> findLowStock() { return inventarioService.findLowStock(); }

    @GetMapping("/expired-pending")
    public List<InventarioDTO> getVencidosPendientes() { return inventarioService.findVencidosPendientes(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventarioDTO create(@Valid @RequestBody InventarioCreateDTO request) {
        InventarioDTO result = inventarioService.create(request);
        auditLogService.log("INGRESO_INVENTARIO", "Inventario",
                "Lote ingresado: " + result.nombreMedicamento() + " — Stock: " + result.stockActual() + " uds.");
        return result;
    }

    @PutMapping("/{id}")
    public InventarioDTO update(@PathVariable Long id, @Valid @RequestBody InventarioCreateDTO request) {
        return inventarioService.update(id, request);
    }

    @PostMapping("/adjustments")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ajustar(@Valid @RequestBody AjusteInventarioDTO request, Authentication authentication) {
        inventarioService.ajustar(request, authentication.getName());
        auditLogService.log("AJUSTE_INVENTARIO", "Inventario",
                "Ajuste tipo " + request.tipoAjuste() + " — " + request.cantidad() + " uds. (lote ID: " + request.inventarioId() + ")");
    }
}
