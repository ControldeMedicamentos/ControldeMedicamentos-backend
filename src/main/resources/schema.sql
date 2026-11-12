CREATE TABLE IF NOT EXISTS pacientes (
    id BIGSERIAL PRIMARY KEY,
    tipo_documento VARCHAR(25) NOT NULL DEFAULT 'DNI',
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombres_apellidos VARCHAR(150) NOT NULL,
    edad INTEGER,
    sexo VARCHAR(20),
    carrera_area VARCHAR(150),
    ciclo_academico VARCHAR(30),
    telefono VARCHAR(15),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    rol VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS medicamentos (
    id BIGSERIAL PRIMARY KEY,
    codigo_sismed VARCHAR(20) NOT NULL UNIQUE,
    codigo_siga VARCHAR(30),
    descripcion_sismed VARCHAR(300) NOT NULL,
    presentacion_frasco VARCHAR(80),
    descripcion_corta VARCHAR(120),
    conversion INTEGER NOT NULL DEFAULT 1,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS inventarios (
    id BIGSERIAL PRIMARY KEY,
    medicamento_id BIGINT NOT NULL REFERENCES medicamentos(id),
    stock_actual INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER NOT NULL DEFAULT 0,
    lote VARCHAR(80),
    fecha_vencimiento DATE,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS atenciones (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL REFERENCES pacientes(id),
    fecha_evaluacion DATE NOT NULL,
    motivo VARCHAR(300) NOT NULL,
    antecedentes TEXT,
    inmunizaciones TEXT,
    signos_vitales TEXT,
    examen_fisico TEXT,
    laboratorio TEXT,
    diagnostico_1 VARCHAR(150),
    cie10_1 VARCHAR(15),
    tipo_diagnostico_1 VARCHAR(20),
    diagnostico_2 VARCHAR(150),
    cie10_2 VARCHAR(15),
    tipo_diagnostico_2 VARCHAR(20),
    diagnostico_3 VARCHAR(150),
    cie10_3 VARCHAR(15),
    tipo_diagnostico_3 VARCHAR(20),
    conclusion VARCHAR(80),
    derivacion VARCHAR(120),
    observaciones TEXT,
    usuario_registro VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id BIGSERIAL PRIMARY KEY,
    medicamento_id BIGINT NOT NULL REFERENCES medicamentos(id),
    atencion_id BIGINT REFERENCES atenciones(id),
    tipo_movimiento VARCHAR(30) NOT NULL,
    tipo_consumo VARCHAR(30),
    cantidad INTEGER NOT NULL,
    periodo VARCHAR(6),
    observacion TEXT,
    usuario_registro VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS movimientos_inventario_lotes (
    id BIGSERIAL PRIMARY KEY,
    movimiento_id BIGINT NOT NULL REFERENCES movimientos_inventario(id),
    inventario_id BIGINT NOT NULL REFERENCES inventarios(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    stock_antes INTEGER NOT NULL CHECK (stock_antes >= 0),
    stock_despues INTEGER NOT NULL CHECK (stock_despues >= 0),
    CONSTRAINT uk_movimiento_inventario_lote UNIQUE (movimiento_id, inventario_id)
);

CREATE TABLE IF NOT EXISTS consumos_medicamento (
    id BIGSERIAL PRIMARY KEY,
    atencion_id BIGINT NOT NULL REFERENCES atenciones(id),
    medicamento_id BIGINT NOT NULL REFERENCES medicamentos(id),
    movimiento_inventario_id BIGINT REFERENCES movimientos_inventario(id),
    cantidad_consumida INTEGER NOT NULL,
    tipo_consumo VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_atenciones_paciente ON atenciones(paciente_id);
CREATE INDEX IF NOT EXISTS idx_atenciones_fecha ON atenciones(fecha_evaluacion);
CREATE INDEX IF NOT EXISTS idx_inventarios_medicamento ON inventarios(medicamento_id);
CREATE INDEX IF NOT EXISTS idx_movimientos_periodo ON movimientos_inventario(periodo);
CREATE INDEX IF NOT EXISTS idx_movimientos_medicamento_periodo ON movimientos_inventario(medicamento_id, periodo);
CREATE INDEX IF NOT EXISTS idx_movimiento_lote_movimiento ON movimientos_inventario_lotes(movimiento_id);
CREATE INDEX IF NOT EXISTS idx_movimiento_lote_inventario ON movimientos_inventario_lotes(inventario_id);
CREATE INDEX IF NOT EXISTS idx_consumos_atencion ON consumos_medicamento(atencion_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);

CREATE TABLE IF NOT EXISTS conciliaciones_mensuales (
    id BIGSERIAL PRIMARY KEY,
    periodo VARCHAR(6) NOT NULL UNIQUE,
    resultado VARCHAR(25) NOT NULL,
    observacion TEXT,
    usuario_registro VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS conciliaciones_mensuales_detalle (
    id BIGSERIAL PRIMARY KEY,
    conciliacion_id BIGINT NOT NULL REFERENCES conciliaciones_mensuales(id) ON DELETE CASCADE,
    medicamento_id BIGINT NOT NULL REFERENCES medicamentos(id),
    stock_sistema INTEGER NOT NULL CHECK (stock_sistema >= 0),
    stock_fisico INTEGER NOT NULL CHECK (stock_fisico >= 0),
    diferencia INTEGER NOT NULL,
    CONSTRAINT uk_conciliacion_medicamento UNIQUE (conciliacion_id, medicamento_id)
);

CREATE INDEX IF NOT EXISTS idx_conciliacion_periodo ON conciliaciones_mensuales(periodo);
CREATE INDEX IF NOT EXISTS idx_conciliacion_detalle_medicamento ON conciliaciones_mensuales_detalle(medicamento_id);

CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    descripcion VARCHAR(250),
    es_sistema BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS vistas (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    ruta VARCHAR(150),
    grupo VARCHAR(80),
    orden INTEGER DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS rol_vista_permisos (
    id SERIAL PRIMARY KEY,
    rol_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    vista_id INTEGER NOT NULL REFERENCES vistas(id) ON DELETE CASCADE,
    leer BOOLEAN NOT NULL DEFAULT FALSE,
    escribir BOOLEAN NOT NULL DEFAULT FALSE,
    modificar BOOLEAN NOT NULL DEFAULT FALSE,
    eliminar BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(rol_id, vista_id)
);

INSERT INTO roles (name, descripcion, es_sistema)
VALUES
    ('ROLE_ADMIN', 'Administrador del sistema con acceso total', TRUE),
    ('ROLE_MEDICO', 'Personal médico con acceso clínico', TRUE),
    ('ROLE_ENFERMERA', 'Personal de enfermería con acceso asistencial', TRUE)
ON CONFLICT (name) DO UPDATE SET
    descripcion = EXCLUDED.descripcion,
    es_sistema = TRUE;

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    user_email VARCHAR(120),
    user_rol VARCHAR(30),
    accion VARCHAR(60) NOT NULL,
    modulo VARCHAR(50) NOT NULL,
    detalle TEXT,
    ip_address VARCHAR(64),
    request_method VARCHAR(10),
    request_path VARCHAR(300),
    resultado VARCHAR(20) NOT NULL DEFAULT 'EXITOSO',
    entidad_tipo VARCHAR(60),
    entidad_id VARCHAR(80),
    user_agent VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_email ON audit_logs(user_email);
CREATE INDEX IF NOT EXISTS idx_audit_logs_ip_address ON audit_logs(ip_address);
CREATE INDEX IF NOT EXISTS idx_audit_logs_resultado ON audit_logs(resultado);

INSERT INTO vistas (codigo, nombre, ruta, grupo, orden) VALUES
    ('VISTA_DASHBOARD',     'Dashboard',           '/dashboard',            'General',  1),
    ('VISTA_PACIENTES',     'Pacientes',            '/pacientes',            'Clínico',  2),
    ('VISTA_ATENCIONES',    'Atenciones',           '/atenciones',           'Clínico',  3),
    ('VISTA_MEDICAMENTOS',  'Medicamentos',         '/medicamentos',         'Farmacia', 4),
    ('VISTA_INVENTARIO',    'Inventario / Ajustes', '/medicamentos/ajustes', 'Farmacia', 5),
    ('VISTA_REPORTES',      'Reportes',             '/reportes',             'Reportes', 6)
ON CONFLICT (codigo) DO NOTHING;
