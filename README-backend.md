# Control de Medicamentos - Backend

Backend Spring Boot 3.3.0 para sistema de gestión de medicamentos y vacunas SISMED.

## Tecnologías

- **Framework**: Spring Boot 3.3.0
- **BD**: PostgreSQL
- **ORM**: JPA/Hibernate
- **Java**: 21
- **Build**: Maven
- **API Documentation**: Swagger 3.0 (Springdoc OpenAPI)
- **Mapping**: MapStruct
- **Utilities**: Lombok, Apache POI (Excel)

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/medicamentos/
│   │   ├── config/              Configuración (Web, BD, etc.)
│   │   ├── domain/
│   │   │   ├── model/           Entidades JPA
│   │   │   └── enums/           Enumeraciones
│   │   ├── repository/          Acceso a datos (Spring Data JPA)
│   │   ├── service/             Lógica de negocio
│   │   ├── controller/          Endpoints REST
│   │   ├── dto/
│   │   │   ├── request/         DTOs entrada
│   │   │   └── response/        DTOs salida
│   │   ├── exception/           Excepciones personalizadas
│   │   ├── util/                Utilidades reutilizables
│   │   └── MedicamentosApplication.java
│   └── resources/
│       ├── application.properties
│       └── schema.sql            (Scripts de BD)
└── test/                        Pruebas unitarias e integración
```

## Instalación

1. **Clonar el proyecto**
2. **Crear BD PostgreSQL**
3. **mvn clean install**
4. **mvn spring-boot:run**

API: http://localhost:8080/api
Swagger: http://localhost:8080/api/swagger-ui.html

## Endpoints (Estándar Swagger - Inglés)

- `GET /api/patients` - Listar pacientes
- `POST /api/patients` - Crear paciente
- `GET /api/appointments` - Listar atenciones
- `POST /api/appointments` - Crear atención
- `GET /api/medicines` - Listar medicamentos
- `GET /api/inventory` - Stock disponible
- `GET /api/reports/sismed` - Reporte mensual

## Próximas Implementaciones

1. Entidades JPA
2. Repositories
3. Services
4. Controllers
5. DTOs
6. Tests
7. Scripts SQL
