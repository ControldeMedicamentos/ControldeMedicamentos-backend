package com.medicamentos.controller;

import com.medicamentos.dto.request.MedicamentoCreateDTO;
import com.medicamentos.dto.response.MedicamentoDTO;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.MedicamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicines")
@RequiredArgsConstructor
public class MedicamentoController {

    private final MedicamentoService medicamentoService;
    private final AuditLogService auditLogService;

    @GetMapping
    public List<MedicamentoDTO> findAll() {
        return medicamentoService.findAll();
    }

    @GetMapping("/{id}")
    public MedicamentoDTO findById(@PathVariable Long id) {
        return medicamentoService.findById(id);
    }

    @GetMapping("/sismed/{codigoSismed}")
    public MedicamentoDTO findByCodigoSismed(@PathVariable String codigoSismed) {
        return medicamentoService.findByCodigoSismed(codigoSismed);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicamentoDTO create(@Valid @RequestBody MedicamentoCreateDTO request) {
        MedicamentoDTO result = medicamentoService.create(request);
        auditLogService.log("CREAR_MEDICAMENTO", "Medicamentos",
                "Medicamento creado: " + result.nombre() + " — Código SISMED: " + result.codigoSismed());
        return result;
    }

    @PutMapping("/{id}")
    public MedicamentoDTO update(@PathVariable Long id, @Valid @RequestBody MedicamentoCreateDTO request) {
        MedicamentoDTO result = medicamentoService.update(id, request);
        auditLogService.log("ACTUALIZAR_MEDICAMENTO", "Medicamentos",
                "Medicamento actualizado: " + result.nombre());
        return result;
    }

    @PatchMapping("/{id}/status")
    public MedicamentoDTO toggleActivo(@PathVariable Long id) {
        MedicamentoDTO result = medicamentoService.toggleActivo(id);
        String accion = result.activo() ? "ACTIVAR_MEDICAMENTO" : "DESACTIVAR_MEDICAMENTO";
        auditLogService.log(accion, "Medicamentos",
                result.nombre() + " → " + (result.activo() ? "Activo" : "Inactivo"));
        return result;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        medicamentoService.delete(id);
        auditLogService.log("ELIMINAR_MEDICAMENTO", "Medicamentos", "Medicamento ID " + id + " eliminado");
    }
}
