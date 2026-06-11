package com.medicamentos;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Control de Medicamentos API",
        version = "1.0.0",
        description = "API REST para gestión de medicamentos y vacunas bajo estándares SISMED del Ministerio de Salud Perú"
    )
)
public class MedicamentosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicamentosApplication.class, args);
    }

}
