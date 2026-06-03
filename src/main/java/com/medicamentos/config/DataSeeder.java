package com.medicamentos.config;

import com.medicamentos.domain.model.Role;
import com.medicamentos.domain.model.Vista;
import com.medicamentos.repository.RoleRepository;
import com.medicamentos.repository.VistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final VistaRepository vistaRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        try {
            seedRoleAdmin();
            seedVistas();
            log.info("DataSeeder completado correctamente");
        } catch (Exception e) {
            log.error("Error en DataSeeder: {}", e.getMessage(), e);
        }
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
