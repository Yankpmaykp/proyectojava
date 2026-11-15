-- =====================================================================
--  Producto 3 - Sistema de gestión de una cadena de hoteles
--  Esquema relacional MySQL: tablas, claves, restricciones y
--  procedimientos almacenados.
--
--  Ejecución:  mysql -u root -p < sql/01_schema.sql
--  (el cliente "mysql" interpreta DELIMITER; el script es re-ejecutable)
-- =====================================================================

DROP DATABASE IF EXISTS hoteles;
CREATE DATABASE hoteles CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE hoteles;

-- Usuario de la aplicación con los mínimos privilegios necesarios
-- (la aplicación no necesita ser root).
CREATE USER IF NOT EXISTS 'hoteles_app'@'localhost' IDENTIFIED BY 'hoteles_app_2026';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON hoteles.* TO 'hoteles_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE, CREATE, DROP, ALTER, INDEX, REFERENCES, CREATE ROUTINE, ALTER ROUTINE ON hoteles_test.* TO 'hoteles_app'@'localhost';  -- solo para las pruebas JUnit

-- ---------------------------------------------------------------------
--  TABLAS
-- ---------------------------------------------------------------------

-- Catálogo de categorías: la capacidad máxima vive en la BD y no solo en el enum Java.
CREATE TABLE categoria_habitacion (
    nombre           VARCHAR(20) NOT NULL,
    capacidad_maxima TINYINT UNSIGNED NOT NULL,
    CONSTRAINT pk_categoria PRIMARY KEY (nombre),
    CONSTRAINT ck_categoria_capacidad CHECK (capacidad_maxima BETWEEN 1 AND 10)
) ENGINE = InnoDB;

INSERT INTO categoria_habitacion (nombre, capacidad_maxima) VALUES
    ('INDIVIDUAL', 1), ('DOBLE', 2), ('TRIPLE', 3), ('SUITE', 4);

CREATE TABLE hotel (
    id_hotel   INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre     VARCHAR(100) NOT NULL,
    direccion  VARCHAR(150) NULL,
    ciudad     VARCHAR(80)  NOT NULL,
    telefono   VARCHAR(20)  NULL,
    email      VARCHAR(100) NULL,
    estrellas  TINYINT UNSIGNED NOT NULL,
    CONSTRAINT pk_hotel PRIMARY KEY (id_hotel),
    CONSTRAINT ck_hotel_estrellas CHECK (estrellas BETWEEN 1 AND 5),
    INDEX idx_hotel_ciudad (ciudad)
) ENGINE = InnoDB;

CREATE TABLE habitacion (
    id_habitacion INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_hotel      INT UNSIGNED NOT NULL,
    numero        VARCHAR(10)  NOT NULL,
    planta        SMALLINT     NOT NULL,
    categoria     VARCHAR(20)  NOT NULL,
    precio_noche  DECIMAL(8,2) NOT NULL,
    en_servicio   BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_habitacion PRIMARY KEY (id_habitacion),
    CONSTRAINT uq_habitacion_numero UNIQUE (id_hotel, numero),
    CONSTRAINT fk_habitacion_hotel FOREIGN KEY (id_hotel) REFERENCES hotel (id_hotel)
        ON DELETE CASCADE ON UPDATE CASCADE,                      -- composición Hotel ◆ Habitacion
    CONSTRAINT fk_habitacion_categoria FOREIGN KEY (categoria) REFERENCES categoria_habitacion (nombre)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_habitacion_precio CHECK (precio_noche > 0)
) ENGINE = InnoDB;

-- Persona es abstracta: se mapea una tabla por clase concreta (cliente, empleado).
CREATE TABLE cliente (
    id_cliente     INT UNSIGNED NOT NULL AUTO_INCREMENT,
    dni            VARCHAR(15)  NOT NULL,
    nombre         VARCHAR(60)  NOT NULL,
    apellidos      VARCHAR(100) NOT NULL,
    email          VARCHAR(100) NOT NULL,
    telefono       VARCHAR(20)  NULL,
    direccion      VARCHAR(150) NULL,
    fecha_registro DATE         NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
    CONSTRAINT uq_cliente_dni UNIQUE (dni),
    INDEX idx_cliente_nombre (apellidos, nombre)
) ENGINE = InnoDB;

CREATE TABLE empleado (
    id_empleado INT UNSIGNED NOT NULL AUTO_INCREMENT,
    dni         VARCHAR(15)  NOT NULL,
    nombre      VARCHAR(60)  NOT NULL,
    apellidos   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL,
    telefono    VARCHAR(20)  NULL,
    usuario     VARCHAR(30)  NOT NULL,
    rol         ENUM ('RECEPCIONISTA', 'ADMINISTRADOR') NOT NULL,
    id_hotel    INT UNSIGNED NULL,
    CONSTRAINT pk_empleado PRIMARY KEY (id_empleado),
    CONSTRAINT uq_empleado_dni UNIQUE (dni),
    CONSTRAINT uq_empleado_usuario UNIQUE (usuario),
    CONSTRAINT fk_empleado_hotel FOREIGN KEY (id_hotel) REFERENCES hotel (id_hotel)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB;

CREATE TABLE reserva (
    id_reserva     INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_cliente     INT UNSIGNED NOT NULL,
    id_hotel       INT UNSIGNED NOT NULL,
    fecha_entrada  DATE         NOT NULL,
    fecha_salida   DATE         NOT NULL,
    num_personas   TINYINT UNSIGNED NOT NULL,
    estado         ENUM ('PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'FINALIZADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_reserva PRIMARY KEY (id_reserva),
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
        ON DELETE RESTRICT ON UPDATE CASCADE,           -- no se borra un cliente con reservas
    CONSTRAINT fk_reserva_hotel FOREIGN KEY (id_hotel) REFERENCES hotel (id_hotel)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_reserva_fechas CHECK (fecha_salida > fecha_entrada),
    CONSTRAINT ck_reserva_personas CHECK (num_personas > 0),
    INDEX idx_reserva_fechas (id_hotel, fecha_entrada, fecha_salida),
    INDEX idx_reserva_cliente (id_cliente)
) ENGINE = InnoDB;

-- Reserva ◆ 1..* LineaReserva. El precio de la noche queda congelado en la línea.
CREATE TABLE linea_reserva (
    id_reserva    INT UNSIGNED NOT NULL,
    id_habitacion INT UNSIGNED NOT NULL,
    precio_noche  DECIMAL(8,2) NOT NULL,
    CONSTRAINT pk_linea_reserva PRIMARY KEY (id_reserva, id_habitacion),
    CONSTRAINT fk_linea_reserva FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_linea_habitacion FOREIGN KEY (id_habitacion) REFERENCES habitacion (id_habitacion)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_linea_precio CHECK (precio_noche > 0),
    INDEX idx_linea_habitacion (id_habitacion)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
--  PROCEDIMIENTOS ALMACENADOS
--
--  Códigos de error propios (SIGNAL ... MYSQL_ERRNO) que Java traduce a
--  excepciones de negocio:
--    50001 habitación no disponible      50004 cliente con reservas
--    50002 elemento no encontrado        50005 datos no válidos
--    50003 operación no permitida por el estado
--
--  Los procedimientos que modifican datos NO abren su propia transacción:
--  se ejecutan dentro de la transacción que controla JDBC (commit/rollback
--  en la capa DAO), de modo que varias llamadas forman una única unidad.
-- ---------------------------------------------------------------------
DELIMITER $$

-- Crea la cabecera de la reserva (estado CONFIRMADA). Devuelve su id.
CREATE PROCEDURE sp_crear_reserva (
    IN  p_cliente  INT UNSIGNED, IN p_hotel INT UNSIGNED,
    IN  p_entrada  DATE,         IN p_salida DATE,
    IN  p_personas INT,
    OUT p_id_reserva INT UNSIGNED)
BEGIN
    IF p_salida <= p_entrada THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50005,
            MESSAGE_TEXT = 'La fecha de salida debe ser posterior a la de entrada.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM cliente WHERE id_cliente = p_cliente) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50002, MESSAGE_TEXT = 'No existe el cliente indicado.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM hotel WHERE id_hotel = p_hotel) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50002, MESSAGE_TEXT = 'No existe el hotel indicado.';
    END IF;
    INSERT INTO reserva (id_cliente, id_hotel, fecha_entrada, fecha_salida, num_personas, estado)
    VALUES (p_cliente, p_hotel, p_entrada, p_salida, p_personas, 'CONFIRMADA');
    SET p_id_reserva = LAST_INSERT_ID();
END$$

-- Añade una habitación a la reserva. Bloquea la fila de la habitación (FOR UPDATE)
-- para que dos reservas simultáneas no puedan ocupar la misma habitación y fechas.
CREATE PROCEDURE sp_agregar_linea_reserva (
    IN p_reserva INT UNSIGNED, IN p_habitacion INT UNSIGNED)
BEGIN
    DECLARE v_msg VARCHAR(255);
    DECLARE v_hotel_reserva INT UNSIGNED;
    DECLARE v_entrada DATE;
    DECLARE v_salida  DATE;
    DECLARE v_hotel_hab INT UNSIGNED;
    DECLARE v_numero  VARCHAR(10);
    DECLARE v_precio  DECIMAL(8,2);
    DECLARE v_servicio BOOLEAN;

    SELECT id_hotel, fecha_entrada, fecha_salida
      INTO v_hotel_reserva, v_entrada, v_salida
      FROM reserva WHERE id_reserva = p_reserva;

    SELECT id_hotel, numero, precio_noche, en_servicio
      INTO v_hotel_hab, v_numero, v_precio, v_servicio
      FROM habitacion WHERE id_habitacion = p_habitacion FOR UPDATE;

    IF v_hotel_hab IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50002, MESSAGE_TEXT = 'No existe la habitación indicada.';
    END IF;
    IF v_hotel_hab <> v_hotel_reserva THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50005,
            MESSAGE_TEXT = 'La habitación no pertenece al hotel de la reserva.';
    END IF;
    IF NOT v_servicio THEN
        SET v_msg = CONCAT('La habitación ', v_numero, ' está fuera de servicio.');
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50003, MESSAGE_TEXT = v_msg;
    END IF;
    -- Lectura bajo el bloqueo de la fila de la habitación: solapa si otra reserva no cancelada
    -- ocupa [entrada, salida) (la salida de una no cuenta como noche ocupada).
    IF EXISTS (SELECT 1 FROM linea_reserva lo
                  JOIN reserva ro ON ro.id_reserva = lo.id_reserva
                 WHERE lo.id_habitacion = p_habitacion
                   AND ro.id_reserva <> p_reserva
                   AND ro.estado <> 'CANCELADA'
                   AND ro.fecha_entrada < v_salida
                   AND v_entrada < ro.fecha_salida) THEN
        SET v_msg = CONCAT('La habitación ', v_numero, ' no está disponible del ',
                                  v_entrada, ' al ', v_salida, '.');
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50001, MESSAGE_TEXT = v_msg;
    END IF;
    INSERT INTO linea_reserva (id_reserva, id_habitacion, precio_noche)
    VALUES (p_reserva, p_habitacion, v_precio);
END$$

-- Cambia fechas y nº de personas de una reserva activa, revalidando cada habitación.
CREATE PROCEDURE sp_modificar_reserva (
    IN p_reserva INT UNSIGNED, IN p_entrada DATE, IN p_salida DATE, IN p_personas INT)
BEGIN
    DECLARE v_msg VARCHAR(255);
    DECLARE v_estado VARCHAR(20);
    DECLARE v_capacidad INT;
    DECLARE v_numero VARCHAR(10);
    DECLARE v_bloqueadas INT;

    IF p_salida <= p_entrada THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50005,
            MESSAGE_TEXT = 'La fecha de salida debe ser posterior a la de entrada.';
    END IF;

    SELECT estado INTO v_estado FROM reserva WHERE id_reserva = p_reserva FOR UPDATE;
    IF v_estado IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50002, MESSAGE_TEXT = 'No existe la reserva indicada.';
    END IF;
    IF v_estado IN ('CANCELADA', 'FINALIZADA') THEN
        SET v_msg = CONCAT('La reserva ', p_reserva, ' está ', v_estado, ' y no se puede modificar.');
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50003, MESSAGE_TEXT = v_msg;
    END IF;

    SELECT SUM(c.capacidad_maxima) INTO v_capacidad
      FROM linea_reserva l
      JOIN habitacion h ON h.id_habitacion = l.id_habitacion
      JOIN categoria_habitacion c ON c.nombre = h.categoria
     WHERE l.id_reserva = p_reserva;
    IF p_personas <= 0 OR p_personas > v_capacidad THEN
        SET v_msg = CONCAT('El número de personas debe estar entre 1 y ', v_capacidad,
                                  ' para las habitaciones de la reserva.');
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50005, MESSAGE_TEXT = v_msg;
    END IF;

    -- Bloquea las habitaciones de la reserva y comprueba que siguen libres en las nuevas fechas.
    SELECT COUNT(*) INTO v_bloqueadas
      FROM habitacion
     WHERE id_habitacion IN (SELECT id_habitacion FROM linea_reserva WHERE id_reserva = p_reserva)
       FOR UPDATE;
    SELECT h.numero INTO v_numero
      FROM linea_reserva l
      JOIN habitacion h ON h.id_habitacion = l.id_habitacion
     WHERE l.id_reserva = p_reserva
       AND EXISTS (SELECT 1 FROM linea_reserva lo
                  JOIN reserva ro ON ro.id_reserva = lo.id_reserva
                 WHERE lo.id_habitacion = l.id_habitacion
                   AND ro.id_reserva <> p_reserva
                   AND ro.estado <> 'CANCELADA'
                   AND ro.fecha_entrada < p_salida
                   AND p_entrada < ro.fecha_salida)
     ORDER BY h.id_habitacion
     LIMIT 1;
    IF v_numero IS NOT NULL THEN
        SET v_msg = CONCAT('La habitación ', v_numero, ' no está disponible del ',
                                  p_entrada, ' al ', p_salida, '.');
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50001, MESSAGE_TEXT = v_msg;
    END IF;

    UPDATE reserva
       SET fecha_entrada = p_entrada, fecha_salida = p_salida, num_personas = p_personas
     WHERE id_reserva = p_reserva;
END$$

-- Cambia el estado de una reserva respetando las transiciones permitidas.
--   p_nuevo = 'CANCELADA'  : desde PENDIENTE o CONFIRMADA
--   p_nuevo = 'FINALIZADA' : solo desde CONFIRMADA
CREATE PROCEDURE sp_cambiar_estado_reserva (IN p_reserva INT UNSIGNED, IN p_nuevo VARCHAR(20))
BEGIN
    DECLARE v_msg VARCHAR(255);
    DECLARE v_estado VARCHAR(20);
    SELECT estado INTO v_estado FROM reserva WHERE id_reserva = p_reserva FOR UPDATE;
    IF v_estado IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50002, MESSAGE_TEXT = 'No existe la reserva indicada.';
    END IF;
    IF p_nuevo = 'CANCELADA' AND v_estado IN ('CANCELADA', 'FINALIZADA') THEN
        SET v_msg = CONCAT('La reserva ', p_reserva, ' está ', v_estado, ' y no se puede modificar.');
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50003, MESSAGE_TEXT = v_msg;
    ELSEIF p_nuevo = 'FINALIZADA' AND v_estado <> 'CONFIRMADA' THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50003,
            MESSAGE_TEXT = 'Solo se pueden finalizar reservas confirmadas.';
    ELSEIF p_nuevo NOT IN ('CANCELADA', 'FINALIZADA') THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50005, MESSAGE_TEXT = 'Estado de destino no válido.';
    END IF;
    UPDATE reserva SET estado = p_nuevo WHERE id_reserva = p_reserva;
END$$

-- Elimina un cliente solo si no tiene reservas.
CREATE PROCEDURE sp_eliminar_cliente (IN p_cliente INT UNSIGNED)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM cliente WHERE id_cliente = p_cliente FOR UPDATE) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50002, MESSAGE_TEXT = 'No existe el cliente indicado.';
    END IF;
    IF EXISTS (SELECT 1 FROM reserva WHERE id_cliente = p_cliente) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50004,
            MESSAGE_TEXT = 'No se puede eliminar el cliente porque tiene reservas asociadas.';
    END IF;
    DELETE FROM cliente WHERE id_cliente = p_cliente;
END$$

-- Habitaciones en servicio de un hotel libres en [entrada, salida); p_personas = 0 no filtra.
CREATE PROCEDURE sp_habitaciones_disponibles (
    IN p_hotel INT UNSIGNED, IN p_entrada DATE, IN p_salida DATE, IN p_personas INT)
BEGIN
    IF p_salida <= p_entrada THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 50005,
            MESSAGE_TEXT = 'La fecha de salida debe ser posterior a la de entrada.';
    END IF;
    SELECT h.id_habitacion, h.id_hotel, h.numero, h.planta, h.categoria, h.precio_noche, h.en_servicio
      FROM habitacion h
      JOIN categoria_habitacion c ON c.nombre = h.categoria
     WHERE h.id_hotel = p_hotel
       AND h.en_servicio
       AND (p_personas <= 0 OR c.capacidad_maxima >= p_personas)
       AND NOT EXISTS (SELECT 1 FROM linea_reserva lo
                  JOIN reserva ro ON ro.id_reserva = lo.id_reserva
                 WHERE lo.id_habitacion = h.id_habitacion
                   AND ro.id_reserva <> 0
                   AND ro.estado <> 'CANCELADA'
                   AND ro.fecha_entrada < p_salida
                   AND p_entrada < ro.fecha_salida)
     ORDER BY h.id_habitacion;
END$$

DELIMITER ;
