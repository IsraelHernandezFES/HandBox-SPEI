-- 1. Instituciones
INSERT INTO institucion (codigo, nombre, estado_operativo)
VALUES
    (801, 'Banco Praxis Alfa', 'NORMAL'),
    (802, 'Banco Praxis Beta', 'NORMAL'),
    (803, 'Banco Praxis Gamma', 'NORMAL'),
    (804, 'Praxis Servicios de Pago', 'NO_BANCARIA'),
    (805, 'Banco Praxis Delta', 'MANTENIMIENTO');

-- 2. Tipos de Operación
INSERT INTO tipo_operacion (codigo, nombre, descripcion, particularidad_estructural)
VALUES
    ('T2T', 'Tercero a tercero', 'Un cliente de una institucion envia dinero a un cliente de otra institucion.', 'Tiene cuenta ordenante y cuenta beneficiaria. Es el caso base.'),
    ('VNT', 'Ventanilla a tercero', 'Una persona deposita efectivo en una sucursal para abonar a la cuenta de un beneficiario en otra.', 'No hay cuenta ordenante. El ordenante se identifica por nombre y documento, y aparece la sucursal como campo obligatorio.');

-- 3. Estados Oficiales del Contrato
INSERT INTO estado (codigo, cve, nombre, descripcion)
VALUES
    (1, 'S01', 'Recibida', 'La orden de pago fue recibida y registrada.'),
    (2, 'S02', 'En proceso', 'La orden se encuentra en proceso de validacion y procesamiento.'),
    (3, 'S03', 'Liquidada', 'La orden fue procesada y liquidada correctamente.'),
    (4, 'S04', 'Devuelta', 'La orden fue devuelta despues de haber sido liquidada.'),
    (5, 'S05', 'Rechazada', 'La orden no pudo ser procesada.'),
    (6, 'S06', 'En investigacion', 'La orden requiere una investigacion adicional.');