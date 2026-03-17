-- =============================================
-- SCRIPT DML - BITÁCORA DIGITAL TEIA
-- Versión 2.0 - Arquitectura simplificada
-- =============================================

-- =============================================
-- DATOS ACTIVOS - Roles y Menús
-- =============================================

-- Poblar roles
INSERT INTO teia.roles (nombre)
VALUES ('estudiante'),
       ('tutor'),
       ('admin');

-- ============================
-- Menú: Inicio
-- ============================
INSERT INTO teia.menus (id, label, icon, orden, router_link)
VALUES (1, 'Inicio', NULL, 1, NULL);

-- Rol estudiante para el menú Inicio
INSERT INTO teia.menu_roles (menu_id, rol_id)
SELECT 1, id
FROM teia.roles
WHERE nombre = 'estudiante';

-- Submenú: Dashboard
INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden)
VALUES (1, 1, 'Dashboard', 'pi pi-fw pi-clipboard', '/home', 1),
       (10, 1, 'Notificaciones', 'pi pi-bell', '/home/notificaciones', 8),
       (11, 1, 'Sesiones con tutor', 'pi pi-calendar-clock', '/home/solicitudes-sesion', 8);

INSERT INTO teia.menu_item_roles (menu_item_id, rol_id)
SELECT mi.id, r.id
FROM teia.menu_items mi
         JOIN teia.roles r ON r.nombre = 'estudiante'
WHERE mi.menu_id = 1;


-- ============================
-- Menú: Bitácora Digital
-- ============================
INSERT INTO teia.menus (id, label, icon, orden, router_link)
VALUES (2, 'Bitácora Digital', NULL, 2, NULL);

-- Rol estudiante para el menú Bitácora Digital
INSERT INTO teia.menu_roles (menu_id, rol_id)
SELECT 2, id
FROM teia.roles
WHERE nombre = 'estudiante';

-- Submenús de Bitácora Digital
INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden)
VALUES (9, 2, 'Observar, registrar y actuar de manera oportuna', 'pi pi-fw pi-eye', '/home/bitacora/observar-registrar',
        1),
       (2, 2, 'Identificación de tu curso', 'pi pi-fw pi-id-card', '/home/bitacora/caracteriza-asignatura', 2),
       (3, 2, 'Factores Situacionales', 'pi pi-fw pi-arrow-up-right-and-arrow-down-left-from-center',
        '/home/bitacora/factores-situacionales', 3),
       (4, 2, 'Actividades de Aprendizaje', 'pi pi-fw pi-list', '/home/bitacora/actividades-aprendizaje', 4),
       (5, 2, 'Diseño de la evaluación', 'pi pi-fw pi-trophy', '/home/bitacora/como-evaluare', 5),
       (6, 2, 'Secuencia y cronograma', 'pi pi-fw pi-angle-double-right', '/home/bitacora/secuencia-curso', 6),
       (8, 2, 'Calificación', 'pi pi-fw pi-star', '/home/bitacora/calificacion', 7),
       (7, 2, 'Medios educativos', 'pi pi-fw pi-book', '/home/bitacora/bibliografia', 8);

-- Roles estudiante para todos los submenús de Bitácora Digital
INSERT INTO teia.menu_item_roles (menu_item_id, rol_id)
SELECT mi.id, r.id
FROM teia.menu_items mi
         JOIN teia.roles r ON r.nombre = 'estudiante'
WHERE mi.menu_id = 2;


-- ===============================
-- AJUSTE DE SECUENCIAS (setval)
-- ===============================
SELECT setval(pg_get_serial_sequence('teia.menus', 'id'), (SELECT MAX(id) FROM teia.menus));
SELECT setval(pg_get_serial_sequence('teia.menu_items', 'id'), (SELECT MAX(id) FROM teia.menu_items));

-- =============================================
-- DATOS INICIALES - Configuración de Notificaciones
-- =============================================

INSERT INTO teia.configuracion_notificaciones (clave, valor, descripcion, tipo_dato)
VALUES ('DIAS_ANTICIPACION_VENCIMIENTO', '7,3,1', 'Días antes del vencimiento para notificar (separados por coma)',
        'STRING'),
       ('UMBRAL_PROGRESO_NOTIFICACION', '80', 'Porcentaje de progreso para notificar al tutor', 'INTEGER'),
       ('EMAIL_HABILITADO', 'true', 'Habilitar envío de correos electrónicos', 'BOOLEAN'),
       ('WEBSOCKET_HABILITADO', 'true', 'Habilitar notificaciones en tiempo real via WebSocket', 'BOOLEAN'),
       ('HORA_EJECUCION_SCHEDULER', '08:00', 'Hora de ejecución del scheduler de vencimientos (HH:mm)', 'STRING'),
       ('UMBRALES_COMPLETITUD_COORDINADOR', '25,50,75',
        'Porcentajes de completitud de bitácora que generan alerta (configurado por coordinador)', 'STRING'),
       ('DIAS_DEMORA_COORDINADOR', '7,3,1',
        'Días antes del vencimiento para marcar estudiante en demora (configurado por coordinador)',
        'STRING') ON CONFLICT (clave) DO NOTHING;

-- =============================================
-- DATOS - Menú del Tutor (role_id=2)
-- =============================================

-- Menu: Inicio (Tutor)
INSERT INTO teia.menus (id, label, icon, orden)
VALUES (3, 'Inicio', NULL, 1);
INSERT INTO teia.menu_roles (menu_id, rol_id)
VALUES (3, 2);

INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden)
VALUES (20, 3, 'Dashboard', 'pi pi-fw pi-clipboard', '/home', 1),
       (21, 3, 'Notificaciones', 'pi pi-bell', '/home/notificaciones', 2);

INSERT INTO teia.menu_item_roles (menu_item_id, rol_id)
VALUES (20, 2),
       (21, 2);

-- Menu: Gestión Tutor
INSERT INTO teia.menus (id, label, icon, orden)
VALUES (4, 'Gestión de Estudiantes', NULL, 2);
INSERT INTO teia.menu_roles (menu_id, rol_id)
VALUES (4, 2);

INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden)
VALUES (22, 4, 'Revisión de Respuestas', 'pi pi-fw pi-eye', '/home/tutor/revision', 1),
       (23, 4, 'Solicitudes de Sesión', 'pi pi-calendar-clock', '/home/tutor/solicitudes-sesion', 2),
       (24, 4, 'Configuración de Alertas', 'pi pi-fw pi-cog', '/home/tutor/configuracion-alertas', 3);

INSERT INTO teia.menu_item_roles (menu_item_id, rol_id)
VALUES (22, 2),
       (23, 2),
       (24, 2);

SELECT setval(pg_get_serial_sequence('teia.menus', 'id'), (SELECT MAX(id) FROM teia.menus));
SELECT setval(pg_get_serial_sequence('teia.menu_items', 'id'), (SELECT MAX(id) FROM teia.menu_items));

-- =============================================
-- DATOS - Menú del Coordinador (rol admin, id=3)
-- =============================================

-- Menu: Inicio (Coordinador)
INSERT INTO teia.menus (id, label, icon, orden)
VALUES (5, 'Inicio', NULL, 1) ON CONFLICT (id) DO NOTHING;

INSERT INTO teia.menu_roles (menu_id, rol_id)
SELECT 5, id
FROM teia.roles
WHERE nombre = 'admin' ON CONFLICT (menu_id, rol_id) DO NOTHING;

INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden)
VALUES (30, 5, 'Dashboard', 'pi pi-fw pi-chart-pie', '/home/coordinador/dashboard', 1) ON CONFLICT (id) DO NOTHING;

INSERT INTO teia.menu_item_roles (menu_item_id, rol_id)
SELECT 30, id
FROM teia.roles
WHERE nombre = 'admin' ON CONFLICT (menu_item_id, rol_id) DO NOTHING;

-- Menu: Gestión del Sistema
INSERT INTO teia.menus (id, label, icon, orden)
VALUES (6, 'Gestión del Sistema', NULL, 2) ON CONFLICT (id) DO NOTHING;

INSERT INTO teia.menu_roles (menu_id, rol_id)
SELECT 6, id
FROM teia.roles
WHERE nombre = 'admin' ON CONFLICT (menu_id, rol_id) DO NOTHING;

INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden)
VALUES (31, 6, 'Usuarios', 'pi pi-fw pi-users', '/home/coordinador/usuarios', 1),
       (32, 6, 'Parametrizaciones', 'pi pi-fw pi-sliders-h', '/home/coordinador/parametrizaciones', 2),
       (35, 6, 'Asignación de tutores', 'pi pi-fw pi-sitemap', '/home/coordinador/asignaciones',
        3) ON CONFLICT (id) DO NOTHING;

INSERT INTO teia.menu_item_roles (menu_item_id, rol_id)
SELECT mi.id, r.id
FROM teia.menu_items mi
         JOIN teia.roles r ON r.nombre = 'admin'
WHERE mi.id IN (31, 32, 35) ON CONFLICT (menu_item_id, rol_id) DO NOTHING;

-- Menu: Seguimiento
INSERT INTO teia.menus (id, label, icon, orden)
VALUES (7, 'Seguimiento', NULL, 3) ON CONFLICT (id) DO NOTHING;

INSERT INTO teia.menu_roles (menu_id, rol_id)
SELECT 7, id
FROM teia.roles
WHERE nombre = 'admin' ON CONFLICT (menu_id, rol_id) DO NOTHING;

INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden)
VALUES (33, 7, 'Reportes de Progreso', 'pi pi-fw pi-chart-bar', '/home/coordinador/reportes', 1),
       (34, 7, 'Bitácoras Globales', 'pi pi-fw pi-clipboard', '/home/coordinador/bitacoras',
        2) ON CONFLICT (id) DO NOTHING;

INSERT INTO teia.menu_item_roles (menu_item_id, rol_id)
SELECT mi.id, r.id
FROM teia.menu_items mi
         JOIN teia.roles r ON r.nombre = 'admin'
WHERE mi.id IN (33, 34) ON CONFLICT (menu_item_id, rol_id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('teia.menus', 'id'), (SELECT MAX(id) FROM teia.menus));
SELECT setval(pg_get_serial_sequence('teia.menu_items', 'id'), (SELECT MAX(id) FROM teia.menu_items));

-- Claves de configuración para alertas del coordinador
INSERT INTO teia.configuracion_notificaciones (clave, valor, descripcion, tipo_dato)
VALUES ('UMBRALES_COMPLETITUD_COORDINADOR', '25,50,75', 'Porcentajes de avance que generan alerta al coordinador',
        'STRING'),
       ('DIAS_DEMORA_COORDINADOR', '7,15', 'Días antes del vencimiento de un momento para alertar por demora',
        'STRING'),
       ('DIAS_ANTICIPACION_RIESGO_COORDINADOR', '15',
        'Días antes del cierre del curso para evaluar riesgo de no completar', 'INTEGER'),
       ('PORCENTAJE_MINIMO_RIESGO_COORDINADOR', '50',
        'Porcentaje mínimo esperado de avance para no considerar al estudiante en riesgo',
        'INTEGER') ON CONFLICT (clave) DO NOTHING;

-- Datos precargados
INSERT INTO teia.programas (nombre)
VALUES ('Medicina'),
       ('Odontología'),
       ('Psicología'),
       ('Enfermería'),
       ('Ingeniería de Sistemas'),
       ('Ingeniería Industrial'),
       ('Diseño Industrial'),
       ('Administración de Empresas'),
       ('Economía'),
       ('Arte Dramático'),
       ('Música') ON CONFLICT DO NOTHING;

-- Datos precargados: Escritos
INSERT INTO teia.medios (label, value, categoria)
VALUES ('Carpeta o dossier / carpeta colaborativa', 'carpeta_dossier', 'ESCRITOS'),
       ('Control (Examen)', 'control_examen', 'ESCRITOS'),
       ('Cuaderno / cuaderno de notas / cuaderno de campo', 'cuaderno', 'ESCRITOS'),
       ('Cuestionario', 'cuestionario', 'ESCRITOS'),
       ('Diario reflexivo / diario de clase', 'diario', 'ESCRITOS'),
       ('Estudio de casos', 'estudio_casos', 'ESCRITOS'),
       ('Ensayo', 'ensayo', 'ESCRITOS'),
       ('Examen', 'examen', 'ESCRITOS'),
       ('Foro virtual', 'foro_virtual', 'ESCRITOS'),
       ('Memoria', 'memoria', 'ESCRITOS'),
       ('Monografía', 'monografia', 'ESCRITOS'),
       ('Informe', 'informe', 'ESCRITOS'),
       ('Portafolio / portafolio electrónico', 'portafolio', 'ESCRITOS'),
       ('Póster', 'poster', 'ESCRITOS'),
       ('Proyecto', 'proyecto', 'ESCRITOS'),
       ('Pruebas objetivas', 'pruebas_objetivas', 'ESCRITOS'),
       ('Recensión', 'recension', 'ESCRITOS'),
       ('Test diagnóstico', 'test_diagnostico', 'ESCRITOS'),
       ('Trabajo escrito', 'trabajo_escrito', 'ESCRITOS'),
-- Datos precargados: Orales
       ('Comunicación', 'comunicacion_oral', 'ORALES'),
       ('Cuestionario oral', 'cuestionario_oral', 'ORALES'),
       ('Debate / diálogo grupal', 'debate', 'ORALES'),
       ('Exposición', 'exposicion', 'ORALES'),
       ('Discusión grupal', 'discusion_grupal', 'ORALES'),
       ('Mesa redonda', 'mesa_redonda', 'ORALES'),
       ('Ponencia', 'ponencia', 'ORALES'),
       ('Pregunta de clase', 'pregunta_clase', 'ORALES'),
       ('Presentación oral', 'presentacion_oral', 'ORALES'),
-- Datos precargados: Prácticos
       ('Práctica supervisada', 'practica_supervisada', 'PRACTICOS'),
       ('Demostración / actuación / representación', 'demostracion', 'PRACTICOS'),
       ('Role playing', 'role_playing', 'PRACTICOS') ON CONFLICT (value) DO NOTHING;

-- Técnicas: El alumno no interviene
INSERT INTO teia.tecnicas (label, value, grupo)
VALUES ('Análisis documental', 'analisis_documental', 'ALUMNO_NO_INTERVIENE'),
       ('Análisis de producciones', 'analisis_producciones', 'ALUMNO_NO_INTERVIENE'),
       ('Observación directa del alumno', 'observacion_directa', 'ALUMNO_NO_INTERVIENE'),
       ('Observación del grupo', 'observacion_grupo', 'ALUMNO_NO_INTERVIENE'),
       ('Observación sistemática', 'observacion_sistematica', 'ALUMNO_NO_INTERVIENE'),
       ('Análisis de grabación de audio o video', 'analisis_audio_video',
        'ALUMNO_NO_INTERVIENE') ON CONFLICT (value) DO NOTHING;

-- Técnicas: El alumno participa
INSERT INTO teia.tecnicas (label, value, grupo)
VALUES ('Autoevaluación (autorreflexión y/o análisis documental)', 'autoevaluacion', 'ALUMNO_PARTICIPA'),
       ('Evaluación entre pares (análisis documental y/o observación)', 'coevaluacion', 'ALUMNO_PARTICIPA'),
       ('Evaluación compartida o colaborativa (entrevista individual o grupal)', 'evaluacion_colaborativa',
        'ALUMNO_PARTICIPA') ON CONFLICT (value) DO NOTHING;

-- Instrumentos
INSERT INTO teia.instrumentos (label, value)
VALUES ('Diario del profesor', 'diario_profesor'),
       ('Escala de comprobación', 'escala_comprobacion'),
       ('Escala de diferencial semántico', 'escala_diferencial'),
       ('Escala verbal o numérica', 'escala_verbal_numerica'),
       ('Escala descriptiva o rúbrica', 'escala_rubrica'),
       ('Escala de estimación', 'escala_estimacion'),
       ('Ficha de observación', 'ficha_observacion'),
       ('Lista de control', 'lista_control'),
       ('Matrices de decisión', 'matrices_decision'),
       ('Fichas de seguimiento individual o grupal', 'fichas_seguimiento'),
       ('Fichas de autoevaluación', 'fichas_autoevaluacion'),
       ('Fichas de evaluación entre iguales', 'fichas_entre_iguales'),
       ('Informe de expertos', 'informe_expertos'),
       ('Informe de autoevaluación', 'informe_autoevaluacion') ON CONFLICT (value) DO NOTHING;

INSERT INTO teia.dimensiones (nombre)
VALUES ('Compromiso o valoración'),
       ('Dimensiones humanas del aprendizaje'),
       ('Conocimiento Fundamental'),
       ('Aplicación del aprendizaje'),
       ('Integración'),
       ('Aprender a aprender') ON CONFLICT (nombre) DO NOTHING;

INSERT INTO teia.metodologias (label, value)
VALUES ('Aprendizaje basado en proyectos', 'proyectos'),
       ('Aprendizaje basado en juegos', 'juegos'),
       ('Aprendizaje invertido', 'invertido'),
       ('Aprendizaje basado en evidencia', 'evidencia'),
       ('Diálogo reflexivo', 'dialogo'),
       ('Aprendizaje cooperativo', 'cooperativo'),
       ('Aprendizaje basado en problemas', 'problemas'),
       ('Investigación - Acción', 'investigacion'),
       ('Aprendizaje a través del servicio', 'servicio'),
       ('Aprendizaje adaptativo', 'adaptativo') ON CONFLICT (value) DO NOTHING;