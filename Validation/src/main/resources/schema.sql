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
    puede_enviar BOOLEAN NOT NULL DEFAULT TRUE,
    puede_recibir BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_actualizacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (codigo),
    CONSTRAINT uk_institucion_nombre UNIQUE (nombre),
    CONSTRAINT chk_institucion_estado_operativo
        CHECK (estado_operativo IN ('NORMAL', 'NO_BANCARIA', 'MANTENIMIENTO'))
) ENGINE=InnoDB;

INSERT INTO institucion (codigo, nombre, estado_operativo)
VALUES
    (801, 'Banco Praxis Alfa', 'NORMAL'),
    (802, 'Banco Praxis Beta', 'NORMAL'),
    (803, 'Banco Praxis Gamma', 'NORMAL'),
    (804, 'Praxis Servicios de Pago', 'NO_BANCARIA'),
    (805, 'Banco Praxis Delta', 'MANTENIMIENTO');

-- 2. Tabla de Tipos de Operación (Actualizada con T2T y VNT)
CREATE TABLE tipo_operacion (
    codigo VARCHAR(10) NOT NULL,
    descripcion VARCHAR(150) NOT NULL,
    particularidad_estructural TEXT NOT NULL,
    PRIMARY KEY (codigo),
    CONSTRAINT uk_tipo_operacion_descripcion UNIQUE (descripcion)
) ENGINE=InnoDB;

INSERT INTO tipo_operacion (codigo, descripcion, particularidad_estructural)
VALUES
    ('T2T', 'Tercero a tercero', 'Un cliente de una institución envía dinero a un cliente de otra institución.','Tiene cuenta ordenante y cuenta beneficiaria. Es el caso base.'),
    ('VNT', 'Ventanilla a tercero', 'Una persona deposita efectivo en una sucursalpara abonar a la cuenta de un beneficiario en otra.','No hay cuenta ordenante. El ordenante se identifica por nombre y documento, y aparece la sucursal como campo obligatorio.');

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

INSERT INTO estado (codigo, cve, nombre, descripcion)
VALUES
    (1, 'S01', 'Recibida', 'La orden de pago fue recibida y registrada.'),
    (2, 'S02', 'En proceso', 'La orden se encuentra en proceso de validación y procesamiento.'),
    (3, 'S03', 'Liquidada', 'La orden fue procesada y liquidada correctamente.'),
    (4, 'S04', 'Devuelta', 'La orden fue devuelta después de haber sido liquidada.'),
    (5, 'S05', 'Rechazada', 'La orden no pudo ser procesada.'),
    (6, 'S06', 'En investigación', 'La orden requiere una investigación adicional.');

-- 4. Tabla Principal de Orden de Pago (Datos Generales)
CREATE TABLE orden_pago (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    referencia VARCHAR(100) NOT NULL,
    codigo_institucion INT UNSIGNED NOT NULL,
    codigo_tipo_operacion VARCHAR(10) NOT NULL,
    estado_actual TINYINT UNSIGNED NOT NULL, -- Corregido para coincidir con estado(codigo)
    importe DECIMAL(18,2) NOT NULL,
    moneda CHAR(3) NOT NULL DEFAULT 'MXN',
    fecha_registro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_actualizacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_orden_pago_referencia UNIQUE (referencia),
    CONSTRAINT fk_orden_institucion FOREIGN KEY (codigo_institucion) REFERENCES institucion(codigo),
    CONSTRAINT fk_orden_tipo_operacion FOREIGN KEY (codigo_tipo_operacion) REFERENCES tipo_operacion(codigo),
    CONSTRAINT fk_orden_estado FOREIGN KEY (estado_actual) REFERENCES estado(codigo),
    CONSTRAINT chk_orden_importe CHECK (importe >= 0),
    CONSTRAINT chk_orden_moneda CHECK (moneda REGEXP '^[A-Z]{3}$')
) ENGINE=InnoDB;

-- 5. Tabla de Detalle para Operaciones T2T (Tercero a Tercero)
-- Aplica cuando el tipo de operación es 'T2T'
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
-- Aplica cuando el tipo de operación es 'VNT' (Sin cuenta ordenante, requiere documento y sucursal)
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

-- 7. Tabla de Logs de Auditoría y Seguimiento de Pasos
CREATE TABLE orden_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    orden_pago_id BIGINT UNSIGNED NOT NULL,
    step TINYINT UNSIGNED NOT NULL,
    estado_codigo TINYINT UNSIGNED NOT NULL,
    cve_rastreo VARCHAR(100) NULL,
    hora_procesamiento DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    detalle JSON NULL,
    motivo VARCHAR(500) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_orden_log_orden FOREIGN KEY (orden_pago_id) REFERENCES orden_pago(id) ON DELETE CASCADE,
    CONSTRAINT fk_orden_log_estado FOREIGN KEY (estado_codigo) REFERENCES estado(codigo),
    CONSTRAINT uk_orden_log_step UNIQUE (orden_pago_id, step),
    INDEX idx_orden_log_orden (orden_pago_id, step),
    INDEX idx_orden_log_estado (estado_codigo),
    INDEX idx_orden_log_fecha (hora_procesamiento)
) ENGINE=InnoDB;