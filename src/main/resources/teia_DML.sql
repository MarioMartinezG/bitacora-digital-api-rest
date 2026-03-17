-- =============================================
-- SCRIPT DML - BITÁCORA DIGITAL TEIA
-- Versión 2.0 - Arquitectura simplificada
-- =============================================

-- =============================================
-- DATOS ACTIVOS - Roles y Menús
-- =============================================

-- Poblar roles
INSERT INTO teia.roles (nombre) VALUES ('estudiante'), ('tutor'), ('admin');

-- ============================
-- Menú: Inicio
-- ============================
INSERT INTO teia.menus (id, label, icon, orden, router_link)
VALUES (1, 'Inicio', NULL, 1, NULL);

-- Rol estudiante para el menú Inicio
INSERT INTO teia.menu_roles (menu_id, rol_id)
SELECT 1, id FROM teia.roles WHERE nombre = 'estudiante';

-- Submenú: Dashboard
INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden)
VALUES
    (1, 1, 'Dashboard', 'pi pi-fw pi-clipboard', '/home', 1),
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
SELECT 2, id FROM teia.roles WHERE nombre = 'estudiante';

-- Submenús de Bitácora Digital
INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden)
VALUES
  (9, 2, 'Observar, registrar y actuar de manera oportuna', 'pi pi-fw pi-eye', '/home/bitacora/observar-registrar', 1),
  (2, 2, 'Identificación de tu curso', 'pi pi-fw pi-id-card', '/home/bitacora/caracteriza-asignatura', 2),
  (3, 2, 'Factores Situacionales', 'pi pi-fw pi-arrow-up-right-and-arrow-down-left-from-center', '/home/bitacora/factores-situacionales', 3),
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

INSERT INTO teia.configuracion_notificaciones (clave, valor, descripcion, tipo_dato) VALUES
    ('DIAS_ANTICIPACION_VENCIMIENTO', '7,3,1', 'Días antes del vencimiento para notificar (separados por coma)', 'STRING'),
    ('UMBRAL_PROGRESO_NOTIFICACION', '80', 'Porcentaje de progreso para notificar al tutor', 'INTEGER'),
    ('EMAIL_HABILITADO', 'true', 'Habilitar envío de correos electrónicos', 'BOOLEAN'),
    ('WEBSOCKET_HABILITADO', 'true', 'Habilitar notificaciones en tiempo real via WebSocket', 'BOOLEAN'),
    ('HORA_EJECUCION_SCHEDULER', '08:00', 'Hora de ejecución del scheduler de vencimientos (HH:mm)', 'STRING'),
    ('UMBRALES_COMPLETITUD_COORDINADOR', '25,50,75', 'Porcentajes de completitud de bitácora que generan alerta (configurado por coordinador)', 'STRING'),
    ('DIAS_DEMORA_COORDINADOR', '7,3,1', 'Días antes del vencimiento para marcar estudiante en demora (configurado por coordinador)', 'STRING')
ON CONFLICT (clave) DO NOTHING;

-- =============================================
-- DATOS - Menú del Tutor (role_id=2)
-- =============================================

-- Menu: Inicio (Tutor)
INSERT INTO teia.menus (id, label, icon, orden) VALUES (3, 'Inicio', NULL, 1);
INSERT INTO teia.menu_roles (menu_id, rol_id) VALUES (3, 2);

INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden) VALUES
    (20, 3, 'Dashboard', 'pi pi-fw pi-clipboard', '/home', 1),
    (21, 3, 'Notificaciones', 'pi pi-bell', '/home/notificaciones', 2);

INSERT INTO teia.menu_item_roles (menu_item_id, rol_id) VALUES (20, 2), (21, 2);

-- Menu: Gestión Tutor
INSERT INTO teia.menus (id, label, icon, orden) VALUES (4, 'Gestión de Estudiantes', NULL, 2);
INSERT INTO teia.menu_roles (menu_id, rol_id) VALUES (4, 2);

INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden) VALUES
    (22, 4, 'Revisión de Respuestas', 'pi pi-fw pi-eye', '/home/tutor/revision', 1),
    (23, 4, 'Solicitudes de Sesión', 'pi pi-calendar-clock', '/home/tutor/solicitudes-sesion', 2),
    (24, 4, 'Configuración de Alertas', 'pi pi-fw pi-cog', '/home/tutor/configuracion-alertas', 3);

INSERT INTO teia.menu_item_roles (menu_item_id, rol_id) VALUES (22, 2), (23, 2), (24, 2);

SELECT setval(pg_get_serial_sequence('teia.menus', 'id'), (SELECT MAX(id) FROM teia.menus));
SELECT setval(pg_get_serial_sequence('teia.menu_items', 'id'), (SELECT MAX(id) FROM teia.menu_items));

-- =============================================
-- DATOS - Menú del Coordinador (rol admin, id=3)
-- =============================================

-- Menu: Inicio (Coordinador)
INSERT INTO teia.menus (id, label, icon, orden)
VALUES (5, 'Inicio', NULL, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO teia.menu_roles (menu_id, rol_id)
SELECT 5, id FROM teia.roles WHERE nombre = 'admin'
ON CONFLICT (menu_id, rol_id) DO NOTHING;

INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden)
VALUES (30, 5, 'Dashboard', 'pi pi-fw pi-chart-pie', '/home/coordinador/dashboard', 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO teia.menu_item_roles (menu_item_id, rol_id)
SELECT 30, id FROM teia.roles WHERE nombre = 'admin'
ON CONFLICT (menu_item_id, rol_id) DO NOTHING;

-- Menu: Gestión del Sistema
INSERT INTO teia.menus (id, label, icon, orden)
VALUES (6, 'Gestión del Sistema', NULL, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO teia.menu_roles (menu_id, rol_id)
SELECT 6, id FROM teia.roles WHERE nombre = 'admin'
ON CONFLICT (menu_id, rol_id) DO NOTHING;

INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden)
VALUES
    (31, 6, 'Usuarios', 'pi pi-fw pi-users', '/home/coordinador/usuarios', 1),
    (32, 6, 'Parametrizaciones', 'pi pi-fw pi-sliders-h', '/home/coordinador/parametrizaciones', 2),
    (35, 6, 'Asignación de tutores', 'pi pi-fw pi-sitemap', '/home/coordinador/asignaciones', 3)
ON CONFLICT (id) DO NOTHING;

INSERT INTO teia.menu_item_roles (menu_item_id, rol_id)
SELECT mi.id, r.id
FROM teia.menu_items mi
JOIN teia.roles r ON r.nombre = 'admin'
WHERE mi.id IN (31, 32, 35)
ON CONFLICT (menu_item_id, rol_id) DO NOTHING;

-- Menu: Seguimiento
INSERT INTO teia.menus (id, label, icon, orden)
VALUES (7, 'Seguimiento', NULL, 3)
ON CONFLICT (id) DO NOTHING;

INSERT INTO teia.menu_roles (menu_id, rol_id)
SELECT 7, id FROM teia.roles WHERE nombre = 'admin'
ON CONFLICT (menu_id, rol_id) DO NOTHING;

INSERT INTO teia.menu_items (id, menu_id, label, icon, router_link, orden)
VALUES
    (33, 7, 'Reportes de Progreso', 'pi pi-fw pi-chart-bar', '/home/coordinador/reportes', 1),
    (34, 7, 'Bitácoras Globales', 'pi pi-fw pi-clipboard', '/home/coordinador/bitacoras', 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO teia.menu_item_roles (menu_item_id, rol_id)
SELECT mi.id, r.id
FROM teia.menu_items mi
JOIN teia.roles r ON r.nombre = 'admin'
WHERE mi.id IN (33, 34)
ON CONFLICT (menu_item_id, rol_id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('teia.menus', 'id'), (SELECT MAX(id) FROM teia.menus));
SELECT setval(pg_get_serial_sequence('teia.menu_items', 'id'), (SELECT MAX(id) FROM teia.menu_items));
