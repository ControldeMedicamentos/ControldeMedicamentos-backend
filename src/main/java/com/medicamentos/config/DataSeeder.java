package com.medicamentos.config;

import com.medicamentos.domain.enums.RolUsuario;
import com.medicamentos.domain.model.Role;
import com.medicamentos.domain.model.Usuario;
import com.medicamentos.domain.model.Vista;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final VistaRepository vistaRepository;
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
            seedRoleAdmin();
            seedVistas();
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

    private void seedRoleAdmin() {
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            Role admin = new Role();
            admin.setName("ROLE_ADMIN");
            admin.setDescripcion("Administrador del sistema con acceso total");
            admin.setEsSistema(true);
            roleRepository.save(admin);
            log.info("ROLE_ADMIN creado");
        }
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
}
