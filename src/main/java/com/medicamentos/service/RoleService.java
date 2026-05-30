package com.medicamentos.service;

import com.medicamentos.dto.response.RolDTO;
import com.medicamentos.dto.response.RolVistaPermisoDTO;

import java.util.List;

public interface RoleService {

    List<RolDTO> getSystemRoles();

    List<RolDTO> getEmpresaRoles();

    RolDTO createRole(String name, String descripcion);

    RolDTO updateRole(Integer id, String name, String descripcion);

    void deleteRole(Integer id);

    List<RolVistaPermisoDTO> getVistasByRole(Integer roleId);

    List<RolVistaPermisoDTO> saveVistasByRole(Integer roleId, List<RolVistaPermisoDTO> permisos);
}
