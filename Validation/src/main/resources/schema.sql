DROP TABLE IF EXISTS orden_log;
DROP TABLE IF EXISTS detalle_vnt;
DROP TABLE IF EXISTS detalle_t2t;
DROP TABLE IF EXISTS orden_pago;
DROP TABLE IF EXISTS estado;
DROP TABLE IF EXISTS tipo_operacion;
DROP TABLE IF EXISTS institucion;

-- 1. Tabla de Instituciones
CREATE TABLE institucion (
    codigo INT UNSIGNED NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    estado_operativo VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (codigo),
    CONSTRAINT uk_institucion_nombre UNIQUE (nombre),
    CONSTRAINT chk_institucion_estado_operativo
        CHECK (estado_operativo IN ('NORMAL', 'NO_BANCARIA', 'MANTENIMIENTO'))
) ENGINE=InnoDB;

-- 2. Tabla de Tipos de Operación
CREATE TABLE tipo_operacion (
    codigo VARCHAR(10) NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    particularidad_estructural TEXT NOT NULL,
    PRIMARY KEY (codigo),
    CONSTRAINT uk_tipo_operacion_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- 3. Tabla de Estados
CREATE TABLE estado (
    codigo TINYINT UNSIGNED NOT NULL,
    cve VARCHAR(30) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    PRIMARY KEY (codigo),
    CONSTRAINT uk_estado_cve UNIQUE (cve),
    CONSTRAINT uk_estado_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- 4. Tabla Principal de Orden de Pago
CREATE TABLE orden_pago (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    clave_rastreo VARCHAR(50) NOT NULL,
    monto DECIMAL(18,2) NOT NULL,
    moneda CHAR(3) NOT NULL DEFAULT 'MXN',
    fecha DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    -- Datos del Ordenante
    nombre_ordenante VARCHAR(150) NOT NULL,
    cuenta_ordenante VARCHAR(18) NOT NULL,

    -- Datos del Beneficiario
    nombre_beneficiario VARCHAR(150) NOT NULL,
    cuenta_beneficiaria VARCHAR(18) NOT NULL,

    -- Relaciones con catálogos
    codigo_institucion INT UNSIGNED NOT NULL,
    codigo_tipo_operacion VARCHAR(10) NOT NULL,
    estado_actual TINYINT UNSIGNED NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_orden_pago_clave_rastreo UNIQUE (clave_rastreo),
    CONSTRAINT fk_orden_institucion FOREIGN KEY (codigo_institucion) REFERENCES institucion(codigo),
    CONSTRAINT fk_orden_tipo_operacion FOREIGN KEY (codigo_tipo_operacion) REFERENCES tipo_operacion(codigo),
    CONSTRAINT fk_orden_estado FOREIGN KEY (estado_actual) REFERENCES estado(codigo),
    CONSTRAINT chk_orden_monto CHECK (monto > 0),
    CONSTRAINT chk_orden_moneda CHECK (moneda REGEXP '^[A-Z]{3}$')
) ENGINE=InnoDB;

-- 5. Tabla de Detalle para Operaciones T2T (Tercero a Tercero)
CREATE TABLE detalle_t2t (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    orden_pago_id BIGINT UNSIGNED NOT NULL,
    cuenta_ordenante VARCHAR(50) NOT NULL,
    institucion_ordenante VARCHAR(100) NOT NULL,
    cuenta_beneficiaria VARCHAR(50) NOT NULL,
    institucion_beneficiaria VARCHAR(100) NOT NULL,
    beneficiario_nombre VARCHAR(150) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_detalle_t2t_orden FOREIGN KEY (orden_pago_id) REFERENCES orden_pago(id) ON DELETE CASCADE,
    CONSTRAINT uk_detalle_t2t_orden UNIQUE (orden_pago_id)
) ENGINE=InnoDB;

-- 6. Tabla de Detalle para Operaciones VNT (Ventanilla a Tercero)
CREATE TABLE detalle_vnt (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    orden_pago_id BIGINT UNSIGNED NOT NULL,
    nombre_ordenante VARCHAR(150) NOT NULL,
    tipo_documento VARCHAR(50) NOT NULL,
    numero_documento VARCHAR(50) NOT NULL,
    sucursal VARCHAR(100) NOT NULL,
    cuenta_beneficiaria VARCHAR(50) NOT NULL,
    institucion_beneficiaria VARCHAR(100) NOT NULL,
    beneficiario_nombre VARCHAR(150) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_detalle_vnt_orden FOREIGN KEY (orden_pago_id) REFERENCES orden_pago(id) ON DELETE CASCADE,
    CONSTRAINT uk_detalle_vnt_orden UNIQUE (orden_pago_id)
) ENGINE=InnoDB;

-- 7. Tabla de Logs de Auditoría
CREATE TABLE orden_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    orden_pago_id BIGINT UNSIGNED NOT NULL,
    step TINYINT UNSIGNED NOT NULL,
    estado_codigo TINYINT UNSIGNED NOT NULL,
    cve_rastreo VARCHAR(50) NULL,
    hora_procesamiento DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    detalle JSON NULL,
    motivo VARCHAR(500) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_orden_log_orden FOREIGN KEY (orden_pago_id) REFERENCES orden_pago(id) ON DELETE CASCADE,
    CONSTRAINT fk_orden_log_estado FOREIGN KEY (estado_codigo) REFERENCES estado(codigo),
    CONSTRAINT uk_orden_log_step UNIQUE (orden_pago_id, step),
    INDEX idx_orden_log_estado (estado_codigo),
    INDEX idx_orden_log_fecha (hora_procesamiento)
) ENGINE=InnoDB;