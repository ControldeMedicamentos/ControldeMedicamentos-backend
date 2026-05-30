package com.medicamentos.controller;

import com.medicamentos.dto.request.EmpleadoCreateDTO;
import com.medicamentos.dto.request.EmpleadoUpdateDTO;
import com.medicamentos.dto.response.EmpleadoDTO;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.EmpleadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final AuditLogService auditLogService;

    @GetMapping
    public List<EmpleadoDTO> findAll() {
        return empleadoService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpleadoDTO create(@Valid @RequestBody EmpleadoCreateDTO dto) {
        EmpleadoDTO result = empleadoService.create(dto);
        auditLogService.log("CREAR_EMPLEADO", "Empleados",
                "Empleado creado: " + result.nombre() + " (" + result.email() + ") — Rol: " + result.rol());
        return result;
    }

    @PutMapping("/{id}")
    public EmpleadoDTO update(@PathVariable Long id, @Valid @RequestBody EmpleadoUpdateDTO dto) {
        EmpleadoDTO result = empleadoService.update(id, dto);
        auditLogService.log("ACTUALIZAR_EMPLEADO", "Empleados",
                "Empleado actualizado: " + result.nombre() + " (" + result.email() + ")");
        return result;
    }

    @PatchMapping("/{id}/estado")
    public EmpleadoDTO toggleEstado(@PathVariable Long id) {
        EmpleadoDTO result = empleadoService.toggleEstado(id);
        String accion = result.activo() ? "ACTIVAR_EMPLEADO" : "DESACTIVAR_EMPLEADO";
        auditLogService.log(accion, "Empleados",
                "Estado cambiado: " + result.nombre() + " → " + (result.activo() ? "Activo" : "Inactivo"));
        return result;
    }
}
