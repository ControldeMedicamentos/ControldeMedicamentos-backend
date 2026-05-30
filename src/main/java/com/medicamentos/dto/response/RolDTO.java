package com.medicamentos.dto.response;

import lombok.Data;

@Data
public class RolDTO {
    private Integer id;
    private String name;
    private String descripcion;
    private boolean esSistema;
}
