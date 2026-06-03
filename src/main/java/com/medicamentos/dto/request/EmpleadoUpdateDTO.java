package com.medicamentos.dto.request;

import com.medicamentos.domain.enums.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmpleadoUpdateDTO(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email @Size(max = 120) String email,
        @Pattern(regexp = "^\\d{8}$", message = "DNI debe tener exactamente 8 dígitos") String dni,
        @NotNull RolUsuario rol
) {}
