-- Datos iniciales para el perfil por defecto (H2)
INSERT INTO operador (nombre, activo) VALUES ('Tigo', TRUE);
INSERT INTO operador (nombre, activo) VALUES ('Movistar', TRUE);
INSERT INTO operador (nombre, activo) VALUES ('Comcel', TRUE);
INSERT INTO operador (nombre, activo) VALUES ('Uff', TRUE);

INSERT INTO vendedor (numero_documento, nombre, activo) VALUES ('1010101010', 'Ana Gómez', TRUE);
INSERT INTO vendedor (numero_documento, nombre, activo) VALUES ('2020202020', 'Carlos Pérez', TRUE);
INSERT INTO vendedor (numero_documento, nombre, activo) VALUES ('3030303030', 'Luisa Martínez', TRUE);

-- Algunas ventas de ejemplo para que el resumen no arranque vacío
INSERT INTO recarga (operador_id, vendedor_id, numero_celular, valor, fecha_venta)
VALUES ((SELECT id FROM operador WHERE nombre = 'Tigo'),
        (SELECT id FROM vendedor WHERE numero_documento = '1010101010'),
        '3001234567', 10000, CURRENT_TIMESTAMP - INTERVAL '2' DAY);
INSERT INTO recarga (operador_id, vendedor_id, numero_celular, valor, fecha_venta)
VALUES ((SELECT id FROM operador WHERE nombre = 'Movistar'),
        (SELECT id FROM vendedor WHERE numero_documento = '1010101010'),
        '3157654321', 5000, CURRENT_TIMESTAMP - INTERVAL '1' DAY);
INSERT INTO recarga (operador_id, vendedor_id, numero_celular, valor, fecha_venta)
VALUES ((SELECT id FROM operador WHERE nombre = 'Comcel'),
        (SELECT id FROM vendedor WHERE numero_documento = '2020202020'),
        '3109876543', 20000, CURRENT_TIMESTAMP - INTERVAL '1' DAY);
INSERT INTO recarga (operador_id, vendedor_id, numero_celular, valor, fecha_venta)
VALUES ((SELECT id FROM operador WHERE nombre = 'Tigo'),
        (SELECT id FROM vendedor WHERE numero_documento = '3030303030'),
        '3012223344', 3000, CURRENT_TIMESTAMP);
