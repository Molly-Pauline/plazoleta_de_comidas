-- Esquema MySQL 8 para las entidades JPA actuales de Usuarios y Plazoleta.
-- Ambos microservicios comparten la base plazoleta_bd, pero conservan sus propias tablas.
-- Ejecutar en una base nueva o vacia. Este script no borra ni migra tablas existentes.

CREATE DATABASE IF NOT EXISTS plazoleta_bd
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE plazoleta_bd;

-- Usuarios usa Integer para su ID; el rol se persiste como texto (EnumType.STRING).
CREATE TABLE IF NOT EXISTS usuario (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NULL,
    documento_de_identidad VARCHAR(255) NULL,
    celular VARCHAR(255) NULL,
    fecha_de_nacimiento TIMESTAMP(6) NULL,
    correo VARCHAR(255) NOT NULL,
    clave VARCHAR(255) NULL,
    rol VARCHAR(255) NULL,
    id_restaurante BIGINT NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_documento UNIQUE (documento_de_identidad),
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    INDEX idx_usuario_rol (rol),
    INDEX idx_usuario_restaurante (id_restaurante)
) ENGINE = InnoDB;

-- id_propietario es una referencia lógica al ID de Usuarios, validada por HTTP.
-- No se declara FK porque Usuarios.id es INT y este atributo Java es Long/BIGINT,
-- y porque los microservicios no deben depender de la tabla interna del otro.
CREATE TABLE IF NOT EXISTS restaurantes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    nit VARCHAR(20) NOT NULL,
    direccion VARCHAR(240) NOT NULL,
    telefono VARCHAR(13) NOT NULL,
    url_logo VARCHAR(500) NOT NULL,
    id_propietario BIGINT NOT NULL,
    CONSTRAINT pk_restaurantes PRIMARY KEY (id),
    CONSTRAINT uq_restaurantes_nit UNIQUE (nit),
    INDEX idx_restaurantes_nombre (nombre),
    INDEX idx_restaurantes_propietario (id_propietario)
) ENGINE = InnoDB;

-- La categoria es texto en la entidad Plato; no se usa una tabla categoria.
CREATE TABLE IF NOT EXISTS platos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    precio INT NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    url_imagen VARCHAR(500) NOT NULL,
    categoria VARCHAR(80) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    id_restaurante BIGINT NOT NULL,
    CONSTRAINT pk_platos PRIMARY KEY (id),
    CONSTRAINT chk_platos_precio CHECK (precio > 0),
    CONSTRAINT fk_platos_restaurante
        FOREIGN KEY (id_restaurante) REFERENCES restaurantes (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_platos_menu (id_restaurante, activo, categoria)
) ENGINE = InnoDB;

-- estado se almacena con el nombre del enum, por ejemplo PENDIENTE o LISTO.
-- id_cliente e id_empleado son referencias lógicas al microservicio Usuarios.
CREATE TABLE IF NOT EXISTS pedidos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    id_cliente BIGINT NOT NULL,
    id_empleado BIGINT NULL,
    id_restaurante BIGINT NOT NULL,
    estado VARCHAR(24) NOT NULL,
    fecha_creacion TIMESTAMP(6) NOT NULL,
    total BIGINT NOT NULL,
    CONSTRAINT pk_pedidos PRIMARY KEY (id),
    CONSTRAINT chk_pedidos_total CHECK (total >= 0),
    CONSTRAINT fk_pedidos_restaurante
        FOREIGN KEY (id_restaurante) REFERENCES restaurantes (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_pedidos_cliente_estado (id_cliente, estado),
    INDEX idx_pedidos_restaurante_estado (id_restaurante, estado)
) ENGINE = InnoDB;

-- precio_unitario conserva el precio historico aunque el plato cambie despues.
CREATE TABLE IF NOT EXISTS pedido_detalles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    id_pedido BIGINT NOT NULL,
    id_plato BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario INT NOT NULL,
    CONSTRAINT pk_pedido_detalles PRIMARY KEY (id),
    CONSTRAINT uq_pedido_detalles_pedido_plato UNIQUE (id_pedido, id_plato),
    CONSTRAINT chk_pedido_detalles_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_pedido_detalles_precio CHECK (precio_unitario > 0),
    CONSTRAINT fk_pedido_detalles_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedidos (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_pedido_detalles_plato
        FOREIGN KEY (id_plato) REFERENCES platos (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_pedido_detalles_plato (id_plato)
) ENGINE = InnoDB;

-- No hay tablas rol, categoria ni empleado_restaurante: el codigo actual utiliza
-- un enum de roles, categoria como texto y usuario.id_restaurante para empleados.