-- =============================================
-- SCRIPT DDL - BITÁCORA DIGITAL TEIA
-- Versión 2.0 - Arquitectura simplificada
-- =============================================

-- Crear esquema dedicado
CREATE SCHEMA IF NOT EXISTS teia;

-- =============================================
-- TABLAS ACTIVAS - Autenticación y Menús
-- =============================================

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

-- =============================================
-- TABLAS ACTIVAS - Nueva arquitectura v2.0
-- El frontend define la estructura de formularios
-- El backend solo persiste respuestas de forma flexible
-- =============================================

-- Respuestas de secciones (formularios JSON)
-- Almacena todas las respuestas de una sección en un solo registro JSONB
CREATE TABLE IF NOT EXISTS teia.respuestas_seccion (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    seccion_codigo VARCHAR(100) NOT NULL,
    datos JSONB NOT NULL DEFAULT '{}',
    estado_avance VARCHAR(20) DEFAULT 'sin_avances'
        CHECK (estado_avance IN ('sin_avances', 'en_desarrollo', 'completado')),
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW(),
    UNIQUE (usuario_id, seccion_codigo)
);

-- Equipo docente (CRUD individual)
-- Cada miembro del equipo docente es un registro separado
CREATE TABLE IF NOT EXISTS teia.equipo_docente (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    nombre VARCHAR(200) NOT NULL,
    correo VARCHAR(150),
    rol VARCHAR(100),
    atencion VARCHAR(50),
    dias JSONB DEFAULT '[]',
    hora_inicio TIME,
    hora_fin TIME,
    horario VARCHAR(200),
    sitio VARCHAR(200),
    orden INT DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW()
);

-- Temas y subtemas (CRUD jerárquico)
-- Cada tema es un registro, con subtemas almacenados en JSONB
CREATE TABLE IF NOT EXISTS teia.temas_contenido (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    numero_tema INT NOT NULL,
    nombre_tema VARCHAR(500) NOT NULL,
    subtemas JSONB DEFAULT '[]',
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW(),
    UNIQUE (usuario_id, numero_tema)
);

-- Progreso por sección (simplificado)
-- Tracking del estado de avance por sección usando códigos string
CREATE TABLE IF NOT EXISTS teia.progreso_secciones (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    seccion_codigo VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'sin_avances'
        CHECK (estado IN ('sin_avances', 'en_desarrollo', 'completado')),
    porcentaje_completado INT DEFAULT 0,
    fecha_actualizacion TIMESTAMP DEFAULT NOW(),
    UNIQUE (usuario_id, seccion_codigo)
);

-- =============================================
-- TABLAS OBSOLETAS - Arquitectura anterior v1.0
-- @deprecated Desde versión 2.0
-- Comentadas para referencia histórica
-- =============================================

-- -- Tabla de módulos del curso
-- CREATE TABLE teia.modulos (
--     id SERIAL PRIMARY KEY,
--     nombre VARCHAR(150) NOT NULL,
--     descripcion TEXT,
--     orden INT NOT NULL,
--     total_secciones INT DEFAULT 0,
--     tipo_estructura VARCHAR(50) DEFAULT 'simple',
--     instrucciones TEXT,
--     fecha_inicio DATE,
--     fecha_fin DATE,
--     UNIQUE (orden)
-- );

-- -- Tabla de secciones dentro de cada módulo
-- CREATE TABLE teia.secciones (
--     id SERIAL PRIMARY KEY,
--     modulo_id INT NOT NULL REFERENCES teia.modulos(id) ON DELETE CASCADE,
--     nombre VARCHAR(200) NOT NULL,
--     descripcion TEXT,
--     tipo_seccion VARCHAR(50) NOT NULL CHECK (tipo_seccion IN (
--         'formulario', 'tabla', 'lista_temas', 'opciones_si_no', 'texto_largo'
--     )),
--     estructura_json JSONB,
--     orden INT NOT NULL,
--     tiene_estado BOOLEAN DEFAULT true,
--     es_obligatorio BOOLEAN DEFAULT true,
--     configuracion JSONB,
--     UNIQUE (modulo_id, orden)
-- );

-- -- Tabla de campos/preguntas dentro de cada sección
-- CREATE TABLE teia.campos_seccion (
--     id SERIAL PRIMARY KEY,
--     seccion_id INT NOT NULL REFERENCES teia.secciones(id) ON DELETE CASCADE,
--     label VARCHAR(500) NOT NULL,
--     tipo_campo VARCHAR(50) NOT NULL CHECK (tipo_campo IN (
--         'texto', 'numero', 'lista', 'opcion_multiple',
--         'si_no', 'tabla', 'lista_temas', 'textarea', 'seleccion'
--     )),
--     opciones JSONB,
--     es_requerido BOOLEAN DEFAULT false,
--     orden INT NOT NULL,
--     configuracion JSONB,
--     placeholder TEXT,
--     UNIQUE (seccion_id, orden)
-- );

-- -- Tabla de respuestas (versión anterior)
-- CREATE TABLE teia.respuestas (
--     id SERIAL PRIMARY KEY,
--     usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
--     seccion_id INT REFERENCES teia.secciones(id) ON DELETE CASCADE,
--     campo_id INT REFERENCES teia.campos_seccion(id) ON DELETE CASCADE,
--     respuesta_texto TEXT,
--     respuesta_json JSONB,
--     estado_avance VARCHAR(20) DEFAULT 'sin_avances' CHECK (estado_avance IN ('sin_avances', 'en_desarrollo', 'completado')),
--     fecha_creacion TIMESTAMP DEFAULT NOW(),
--     fecha_actualizacion TIMESTAMP DEFAULT NOW()
-- );

-- -- Tabla de estado por sección (para el semáforo - versión anterior)
-- CREATE TABLE teia.estado_secciones (
--     id SERIAL PRIMARY KEY,
--     usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
--     seccion_id INT NOT NULL REFERENCES teia.secciones(id) ON DELETE CASCADE,
--     estado VARCHAR(20) NOT NULL CHECK (estado IN ('sin_avances', 'en_desarrollo', 'completado')),
--     fecha_actualizacion TIMESTAMP DEFAULT NOW(),
--     UNIQUE (usuario_id, seccion_id)
-- );

-- -- Tabla para seguimiento del progreso de cada módulo
-- CREATE TABLE teia.progreso_modulos (
--     id SERIAL PRIMARY KEY,
--     usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
--     modulo_id INT NOT NULL REFERENCES teia.modulos(id) ON DELETE CASCADE,
--     estado VARCHAR(20) NOT NULL CHECK (estado IN ('sin_avances', 'en_desarrollo', 'completado')),
--     secciones_completadas INT DEFAULT 0,
--     total_secciones INT DEFAULT 0,
--     porcentaje_completado NUMERIC(5,2) DEFAULT 0.0,
--     ultima_actualizacion TIMESTAMP DEFAULT NOW(),
--     UNIQUE (usuario_id, modulo_id)
-- );

-- -- Tabla para progreso general del curso
-- CREATE TABLE teia.progreso_curso (
--     id SERIAL PRIMARY KEY,
--     usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
--     estado VARCHAR(20) NOT NULL CHECK (estado IN ('sin_avances', 'en_desarrollo', 'completado')),
--     porcentaje NUMERIC(5,2) DEFAULT 0.0,
--     modulos_completados INT DEFAULT 0,
--     total_modulos INT DEFAULT 0,
--     ultima_actualizacion TIMESTAMP DEFAULT NOW(),
--     UNIQUE (usuario_id)
-- );

-- -- Tabla de retroalimentación de tutores
-- CREATE TABLE teia.retroalimentacion (
--     id SERIAL PRIMARY KEY,
--     usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
--     seccion_id INT REFERENCES teia.secciones(id) ON DELETE CASCADE,
--     tutor_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
--     comentario TEXT NOT NULL,
--     estado_anterior VARCHAR(20),
--     estado_nuevo VARCHAR(20),
--     fecha_creacion TIMESTAMP DEFAULT NOW(),
--     fecha_revision TIMESTAMP DEFAULT NOW()
-- );

-- -- Tabla para estado de revisión del tutor
-- CREATE TABLE teia.estado_revision (
--     id SERIAL PRIMARY KEY,
--     usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
--     modulo_id INT NOT NULL REFERENCES teia.modulos(id) ON DELETE CASCADE,
--     estado VARCHAR(20) NOT NULL CHECK (estado IN ('sin_revisar', 'en_revision', 'revisado', 'requiere_ajustes')),
--     comentario_tutor TEXT,
--     tutor_id INT REFERENCES teia.usuarios(id),
--     fecha_revision TIMESTAMP,
--     UNIQUE (usuario_id, modulo_id)
-- );

-- -- Tabla para relación Tutor-Estudiante
-- CREATE TABLE teia.tutor_estudiante (
--     id SERIAL PRIMARY KEY,
--     tutor_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
--     estudiante_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
--     fecha_asignacion TIMESTAMP DEFAULT NOW(),
--     activo BOOLEAN DEFAULT true,
--     UNIQUE (tutor_id, estudiante_id)
-- );

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

-- =============================================
-- ÍNDICES ACTIVOS - Nueva arquitectura v2.0
-- =============================================

CREATE INDEX IF NOT EXISTS idx_respuestas_seccion_usuario ON teia.respuestas_seccion(usuario_id);
CREATE INDEX IF NOT EXISTS idx_respuestas_seccion_codigo ON teia.respuestas_seccion(seccion_codigo);
CREATE INDEX IF NOT EXISTS idx_equipo_docente_usuario ON teia.equipo_docente(usuario_id);
CREATE INDEX IF NOT EXISTS idx_temas_contenido_usuario ON teia.temas_contenido(usuario_id);
CREATE INDEX IF NOT EXISTS idx_progreso_secciones_usuario ON teia.progreso_secciones(usuario_id);
CREATE INDEX IF NOT EXISTS idx_progreso_secciones_codigo ON teia.progreso_secciones(seccion_codigo);

-- =============================================
-- ÍNDICES OBSOLETOS - Arquitectura anterior v1.0
-- @deprecated Desde versión 2.0
-- =============================================

-- CREATE INDEX idx_respuestas_usuario ON teia.respuestas(usuario_id);
-- CREATE INDEX idx_respuestas_seccion ON teia.respuestas(seccion_id);
-- CREATE INDEX idx_respuestas_campo ON teia.respuestas(campo_id);
-- CREATE INDEX idx_estado_secciones_usuario ON teia.estado_secciones(usuario_id);
-- CREATE INDEX idx_estado_secciones_seccion ON teia.estado_secciones(seccion_id);
-- CREATE INDEX idx_progreso_modulos_usuario ON teia.progreso_modulos(usuario_id);
-- CREATE INDEX idx_progreso_curso_usuario ON teia.progreso_curso(usuario_id);
-- CREATE INDEX idx_estado_revision_tutor ON teia.estado_revision(tutor_id);
-- CREATE INDEX idx_tutor_estudiante_tutor ON teia.tutor_estudiante(tutor_id);
-- CREATE INDEX idx_tutor_estudiante_estudiante ON teia.tutor_estudiante(estudiante_id);
-- CREATE INDEX idx_secciones_modulo ON teia.secciones(modulo_id);
-- CREATE INDEX idx_campos_seccion ON teia.campos_seccion(seccion_id);

-- =============================================
-- FUNCIÓN Y TRIGGERS PARA ACTUALIZACIÓN AUTOMÁTICA
-- =============================================

-- Función para actualizar fecha_actualizacion automáticamente
CREATE OR REPLACE FUNCTION teia.update_fecha_actualizacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para respuestas_seccion
DROP TRIGGER IF EXISTS trigger_update_respuestas_seccion ON teia.respuestas_seccion;
CREATE TRIGGER trigger_update_respuestas_seccion
    BEFORE UPDATE ON teia.respuestas_seccion
    FOR EACH ROW EXECUTE FUNCTION teia.update_fecha_actualizacion();

-- Trigger para equipo_docente
DROP TRIGGER IF EXISTS trigger_update_equipo_docente ON teia.equipo_docente;
CREATE TRIGGER trigger_update_equipo_docente
    BEFORE UPDATE ON teia.equipo_docente
    FOR EACH ROW EXECUTE FUNCTION teia.update_fecha_actualizacion();

-- Trigger para temas_contenido
DROP TRIGGER IF EXISTS trigger_update_temas_contenido ON teia.temas_contenido;
CREATE TRIGGER trigger_update_temas_contenido
    BEFORE UPDATE ON teia.temas_contenido
    FOR EACH ROW EXECUTE FUNCTION teia.update_fecha_actualizacion();

-- Trigger para progreso_secciones
DROP TRIGGER IF EXISTS trigger_update_progreso_secciones ON teia.progreso_secciones;
CREATE TRIGGER trigger_update_progreso_secciones
    BEFORE UPDATE ON teia.progreso_secciones
    FOR EACH ROW EXECUTE FUNCTION teia.update_fecha_actualizacion();

-- =============================================
-- COMENTARIOS DE DOCUMENTACIÓN
-- =============================================

COMMENT ON TABLE teia.respuestas_seccion IS 'Almacena respuestas de formularios en formato JSON flexible. Cada sección tiene un único registro por usuario.';
COMMENT ON TABLE teia.equipo_docente IS 'CRUD de miembros del equipo docente. Cada miembro es un registro separado.';
COMMENT ON TABLE teia.temas_contenido IS 'Temas del contenido de la asignatura con subtemas en JSONB.';
COMMENT ON TABLE teia.progreso_secciones IS 'Tracking de progreso por sección usando códigos string en lugar de IDs.';

COMMENT ON COLUMN teia.respuestas_seccion.seccion_codigo IS 'Código único de sección: caracteriza-identificacion, factores-situacionales, etc.';
COMMENT ON COLUMN teia.respuestas_seccion.datos IS 'JSON con todas las respuestas del formulario de la sección.';
COMMENT ON COLUMN teia.equipo_docente.dias IS 'Array JSON de días de atención: ["Lunes", "Martes", ...]';
COMMENT ON COLUMN teia.temas_contenido.subtemas IS 'Array JSON de nombres de subtemas: ["Subtema 1", "Subtema 2", ...]';

-- =============================================
-- TABLAS - Sistema de Notificaciones v2.1
-- =============================================

-- Relación Tutor-Estudiante (activada desde v2.1)
CREATE TABLE IF NOT EXISTS teia.tutor_estudiante (
    id SERIAL PRIMARY KEY,
    tutor_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    estudiante_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    fecha_asignacion TIMESTAMP DEFAULT NOW(),
    activo BOOLEAN DEFAULT true,
    UNIQUE (tutor_id, estudiante_id)
);

-- Tabla de notificaciones principales
CREATE TABLE IF NOT EXISTS teia.notificaciones (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL
        CHECK (tipo IN ('VENCIMIENTO_PROXIMO', 'SOLICITUD_SESION', 'RESPUESTA_SOLICITUD', 'UMBRAL_ALCANZADO')),
    prioridad VARCHAR(20) NOT NULL
        CHECK (prioridad IN ('CRITICO', 'ALERTA', 'INFO', 'SUCCESS')),
    titulo VARCHAR(255) NOT NULL,
    mensaje TEXT NOT NULL,
    datos_adicionales JSONB DEFAULT '{}',
    leida BOOLEAN DEFAULT false,
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_lectura TIMESTAMP,
    entregada_email BOOLEAN DEFAULT false,
    entregada_websocket BOOLEAN DEFAULT false
);

-- Calendario con fechas límite por módulo/sección
CREATE TABLE IF NOT EXISTS teia.calendario_modulos (
    id SERIAL PRIMARY KEY,
    seccion_codigo VARCHAR(100) NOT NULL UNIQUE,
    nombre_modulo VARCHAR(200) NOT NULL,
    fecha_limite DATE NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW()
);

-- Configuración paramétrica de notificaciones
CREATE TABLE IF NOT EXISTS teia.configuracion_notificaciones (
    id SERIAL PRIMARY KEY,
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor VARCHAR(255) NOT NULL,
    descripcion TEXT,
    tipo_dato VARCHAR(20) DEFAULT 'STRING'
        CHECK (tipo_dato IN ('STRING', 'INTEGER', 'BOOLEAN', 'JSON'))
);

-- Solicitudes de sesión tutor-estudiante
CREATE TABLE IF NOT EXISTS teia.solicitudes_sesion (
    id SERIAL PRIMARY KEY,
    estudiante_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    tutor_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    motivo TEXT NOT NULL,
    estado VARCHAR(20) DEFAULT 'PENDIENTE'
        CHECK (estado IN ('PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'COMPLETADA')),
    fecha_solicitud TIMESTAMP DEFAULT NOW(),
    fecha_respuesta TIMESTAMP,
    notas_tutor TEXT
);

-- =============================================
-- ÍNDICES - Sistema de Notificaciones
-- =============================================

CREATE INDEX IF NOT EXISTS idx_tutor_estudiante_tutor ON teia.tutor_estudiante(tutor_id);
CREATE INDEX IF NOT EXISTS idx_tutor_estudiante_estudiante ON teia.tutor_estudiante(estudiante_id);
CREATE INDEX IF NOT EXISTS idx_tutor_estudiante_activo ON teia.tutor_estudiante(activo);

CREATE INDEX IF NOT EXISTS idx_notificaciones_usuario ON teia.notificaciones(usuario_id);
CREATE INDEX IF NOT EXISTS idx_notificaciones_leida ON teia.notificaciones(usuario_id, leida);
CREATE INDEX IF NOT EXISTS idx_notificaciones_tipo ON teia.notificaciones(tipo);
CREATE INDEX IF NOT EXISTS idx_notificaciones_fecha ON teia.notificaciones(fecha_creacion DESC);

CREATE INDEX IF NOT EXISTS idx_calendario_seccion ON teia.calendario_modulos(seccion_codigo);
CREATE INDEX IF NOT EXISTS idx_calendario_fecha ON teia.calendario_modulos(fecha_limite);

CREATE INDEX IF NOT EXISTS idx_solicitudes_estudiante ON teia.solicitudes_sesion(estudiante_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_tutor ON teia.solicitudes_sesion(tutor_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_estado ON teia.solicitudes_sesion(estado);

-- =============================================
-- TRIGGERS - Sistema de Notificaciones
-- =============================================

-- Trigger para calendario_modulos
DROP TRIGGER IF EXISTS trigger_update_calendario_modulos ON teia.calendario_modulos;
CREATE TRIGGER trigger_update_calendario_modulos
    BEFORE UPDATE ON teia.calendario_modulos
    FOR EACH ROW EXECUTE FUNCTION teia.update_fecha_actualizacion();

-- =============================================
-- COMENTARIOS - Sistema de Notificaciones
-- =============================================

COMMENT ON TABLE teia.tutor_estudiante IS 'Relación de asignación entre tutores y estudiantes.';
COMMENT ON TABLE teia.notificaciones IS 'Sistema de notificaciones para estudiantes, tutores y coordinadores.';
COMMENT ON TABLE teia.calendario_modulos IS 'Calendario con fechas límite por módulo/sección.';
COMMENT ON TABLE teia.configuracion_notificaciones IS 'Configuración paramétrica del sistema de notificaciones.';
COMMENT ON TABLE teia.solicitudes_sesion IS 'Solicitudes de sesión de estudiantes a sus tutores asignados.';

COMMENT ON COLUMN teia.notificaciones.tipo IS 'Tipo: VENCIMIENTO_PROXIMO, SOLICITUD_SESION, RESPUESTA_SOLICITUD, UMBRAL_ALCANZADO';
COMMENT ON COLUMN teia.notificaciones.prioridad IS 'Prioridad: CRITICO (rojo), ALERTA (naranja), INFO (azul), SUCCESS (verde)';
COMMENT ON COLUMN teia.notificaciones.datos_adicionales IS 'JSON con datos contextuales (seccion, estudiante, etc.)';