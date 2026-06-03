package com.medicamentos.controller;

import com.medicamentos.dto.response.RolDTO;
import com.medicamentos.dto.response.RolVistaPermisoDTO;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final AuditLogService auditLogService;

    @GetMapping("/system")
    public List<RolDTO> getSystemRoles() { return roleService.getSystemRoles(); }

    @GetMapping("/empresa")
    public List<RolDTO> getEmpresaRoles() { return roleService.getEmpresaRoles(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolDTO createRole(@RequestBody Map<String, String> body) {
        RolDTO result = roleService.createRole(body.get("name"), body.get("descripcion"));
        auditLogService.log("CREAR_ROL", "Roles", "Rol creado: " + result.getName());
        return result;
    }

    @PutMapping("/{id}")
    public RolDTO updateRole(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        RolDTO result = roleService.updateRole(id, body.get("name"), body.get("descripcion"));
        auditLogService.log("ACTUALIZAR_ROL", "Roles", "Rol actualizado: " + result.getName());
        return result;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        auditLogService.log("ELIMINAR_ROL", "Roles", "Rol ID " + id + " eliminado");
    }

    @GetMapping("/{id}/vistas")
    public List<RolVistaPermisoDTO> getVistas(@PathVariable Integer id) {
        return roleService.getVistasByRole(id);
    }

    @PutMapping("/{id}/vistas")
    public List<RolVistaPermisoDTO> saveVistas(@PathVariable Integer id,
                                                @RequestBody List<RolVistaPermisoDTO> permisos) {
        List<RolVistaPermisoDTO> result = roleService.saveVistasByRole(id, permisos);
        auditLogService.log("GUARDAR_PERMISOS", "Roles", "Permisos actualizados para rol ID " + id);
        return result;
    }
}
