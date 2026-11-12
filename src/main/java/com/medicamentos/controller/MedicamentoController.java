package com.medicamentos.controller;

import com.medicamentos.dto.request.MedicamentoCreateDTO;
import com.medicamentos.dto.response.MedicamentoDTO;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.MedicamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicines")
@RequiredArgsConstructor
public class MedicamentoController {

    private final MedicamentoService medicamentoService;
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('VISTA_MEDICAMENTOS:LEER')")
    public List<MedicamentoDTO> findAll() {
        return medicamentoService.findAll();
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('VISTA_MEDICAMENTOS:LEER')")
    public Page<MedicamentoDTO> findPage(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "activos") String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("nombre").ascending());
        return medicamentoService.findPage(search, estado, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VISTA_MEDICAMENTOS:LEER')")
    public MedicamentoDTO findById(@PathVariable Long id) {
        return medicamentoService.findById(id);
    }

    @GetMapping("/sismed/{codigoSismed}")
    @PreAuthorize("hasAuthority('VISTA_MEDICAMENTOS:LEER')")
    public MedicamentoDTO findByCodigoSismed(@PathVariable String codigoSismed) {
        return medicamentoService.findByCodigoSismed(codigoSismed);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VISTA_MEDICAMENTOS:ESCRIBIR')")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicamentoDTO create(@Valid @RequestBody MedicamentoCreateDTO request) {
        MedicamentoDTO result = medicamentoService.create(request);
        auditLogService.log("CREAR_MEDICAMENTO", "Medicamentos",
                "Medicamento creado — Código SISMED: " + result.codigoSismed(),
                "MEDICAMENTO", result.id().toString());
        return result;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VISTA_MEDICAMENTOS:MODIFICAR')")
    public MedicamentoDTO update(@PathVariable Long id, @Valid @RequestBody MedicamentoCreateDTO request) {
        MedicamentoDTO result = medicamentoService.update(id, request);
        auditLogService.log("ACTUALIZAR_MEDICAMENTO", "Medicamentos",
                "Medicamento actualizado", "MEDICAMENTO", id.toString());
        return result;
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('VISTA_MEDICAMENTOS:ELIMINAR')")
    public MedicamentoDTO toggleActivo(@PathVariable Long id) {
        MedicamentoDTO result = medicamentoService.toggleActivo(id);
        String accion = result.activo() ? "ACTIVAR_MEDICAMENTO" : "DESACTIVAR_MEDICAMENTO";
        auditLogService.log(accion, "Medicamentos",
                "Estado cambiado a " + (result.activo() ? "Activo" : "Inactivo"),
                "MEDICAMENTO", id.toString());
        return result;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VISTA_MEDICAMENTOS:ELIMINAR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        medicamentoService.delete(id);
        auditLogService.log("ELIMINAR_MEDICAMENTO", "Medicamentos", "Medicamento eliminado",
                "MEDICAMENTO", id.toString());
    }
}
