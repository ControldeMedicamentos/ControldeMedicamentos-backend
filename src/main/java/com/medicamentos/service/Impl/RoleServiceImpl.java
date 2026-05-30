package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.Role;
import com.medicamentos.domain.model.RolVistaPermiso;
import com.medicamentos.domain.model.Vista;
import com.medicamentos.dto.response.RolDTO;
import com.medicamentos.dto.response.RolVistaPermisoDTO;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.repository.RolVistaPermisoRepository;
import com.medicamentos.repository.RoleRepository;
import com.medicamentos.repository.VistaRepository;
import com.medicamentos.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final VistaRepository vistaRepository;
    private final RolVistaPermisoRepository rolVistaPermisoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RolDTO> getSystemRoles() {
        return roleRepository.findByEsSistemaTrue().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolDTO> getEmpresaRoles() {
        return roleRepository.findByEsSistemaFalse().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public RolDTO createRole(String name, String descripcion) {
        String normalizado = normalizarNombre(name);
        if (roleRepository.existsByName(normalizado)) {
            throw new IllegalArgumentException("Ya existe un rol con ese nombre");
        }
        Role role = new Role();
        role.setName(normalizado);
        role.setDescripcion(descripcion != null && !descripcion.isBlank() ? descripcion.trim() : null);
        role.setEsSistema(false);
        return toDTO(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RolDTO updateRole(Integer id, String name, String descripcion) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
        if (!role.isEsSistema()) {
            String normalizado = normalizarNombre(name);
            roleRepository.findByName(normalizado)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(e -> { throw new IllegalArgumentException("Ya existe un rol con ese nombre"); });
            role.setName(normalizado);
        }
        role.setDescripcion(descripcion != null && !descripcion.isBlank() ? descripcion.trim() : null);
        return toDTO(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void deleteRole(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
        if (role.isEsSistema()) {
            throw new IllegalArgumentException("No se puede eliminar un rol del sistema");
        }
        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolVistaPermisoDTO> getVistasByRole(Integer roleId) {
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleId));

        List<Vista> vistas = vistaRepository.findByActivoTrueOrderByOrdenAscNombreAsc();
        Map<Integer, RolVistaPermiso> permisoMap = rolVistaPermisoRepository.findByRolId(roleId)
                .stream()
                .collect(Collectors.toMap(p -> p.getVista().getId(), p -> p));

        return vistas.stream().map(v -> {
            RolVistaPermiso p = permisoMap.get(v.getId());
            RolVistaPermisoDTO dto = new RolVistaPermisoDTO();
            dto.setVistaId(v.getId());
            dto.setCodigo(v.getCodigo());
            dto.setNombre(v.getNombre());
            dto.setRuta(v.getRuta());
            dto.setGrupo(v.getGrupo());
            dto.setLeer(p != null && p.isLeer());
            dto.setEscribir(p != null && p.isEscribir());
            dto.setModificar(p != null && p.isModificar());
            dto.setEliminar(p != null && p.isEliminar());
            return dto;
        }).toList();
    }

    @Override
    @Transactional
    public List<RolVistaPermisoDTO> saveVistasByRole(Integer roleId, List<RolVistaPermisoDTO> permisos) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleId));

        rolVistaPermisoRepository.deleteByRolId(roleId);
        rolVistaPermisoRepository.flush();

        for (RolVistaPermisoDTO dto : permisos) {
            Vista vista = vistaRepository.findById(dto.getVistaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vista no encontrada: " + dto.getVistaId()));
            RolVistaPermiso rvp = new RolVistaPermiso();
            rvp.setRol(role);
            rvp.setVista(vista);
            rvp.setLeer(dto.isLeer());
            rvp.setEscribir(dto.isEscribir());
            rvp.setModificar(dto.isModificar());
            rvp.setEliminar(dto.isEliminar());
            rolVistaPermisoRepository.save(rvp);
        }

        return getVistasByRole(roleId);
    }

    private RolDTO toDTO(Role role) {
        RolDTO dto = new RolDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescripcion(role.getDescripcion());
        dto.setEsSistema(role.isEsSistema());
        return dto;
    }

    private String normalizarNombre(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio");
        }
        String n = name.trim().toUpperCase().replaceAll("\\s+", "_");
        if (!n.startsWith("ROLE_")) n = "ROLE_" + n;
        if (!n.matches("^ROLE_[A-Z0-9_]{2,60}$")) {
            throw new IllegalArgumentException("Nombre inválido. Use solo letras, números y guion bajo");
        }
        return n;
    }
}
