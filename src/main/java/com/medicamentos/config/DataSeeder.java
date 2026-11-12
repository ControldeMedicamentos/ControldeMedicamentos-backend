package com.medicamentos.config;

import com.medicamentos.domain.enums.RolUsuario;
import com.medicamentos.domain.model.Role;
import com.medicamentos.domain.model.RolVistaPermiso;
import com.medicamentos.domain.model.Usuario;
import com.medicamentos.domain.model.Vista;
import com.medicamentos.repository.RolVistaPermisoRepository;
import com.medicamentos.repository.RoleRepository;
import com.medicamentos.repository.UsuarioRepository;
import com.medicamentos.repository.VistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final VistaRepository vistaRepository;
    private final RolVistaPermisoRepository rolVistaPermisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.enabled:false}")
    private boolean bootstrapAdminEnabled;

    @Value("${app.bootstrap-admin.username:}")
    private String bootstrapAdminUsername;

    @Value("${app.bootstrap-admin.email:}")
    private String bootstrapAdminEmail;

    @Value("${app.bootstrap-admin.password:}")
    private String bootstrapAdminPassword;

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        try {
            seedSystemRoles();
            seedVistas();
            seedDefaultPermissions();
            migrateLegacyPermissions();
            seedUsuarioAdmin();
            log.info("DataSeeder completado correctamente");
        } catch (Exception e) {
            log.error("Error en DataSeeder: {}", e.getMessage(), e);
        }
    }

    private void seedUsuarioAdmin() {

        // Si el bootstrap está deshabilitado, no hace nada
        if (!bootstrapAdminEnabled) {
            log.info("Bootstrap de administrador deshabilitado");
            return;
        }

        // Validación de variables necesarias
        if (bootstrapAdminUsername == null || bootstrapAdminUsername.isBlank()
            || bootstrapAdminEmail == null || bootstrapAdminEmail.isBlank()
            || bootstrapAdminPassword == null || bootstrapAdminPassword.isBlank()) {

            log.error("No se puede crear el administrador inicial: faltan variables de entorno");
            return;
        }

        // Evita duplicado por username
        if (usuarioRepository.existsByUsername(bootstrapAdminUsername)) {
            log.warn(
                    "No se crea administrador inicial: el username '{}' ya existe",
                    bootstrapAdminUsername
            );
            return;
        }

        // Evita duplicado por correo
        if (usuarioRepository.existsByEmail(bootstrapAdminEmail)) {
            log.warn(
                    "No se crea administrador inicial: el correo '{}' ya existe",
                    bootstrapAdminEmail
            );
            return;
        }

        // Crear administrador inicial
        Usuario admin = new Usuario();

        admin.setUsername(bootstrapAdminUsername);
        admin.setPassword(
            passwordEncoder.encode(bootstrapAdminPassword)
        );

        admin.setNombre("Administrador");
        admin.setEmail(bootstrapAdminEmail);
        admin.setRol(RolUsuario.ADMIN);
        admin.setActivo(true);

        // Obliga a cambiar la contraseña en el primer ingreso
        admin.setMustChangePassword(true);

        usuarioRepository.save(admin);

        log.info("Administrador inicial creado correctamente: {}",bootstrapAdminEmail);
    }

    private void seedSystemRoles() {
        seedSystemRole("ROLE_ADMIN", "Administrador del sistema con acceso total");
        seedSystemRole("ROLE_MEDICO", "Personal médico con acceso clínico");
        seedSystemRole("ROLE_ENFERMERA", "Personal de enfermería con acceso asistencial");
    }

    private void seedSystemRole(String name, String descripcion) {
        Role role = roleRepository.findByName(name).orElseGet(Role::new);
        role.setName(name);
        role.setDescripcion(descripcion);
        role.setEsSistema(true);
        roleRepository.save(role);
    }

    private void seedVistas() {
        insert("VISTA_DASHBOARD",    "Dashboard",            "/dashboard",            "General",  1);
        insert("VISTA_PACIENTES",    "Pacientes",            "/pacientes",            "Clínico",  2);
        insert("VISTA_ATENCIONES",   "Atenciones",           "/atenciones",           "Clínico",  3);
        insert("VISTA_MEDICAMENTOS", "Medicamentos",         "/medicamentos",         "Farmacia", 4);
        insert("VISTA_INVENTARIO",   "Inventario / Ajustes", "/medicamentos/ajustes", "Farmacia", 5);
        insert("VISTA_REPORTES",     "Reportes",             "/reportes",             "Reportes", 6);
    }

    private void insert(String codigo, String nombre, String ruta, String grupo, int orden) {
        if (!vistaRepository.existsByCodigo(codigo)) {
            Vista v = new Vista();
            v.setCodigo(codigo);
            v.setNombre(nombre);
            v.setRuta(ruta);
            v.setGrupo(grupo);
            v.setOrden(orden);
            v.setActivo(true);
            vistaRepository.save(v);
            log.info("Vista creada: {}", codigo);
        }
    }

    private void seedDefaultPermissions() {
        grant("ROLE_ADMIN", "VISTA_DASHBOARD", true, true, true, true, true);
        grant("ROLE_ADMIN", "VISTA_PACIENTES", true, true, true, true, true);
        grant("ROLE_ADMIN", "VISTA_ATENCIONES", true, true, true, true, true);
        grant("ROLE_ADMIN", "VISTA_MEDICAMENTOS", true, true, true, true, true);
        grant("ROLE_ADMIN", "VISTA_INVENTARIO", true, true, true, true, true);
        grant("ROLE_ADMIN", "VISTA_REPORTES", true, true, true, true, true);

        grant("ROLE_MEDICO", "VISTA_DASHBOARD", true, false, false, false, false);
        grant("ROLE_MEDICO", "VISTA_PACIENTES", true, true, true, false, false);
        grant("ROLE_MEDICO", "VISTA_ATENCIONES", true, true, true, false, false);
        grant("ROLE_MEDICO", "VISTA_MEDICAMENTOS", true, false, false, false, false);
        grant("ROLE_MEDICO", "VISTA_INVENTARIO", false, false, false, false, false);

        grant("ROLE_ENFERMERA", "VISTA_DASHBOARD", true, false, false, false, false);
        grant("ROLE_ENFERMERA", "VISTA_PACIENTES", true, true, false, false, false);
        grant("ROLE_ENFERMERA", "VISTA_ATENCIONES", true, true, false, false, false);
        grant("ROLE_ENFERMERA", "VISTA_MEDICAMENTOS", true, false, false, false, false);
        grant("ROLE_ENFERMERA", "VISTA_INVENTARIO", true, false, false, false, false);
    }

    private void migrateLegacyPermissions() {
        // Solo actualiza la matriz inicial anterior; conserva cualquier ajuste hecho por un administrador.
        migrateLegacyRole("ROLE_MEDICO");
        migrateLegacyRole("ROLE_ENFERMERA");
    }

    private void migrateLegacyRole(String roleName) {
        roleRepository.findByName(roleName).ifPresent(role -> {
            List<RolVistaPermiso> permisos = rolVistaPermisoRepository.findByRolId(role.getId());
            Map<String, RolVistaPermiso> porVista = permisos.stream()
                    .collect(Collectors.toMap(p -> p.getVista().getCodigo(), p -> p));
            if (permisos.size() < 5 || permisos.size() > 6 || porVista.size() != permisos.size()
                    || !matches(porVista.get("VISTA_DASHBOARD"), true, false, false)
                    || !matches(porVista.get("VISTA_PACIENTES"), true, true, true)
                    || !matches(porVista.get("VISTA_ATENCIONES"), true, true, true)
                    || !matches(porVista.get("VISTA_MEDICAMENTOS"), true, false, false)
                    || !matches(porVista.get("VISTA_INVENTARIO"), true, false, false)
                    || (permisos.size() == 6
                        && !matches(porVista.get("VISTA_REPORTES"), false, false, false))) {
                return;
            }

            if ("ROLE_MEDICO".equals(roleName)) {
                RolVistaPermiso inventario = porVista.get("VISTA_INVENTARIO");
                inventario.setLeer(false);
                rolVistaPermisoRepository.save(inventario);
            } else {
                RolVistaPermiso pacientes = porVista.get("VISTA_PACIENTES");
                RolVistaPermiso atenciones = porVista.get("VISTA_ATENCIONES");
                pacientes.setModificar(false);
                atenciones.setModificar(false);
                rolVistaPermisoRepository.save(pacientes);
                rolVistaPermisoRepository.save(atenciones);
            }
            log.info("Permisos iniciales actualizados para {}", roleName);
        });
    }

    private boolean matches(RolVistaPermiso permiso, boolean leer, boolean escribir, boolean modificar) {
        return permiso != null && permiso.isLeer() == leer && permiso.isEscribir() == escribir
                && permiso.isModificar() == modificar && !permiso.isEliminar();
    }

    private void grant(
            String roleName,
            String vistaCodigo,
            boolean leer,
            boolean escribir,
            boolean modificar,
            boolean eliminar,
            boolean overwrite) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Rol no encontrado: " + roleName));
        Vista vista = vistaRepository.findByCodigo(vistaCodigo)
                .orElseThrow(() -> new IllegalStateException("Vista no encontrada: " + vistaCodigo));

        var existing = rolVistaPermisoRepository.findByRolIdAndVistaId(role.getId(), vista.getId());
        if (existing.isPresent() && !overwrite) {
            return; // Conserva los permisos configurados posteriormente por el administrador.
        }

        RolVistaPermiso permiso = existing.orElseGet(RolVistaPermiso::new);
        permiso.setRol(role);
        permiso.setVista(vista);
        permiso.setLeer(leer);
        permiso.setEscribir(escribir);
        permiso.setModificar(modificar);
        permiso.setEliminar(eliminar);
        rolVistaPermisoRepository.save(permiso);
    }
}
