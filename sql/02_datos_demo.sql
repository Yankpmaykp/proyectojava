-- =====================================================================
--  Datos de ejemplo (los mismos que usaba el Producto 2 en memoria).
--  Ejecución:  mysql -u root -p < sql/02_datos_demo.sql
--  Las reservas se crean con los procedimientos almacenados, igual que la aplicación.
-- =====================================================================
USE hoteles;

INSERT INTO hotel (nombre, direccion, ciudad, telefono, email, estrellas) VALUES
    ('Hotel Mediterráneo', 'Av. del Mar 10', 'Valencia', '961234567', 'info@mediterraneo.com', 4),
    ('Gran Hotel Castilla', 'Plaza Mayor 3', 'Madrid', '915551234', 'reservas@castilla.com', 5);

INSERT INTO habitacion (id_hotel, numero, planta, categoria, precio_noche) VALUES
    (1, '101', 1, 'INDIVIDUAL', 55.00),
    (1, '102', 1, 'DOBLE',      80.00),
    (1, '201', 2, 'TRIPLE',    105.00),
    (1, '301', 3, 'SUITE',     190.00),
    (2, '10',  1, 'DOBLE',     120.00),
    (2, '11',  1, 'DOBLE',     120.00),
    (2, '20',  2, 'SUITE',     260.00);

INSERT INTO cliente (dni, nombre, apellidos, email, telefono, direccion) VALUES
    ('12345678Z', 'Ana',   'García López', 'ana.garcia@email.com',   '600111222', 'C/ Mayor 5, Madrid'),
    ('87654321X', 'Luis',  'Pérez Ruiz',   'luis.perez@email.com',   '600333444', 'C/ Sol 12, Valencia'),
    ('11223344B', 'Marta', 'García Sanz',  'marta.garcia@email.com', '600555666', 'Av. Libertad 8, Sevilla');

INSERT INTO empleado (dni, nombre, apellidos, email, telefono, usuario, rol, id_hotel) VALUES
    ('55667788C', 'Carlos', 'Mora Gil',   'carlos.mora@cadena.com', '600777888', 'cmora',  'ADMINISTRADOR', NULL),
    ('99887766D', 'Elena',  'Ruiz Prado', 'elena.ruiz@cadena.com',  '600999000', 'eruiz',  'RECEPCIONISTA', 1);

-- Reserva de Ana en el Mediterráneo (habitaciones 101 y 102) y de Luis en el Castilla (habitación 10).
START TRANSACTION;
CALL sp_crear_reserva(1, 1, CURDATE() + INTERVAL 10 DAY, CURDATE() + INTERVAL 13 DAY, 3, @reserva1);
CALL sp_agregar_linea_reserva(@reserva1, 2);
CALL sp_agregar_linea_reserva(@reserva1, 1);
CALL sp_crear_reserva(2, 2, CURDATE() + INTERVAL 20 DAY, CURDATE() + INTERVAL 22 DAY, 2, @reserva2);
CALL sp_agregar_linea_reserva(@reserva2, 5);
COMMIT;
