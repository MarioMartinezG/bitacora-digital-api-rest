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
    -- Estado asignado por profesor/tutor (sobrescribe el estado calculado)
    estado_profesor VARCHAR(20)
    CHECK (estado_profesor IS NULL OR estado_profesor IN ('sin_avances', 'en_desarrollo', 'completado')),
    fecha_actualizacion TIMESTAMP DEFAULT NOW(),
    UNIQUE (usuario_id, seccion_codigo)
    );

-- Migración: Agregar columna estado_profesor si no existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'teia'
        AND table_name = 'progreso_secciones'
        AND column_name = 'estado_profesor'
    ) THEN
ALTER TABLE teia.progreso_secciones
    ADD COLUMN estado_profesor VARCHAR(20)
        CHECK (estado_profesor IS NULL OR estado_profesor IN ('sin_avances', 'en_desarrollo', 'completado'));
END IF;
END $$;

-- Migración: columna revisado en progreso_secciones
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'teia'
        AND table_name = 'progreso_secciones'
        AND column_name = 'revisado'
    ) THEN
ALTER TABLE teia.progreso_secciones
    ADD COLUMN revisado BOOLEAN NOT NULL DEFAULT FALSE;
END IF;
END $$;


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

-- =============================================
-- TABLAS - Módulos del Tutor v3.0
-- =============================================

-- Comentarios del tutor por sub-sección
CREATE TABLE IF NOT EXISTS teia.comentarios_subseccion (
                                                           id SERIAL PRIMARY KEY,
                                                           tutor_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    estudiante_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    seccion_codigo VARCHAR(100) NOT NULL,
    subseccion_codigo VARCHAR(100) NOT NULL,
    comentario TEXT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    resuelto BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_resolucion TIMESTAMP
    );

-- Estado asignado por el tutor a nivel de sub-sección
CREATE TABLE IF NOT EXISTS teia.estado_tutor_subseccion (
                                                            id SERIAL PRIMARY KEY,
                                                            tutor_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    estudiante_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    seccion_codigo VARCHAR(100) NOT NULL,
    subseccion_codigo VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('sin_avances', 'en_desarrollo', 'completado')),
    fecha_actualizacion TIMESTAMP DEFAULT NOW(),
    UNIQUE (estudiante_id, seccion_codigo, subseccion_codigo)
    );

-- Momentos: agrupación de módulos con fechas límite
CREATE TABLE IF NOT EXISTS teia.momentos (
                                             id SERIAL PRIMARY KEY,
                                             nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    fecha_limite DATE NOT NULL,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW()
    );

-- Relación momento-secciones
CREATE TABLE IF NOT EXISTS teia.momento_secciones (
                                                      id SERIAL PRIMARY KEY,
                                                      momento_id INT NOT NULL REFERENCES teia.momentos(id) ON DELETE CASCADE,
    seccion_codigo VARCHAR(100) NOT NULL,
    UNIQUE (momento_id, seccion_codigo)
    );

-- Actualizar constraint de notificaciones.tipo para incluir todos los tipos
ALTER TABLE teia.notificaciones DROP CONSTRAINT IF EXISTS notificaciones_tipo_check;
ALTER TABLE teia.notificaciones ADD CONSTRAINT notificaciones_tipo_check
    CHECK (tipo IN ('VENCIMIENTO_PROXIMO', 'SOLICITUD_SESION', 'RESPUESTA_SOLICITUD', 'UMBRAL_ALCANZADO', 'COMENTARIO_TUTOR', 'ESTADO_TUTOR_ACTUALIZADO', 'COMENTARIO_RESUELTO', 'ESTUDIANTE_EN_RIESGO', 'BITACORA_APROBADA'));

-- =============================================
-- ÍNDICES - Módulos del Tutor
-- =============================================

CREATE INDEX IF NOT EXISTS idx_comentarios_subseccion_estudiante ON teia.comentarios_subseccion(estudiante_id, seccion_codigo, subseccion_codigo);
CREATE INDEX IF NOT EXISTS idx_comentarios_subseccion_tutor ON teia.comentarios_subseccion(tutor_id);

CREATE INDEX IF NOT EXISTS idx_estado_tutor_sub_estudiante ON teia.estado_tutor_subseccion(estudiante_id, seccion_codigo);

CREATE INDEX IF NOT EXISTS idx_momento_secciones_momento ON teia.momento_secciones(momento_id);

-- =============================================
-- TRIGGERS - Módulos del Tutor
-- =============================================

-- Trigger para momentos
DROP TRIGGER IF EXISTS trigger_update_momentos ON teia.momentos;
CREATE TRIGGER trigger_update_momentos
    BEFORE UPDATE ON teia.momentos
    FOR EACH ROW EXECUTE FUNCTION teia.update_fecha_actualizacion();

-- Trigger para estado_tutor_subseccion
DROP TRIGGER IF EXISTS trigger_update_estado_tutor_subseccion ON teia.estado_tutor_subseccion;
CREATE TRIGGER trigger_update_estado_tutor_subseccion
    BEFORE UPDATE ON teia.estado_tutor_subseccion
    FOR EACH ROW EXECUTE FUNCTION teia.update_fecha_actualizacion();

-- =============================================
-- COMENTARIOS - Módulos del Tutor
-- =============================================

COMMENT ON TABLE teia.comentarios_subseccion IS 'Historial de comentarios del tutor por sub-sección de bitácora.';
COMMENT ON TABLE teia.estado_tutor_subseccion IS 'Estado asignado por el tutor a nivel de sub-sección.';
COMMENT ON TABLE teia.momentos IS 'Agrupación de módulos de bitácora en momentos con fechas límite.';
COMMENT ON TABLE teia.momento_secciones IS 'Relación entre momentos y secciones de bitácora.';

-- =============================================
-- TABLAS - Módulo Coordinador v4.0
-- =============================================

-- Alertas del sistema generadas por el coordinador
CREATE TABLE IF NOT EXISTS teia.alertas_sistema (
                                                    id SERIAL PRIMARY KEY,
                                                    tipo VARCHAR(50) NOT NULL,
    prioridad VARCHAR(20) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    mensaje TEXT NOT NULL,
    usuario_referencia_id INT REFERENCES teia.usuarios(id) ON DELETE SET NULL,
    datos_adicionales JSONB DEFAULT '{}',
    resuelta BOOLEAN NOT NULL DEFAULT false,
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_resolucion TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_alertas_sistema_resuelta ON teia.alertas_sistema(resuelta);
CREATE INDEX IF NOT EXISTS idx_alertas_sistema_tipo ON teia.alertas_sistema(tipo);

-- =============================================
-- MIGRACIÓN v4.0 - Multi-rol y Coordinador
-- =============================================

-- Agregar columna activo a usuarios si no existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'teia' AND table_name = 'usuarios' AND column_name = 'activo'
    ) THEN
ALTER TABLE teia.usuarios ADD COLUMN activo BOOLEAN NOT NULL DEFAULT true;
END IF;
END $$;

-- Agregar columna ultimo_acceso a usuarios si no existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'teia' AND table_name = 'usuarios' AND column_name = 'ultimo_acceso'
    ) THEN
ALTER TABLE teia.usuarios ADD COLUMN ultimo_acceso TIMESTAMP;
END IF;
END $$;

-- Tabla de relación usuario-roles (multi-rol)
CREATE TABLE IF NOT EXISTS teia.usuario_roles (
                                                  usuario_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    rol_id INT NOT NULL REFERENCES teia.roles(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, rol_id)
    );

-- Tablas para módulo coordinador: asignaturas
CREATE TABLE IF NOT EXISTS teia.asignaturas (
                                                id SERIAL PRIMARY KEY,
                                                nombre VARCHAR(200) NOT NULL,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    descripcion TEXT,
    creditos INT,
    semestre INT,
    activa BOOLEAN NOT NULL DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW()
    );

-- Relación asignatura-estudiantes
CREATE TABLE IF NOT EXISTS teia.asignatura_estudiantes (
                                                           asignatura_id INT NOT NULL REFERENCES teia.asignaturas(id) ON DELETE CASCADE,
    estudiante_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    PRIMARY KEY (asignatura_id, estudiante_id)
    );

-- Relación asignatura-tutores
CREATE TABLE IF NOT EXISTS teia.asignatura_tutores (
                                                       asignatura_id INT NOT NULL REFERENCES teia.asignaturas(id) ON DELETE CASCADE,
    tutor_id INT NOT NULL REFERENCES teia.usuarios(id) ON DELETE CASCADE,
    PRIMARY KEY (asignatura_id, tutor_id)
    );

CREATE INDEX IF NOT EXISTS idx_usuario_roles_usuario ON teia.usuario_roles(usuario_id);
CREATE INDEX IF NOT EXISTS idx_usuario_roles_rol ON teia.usuario_roles(rol_id);
CREATE INDEX IF NOT EXISTS idx_asignaturas_codigo ON teia.asignaturas(codigo);
-- =============================================
-- Migración v5.0 - Primer login y cambio de clave
-- =============================================
ALTER TABLE teia.usuarios
    ADD COLUMN IF NOT EXISTS requiere_cambio_clave BOOLEAN NOT NULL DEFAULT FALSE;

-- Migración v5.1 - Estado graduado
ALTER TABLE teia.usuarios
    ADD COLUMN IF NOT EXISTS graduado BOOLEAN NOT NULL DEFAULT FALSE;

-- =============================================
-- Migración v6.0 - Alertas coordinador y programas académicos
-- =============================================

-- =============================================
-- Programas académicos
-- =============================================
CREATE TABLE IF NOT EXISTS teia.programas (
                                              id            SERIAL PRIMARY KEY,
                                              nombre        VARCHAR(200) NOT NULL,
    activo        BOOLEAN NOT NULL DEFAULT true,
    fecha_creacion      TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW()
    );

-- =============================================
-- Migración v6.1 - Medios de evaluación
-- =============================================
CREATE TABLE IF NOT EXISTS teia.medios (
                                           id                  SERIAL PRIMARY KEY,
                                           label               VARCHAR(300) NOT NULL,
    value               VARCHAR(100) NOT NULL UNIQUE,
    categoria           VARCHAR(20)  NOT NULL CHECK (categoria IN ('ESCRITOS', 'ORALES', 'PRACTICOS')),
    activo              BOOLEAN NOT NULL DEFAULT true,
    fecha_creacion      TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_medios_categoria ON teia.medios(categoria);
CREATE INDEX IF NOT EXISTS idx_medios_activo    ON teia.medios(activo);


-- =============================================
-- Migración v6.2 - Técnicas e Instrumentos de evaluación
-- =============================================

CREATE TABLE IF NOT EXISTS teia.tecnicas (
                                             id BIGSERIAL PRIMARY KEY,
                                             label VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL UNIQUE,
    grupo VARCHAR(50) NOT NULL CHECK (grupo IN ('ALUMNO_NO_INTERVIENE','ALUMNO_PARTICIPA')),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_tecnicas_grupo   ON teia.tecnicas(grupo);
CREATE INDEX IF NOT EXISTS idx_tecnicas_activo  ON teia.tecnicas(activo);


CREATE TABLE IF NOT EXISTS teia.instrumentos (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 label VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_instrumentos_activo ON teia.instrumentos(activo);

-- ============================================================
-- v6.3 - Dimensiones y Metodologías de aprendizaje
-- ============================================================

CREATE TABLE IF NOT EXISTS teia.dimensiones (
                                                id BIGSERIAL PRIMARY KEY,
                                                nombre VARCHAR(255) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS teia.metodologias (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 label VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
    );
