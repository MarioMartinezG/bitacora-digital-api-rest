-- Crear esquema dedicado
CREATE SCHEMA IF NOT EXISTS teia;

-- Tabla de roles
CREATE TABLE teia.roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

-- Tabla de usuarios
CREATE TABLE teia.usuarios (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    rol_id INT REFERENCES teia.roles(id),
    fecha_creacion TIMESTAMP DEFAULT NOW()
);

-- Tabla de módulos del curso
CREATE TABLE teia.modulos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    orden INT NOT NULL,
    total_preguntas INT DEFAULT 0
);

-- Tabla de preguntas por módulo
CREATE TABLE teia.preguntas (
    id SERIAL PRIMARY KEY,
    modulo_id INT NOT NULL REFERENCES teia.modulos(id) ON DELETE CASCADE,
    enunciado TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('texto', 'numero', 'lista', 'opcion_multiple'))
);

-- Tabla de respuestas de estudiantes
CREATE TABLE teia.respuestas (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    pregunta_id INT NOT NULL REFERENCES teia.preguntas(id) ON DELETE CASCADE,
    respuesta TEXT,
    fecha_respuesta TIMESTAMP DEFAULT NOW(),
    UNIQUE (usuario_id, pregunta_id)
);

-- Tabla para seguimiento del progreso de cada módulo (semáforo)
CREATE TABLE teia.progreso_modulos (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    modulo_id INT NOT NULL REFERENCES teia.modulos(id) ON DELETE CASCADE,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('sin_avances', 'en_desarrollo', 'completado')),
    ultima_actualizacion TIMESTAMP DEFAULT NOW(),
    UNIQUE (usuario_id, modulo_id)
);

-- Tabla para progreso general del curso
CREATE TABLE teia.progreso_curso (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('sin_avances', 'en_desarrollo', 'completado')),
    porcentaje NUMERIC(5,2) DEFAULT 0.0,
    ultima_actualizacion TIMESTAMP DEFAULT NOW(),
    UNIQUE (usuario_id)
);

-- Tabla de menús principales
CREATE TABLE teia.menus (
    id SERIAL PRIMARY KEY,
    label VARCHAR(150) NOT NULL,
    icon VARCHAR(100),
    orden INT NOT NULL,
    router_link VARCHAR(200)
);

-- Tabla de submenús
CREATE TABLE teia.menu_items (
    id SERIAL PRIMARY KEY,
    menu_id INT NOT NULL REFERENCES teia.menus(id) ON DELETE CASCADE,
    label VARCHAR(150) NOT NULL,
    icon VARCHAR(100),
    router_link VARCHAR(200),
    orden INT NOT NULL
);

-- Relación Menú ↔ Roles
CREATE TABLE teia.menu_roles (
    menu_id INT NOT NULL REFERENCES teia.menus(id) ON DELETE CASCADE,
    rol_id INT NOT NULL REFERENCES teia.roles(id) ON DELETE CASCADE,
    PRIMARY KEY (menu_id, rol_id)
);

-- Relación MenuItem ↔ Roles
CREATE TABLE teia.menu_item_roles (
    menu_item_id INT NOT NULL REFERENCES teia.menu_items(id) ON DELETE CASCADE,
    rol_id INT NOT NULL REFERENCES teia.roles(id) ON DELETE CASCADE,
    PRIMARY KEY (menu_item_id, rol_id)
);

-- Índices adicionales
-- CREATE INDEX idx_respuestas_usuario ON teia.respuestas(usuario_id);
-- CREATE INDEX idx_respuestas_pregunta ON teia.respuestas(pregunta_id);
-- CREATE INDEX idx_progreso_modulos_usuario ON teia.progreso_modulos(usuario_id);
-- CREATE INDEX idx_items_roles ON teia.menu_items USING GIN (roles);
-- CREATE INDEX idx_menus_roles ON teia.menus USING GIN (roles);