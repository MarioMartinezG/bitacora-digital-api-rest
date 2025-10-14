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
VALUES (1, 1, 'Dashboard', 'pi pi-fw pi-clipboard', '/home', 1);

INSERT INTO teia.menu_item_roles (menu_item_id, rol_id)
SELECT 1, id FROM teia.roles WHERE nombre = 'estudiante';


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
  (2, 2, 'Caracteriza tu Asignatura', 'pi pi-fw pi-id-card', '/home/bitacora/caracteriza-asignatura', 1),
  (3, 2, 'Factores Situacionales', 'pi pi-fw pi-arrow-up-right-and-arrow-down-left-from-center', '/home/bitacora/factores-situacionales', 2),
  (4, 2, 'Ambientes Sanos y seguros para el aprendizaje', 'pi pi-fw pi-wrench', '/home/bitacora/ajustes-razonables', 3),
  (5, 2, 'RAP y RAC', 'pi pi-fw pi-table', '/home/bitacora/rap-rac', 4),
  (6, 2, 'Actividades de Aprendizaje', 'pi pi-fw pi-list', '/home/bitacora/actividades-aprendizaje', 5),
  (7, 2, 'Cómo Evaluaré', 'pi pi-fw pi-trophy', '/home/bitacora/como-evaluare', 6),
  (8, 2, 'Secuencia del Curso', 'pi pi-fw pi-angle-double-right', '/home/bitacora/secuencia-curso', 7),
  (9, 2, 'Bibliografía y medios educativos', 'pi pi-fw pi-book', '/home/bitacora/bibliografia', 8);

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


-- Creación de módulo 2 - Factores Situacionales
INSERT INTO teia.modulos (id, nombre, descripcion, orden, fecha_inicio, fecha_fin)
VALUES (2, 'Módulo 2: Factores situacionales', 'Contexto específico y características de los estudiantes', 2, '2025-05-12', '2025-05-16');

-- Insertar secciones para el Módulo 2 (Factores Situacionales)
INSERT INTO teia.secciones (modulo_id, nombre, tipo_seccion, orden, configuracion) VALUES
(2, 'Contexto Específico', 'formulario', 1, '{"titulo": "a. Contexto Específico de la situación de Enseñanza/Aprendizaje"}'),
(2, 'Características de los estudiantes', 'formulario', 2, '{"titulo": "b. Características de los estudiantes"}'),
(2, 'Tus características como docente', 'formulario', 3, '{"titulo": "c. Tus características como docente"}');

-- Insertar campos para la sección 1 (Contexto Específico)
INSERT INTO teia.campos_seccion (seccion_id, label, tipo_campo, opciones, es_requerido, orden, configuracion) VALUES
-- Sección 1: Contexto Específico
(1, '¿El curso hace parte de un programa de pregrado, o posgrado?', 'seleccion', '["Pregrado", "Posgrado"]', true, 1, '{"codigo": "pregunta1"}'),
(1, '¿Tu curso es de inicio, mitad o final de programa?', 'seleccion', '["Inicio", "Mitad", "Final"]', true, 2, '{"codigo": "pregunta2"}'),
(1, '¿Cuántos estudiantes hay en tu aula?', 'numero', '[]', true, 3, '{"codigo": "pregunta3", "min_value": 1}'),
(1, '¿El curso es presencial, virtual, híbrido?', 'seleccion', '["Presencial", "Virtual", "Híbrido"]', true, 4, '{"codigo": "pregunta4"}'),
(1, '¿Este tema es principalmente teórico, práctico o una combinación de ambos?', 'seleccion', '["Teórico", "Práctico", "Una combinación de ambos"]', true, 5, '{"codigo": "pregunta5"}'),
(1, '¿Tu curso es de fundamentación, disciplinar, de profundización, electivo?', 'seleccion', '["Fundamentación", "Disciplinar", "Profundización", "Electivo"]', true, 6, '{"codigo": "pregunta6"}'),
(1, '¿Tu curso requiere actualización permanente, o es una temática estable que cambia poco?', 'seleccion', '["Requiere actualización permanente", "Es una temática estable"]', true, 7, '{"codigo": "pregunta7"}'),
(1, '¿Tu curso tiene prerrequisitos o correquisitos?', 'seleccion', '["Prerrequisitos", "Correquisitos", "Ninguno"]', true, 8, '{"codigo": "pregunta8"}'),
(1, '¿Tu curso es prerrequisito de otro?', 'seleccion', '["Sí", "No"]', true, 9, '{"codigo": "pregunta9"}'),
(1, 'Detalle de prerrequisitos', 'textarea', '[]', false, 10, '{"codigo": "detallePregunta9", "depende_de": "pregunta9", "condicion": "Sí"}');

-- Sección 2: Características de los estudiantes
INSERT INTO teia.campos_seccion (seccion_id, label, tipo_campo, opciones, es_requerido, orden, configuracion) VALUES
(2, '¿Cuáles son las características biopsicosociales, culturales, académicas y económicas de tus estudiantes?', 'textarea', '[]', true, 1, '{"codigo": "pregunta10"}'),
(2, '¿Qué conocimiento previo, experiencias y predisposiciones iniciales suelen tener los estudiantes sobre el tema?', 'textarea', '[]', true, 2, '{"codigo": "pregunta11"}'),
(2, '¿Cuáles son los estilos preferidos de aprendizaje de tus estudiantes?', 'textarea', '[]', true, 3, '{"codigo": "pregunta12"}'),
(2, '¿Conoces las características de diversidad de los estudiantes que integrarán tu curso?', 'textarea', '[]', true, 4, '{"codigo": "pregunta13"}');

-- Sección 3: Tus características como docente
INSERT INTO teia.campos_seccion (seccion_id, label, tipo_campo, opciones, es_requerido, orden, configuracion) VALUES
(3, '¿Qué experiencia docente tienes en la enseñanza de este curso o de temas relacionados?', 'textarea', '[]', true, 1, '{"codigo": "pregunta14"}'),
(3, '¿Qué te motiva a enseñar este tema en particular?', 'textarea', '[]', true, 2, '{"codigo": "pregunta15"}'),
(3, '¿Qué resultados has obtenido en la evaluación de este curso anteriormente?', 'textarea', '[]', true, 3, '{"codigo": "pregunta16"}'),
(3, '¿Cómo describes tu estilo de enseñanza?', 'textarea', '[]', true, 4, '{"codigo": "pregunta17"}');