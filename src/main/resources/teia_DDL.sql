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
    total_secciones INT DEFAULT 0,
    tipo_estructura VARCHAR(50) DEFAULT 'simple',
    instrucciones TEXT,
    fecha_inicio DATE,
    fecha_fin DATE,
    UNIQUE (orden)
);

-- Tabla de secciones dentro de cada módulo
CREATE TABLE teia.secciones (
    id SERIAL PRIMARY KEY,
    modulo_id INT NOT NULL REFERENCES teia.modulos(id) ON DELETE CASCADE,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    tipo_seccion VARCHAR(50) NOT NULL CHECK (tipo_seccion IN (
        'formulario', 'tabla', 'lista_temas', 'opciones_si_no', 'texto_largo'
    )),
    estructura_json JSONB, -- Para definir campos dinámicos
    orden INT NOT NULL,
    tiene_estado BOOLEAN DEFAULT true,
    es_obligatorio BOOLEAN DEFAULT true,
    configuracion JSONB, -- Configuraciones adicionales específicas
    UNIQUE (modulo_id, orden)
);

-- Tabla de campos/preguntas dentro de cada sección (NUEVA)
CREATE TABLE teia.campos_seccion (
    id SERIAL PRIMARY KEY,
    seccion_id INT NOT NULL REFERENCES teia.secciones(id) ON DELETE CASCADE,
    label VARCHAR(500) NOT NULL,
    tipo_campo VARCHAR(50) NOT NULL CHECK (tipo_campo IN (
        'texto', 'numero', 'lista', 'opcion_multiple',
        'si_no', 'tabla', 'lista_temas', 'textarea', 'seleccion'
    )),
    opciones JSONB, -- Para almacenar opciones en formato JSON: ["opcion1", "opcion2"]
    es_requerido BOOLEAN DEFAULT false,
    orden INT NOT NULL,
    configuracion JSONB, -- { "max_temas": 4, "max_subtemas": 4, "columnas": ["col1", "col2"] }
    placeholder TEXT,
    UNIQUE (seccion_id, orden)
);

-- Tabla de respuestas (MODIFICADA - Más flexible)
CREATE TABLE teia.respuestas (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    seccion_id INT REFERENCES teia.secciones(id) ON DELETE CASCADE,
    campo_id INT REFERENCES teia.campos_seccion(id) ON DELETE CASCADE,
    respuesta_texto TEXT,
    respuesta_json JSONB, -- Para respuestas complejas (tablas, listas, objetos)
    estado_avance VARCHAR(20) DEFAULT 'sin_avances' CHECK (estado_avance IN ('sin_avances', 'en_desarrollo', 'completado')),
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW()
);

-- Tabla de estado por sección (NUEVA - Para el semáforo)
CREATE TABLE teia.estado_secciones (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    seccion_id INT NOT NULL REFERENCES teia.secciones(id) ON DELETE CASCADE,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('sin_avances', 'en_desarrollo', 'completado')),
    fecha_actualizacion TIMESTAMP DEFAULT NOW(),
    UNIQUE (usuario_id, seccion_id)
);

-- Tabla para seguimiento del progreso de cada módulo (MODIFICADA)
CREATE TABLE teia.progreso_modulos (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    modulo_id INT NOT NULL REFERENCES teia.modulos(id) ON DELETE CASCADE,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('sin_avances', 'en_desarrollo', 'completado')),
    secciones_completadas INT DEFAULT 0,
    total_secciones INT DEFAULT 0,
    porcentaje_completado NUMERIC(5,2) DEFAULT 0.0,
    ultima_actualizacion TIMESTAMP DEFAULT NOW(),
    UNIQUE (usuario_id, modulo_id)
);

-- Tabla para progreso general del curso (MODIFICADA)
CREATE TABLE teia.progreso_curso (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('sin_avances', 'en_desarrollo', 'completado')),
    porcentaje NUMERIC(5,2) DEFAULT 0.0,
    modulos_completados INT DEFAULT 0,
    total_modulos INT DEFAULT 0,
    ultima_actualizacion TIMESTAMP DEFAULT NOW(),
    UNIQUE (usuario_id)
);

-- Tabla de retroalimentación de tutores (NUEVA)
CREATE TABLE teia.retroalimentacion (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE, -- Estudiante
    seccion_id INT REFERENCES teia.secciones(id) ON DELETE CASCADE,
    tutor_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    comentario TEXT NOT NULL,
    estado_anterior VARCHAR(20),
    estado_nuevo VARCHAR(20),
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_revision TIMESTAMP DEFAULT NOW()
);

-- Tabla para estado de revisión del tutor (NUEVA)
CREATE TABLE teia.estado_revision (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE, -- Estudiante
    modulo_id INT NOT NULL REFERENCES teia.modulos(id) ON DELETE CASCADE,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('sin_revisar', 'en_revision', 'revisado', 'requiere_ajustes')),
    comentario_tutor TEXT,
    tutor_id INT REFERENCES teia.usuarios(id),
    fecha_revision TIMESTAMP,
    UNIQUE (usuario_id, modulo_id)
);

-- Tabla para relación Tutor-Estudiante (NUEVA)
CREATE TABLE teia.tutor_estudiante (
    id SERIAL PRIMARY KEY,
    tutor_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    estudiante_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    fecha_asignacion TIMESTAMP DEFAULT NOW(),
    activo BOOLEAN DEFAULT true,
    UNIQUE (tutor_id, estudiante_id)
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
CREATE INDEX idx_respuestas_usuario ON teia.respuestas(usuario_id);
CREATE INDEX idx_respuestas_seccion ON teia.respuestas(seccion_id);
CREATE INDEX idx_respuestas_campo ON teia.respuestas(campo_id);
CREATE INDEX idx_estado_secciones_usuario ON teia.estado_secciones(usuario_id);
CREATE INDEX idx_estado_secciones_seccion ON teia.estado_secciones(seccion_id);
CREATE INDEX idx_progreso_modulos_usuario ON teia.progreso_modulos(usuario_id);
CREATE INDEX idx_progreso_curso_usuario ON teia.progreso_curso(usuario_id);
CREATE INDEX idx_estado_revision_tutor ON teia.estado_revision(tutor_id);
CREATE INDEX idx_tutor_estudiante_tutor ON teia.tutor_estudiante(tutor_id);
CREATE INDEX idx_tutor_estudiante_estudiante ON teia.tutor_estudiante(estudiante_id);
CREATE INDEX idx_secciones_modulo ON teia.secciones(modulo_id);
CREATE INDEX idx_campos_seccion ON teia.campos_seccion(seccion_id);