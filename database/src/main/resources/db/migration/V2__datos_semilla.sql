-- =====================================================================
-- V2: Datos semilla FICTICIOS para pruebas y demostracion.
-- Sustituyen los datos reales del dump original (nombres, correos y
-- contrasenas de alumnas), que no deben publicarse en un repositorio.
-- Contrasena de los 3 usuarios de prueba: Demo1234 (guardada como hash BCrypt).
-- =====================================================================

INSERT INTO usuario (id_usuario, nombre, correo_electronico, contrasena, rol, imagen_usuario) VALUES
    (1, 'Ana Demo',     'ana.demo@ejemplo.com',     '$2a$10$2CO8cQqS1ruKzC4MkKQN9OBQWu77LjArXCefoB0q.4e7ytiDXQVnu', 'Estudiante', '#70FAB7'),
    (2, 'Luis Demo',    'luis.demo@ejemplo.com',    '$2a$10$5OZwqpKw.Xr/E0LfKFHreOFCD71KsR99a.7my78EBwOp9V0hZTgwK', 'Estudiante', '#54EA01'),
    (3, 'Docente Demo', 'docente.demo@ejemplo.com', '$2a$10$LnHsRYRnf/ZIGCdMgRCzXebjniTVYkAmYutMTSZbPofVATq9lmcPG', 'Docente',    '#805611');

INSERT INTO proyectos (id_proyecto, nombre, descripcion, fecha_inicio, fecha_finalizacion, estado_proyecto, fondo) VALUES
    (1, 'My API Rest',   'API REST para un sistema de informacion con al menos 5 tablas', '2026-08-31', '2026-11-29', 'En proceso', '/Images/animacion1.gif'),
    (2, 'Portal Web',    'Interfaz web del gestor de proyectos',                          '2026-09-15', '2026-12-15', 'En proceso', '/Images/azulReal.png');

INSERT INTO tareas (id_tarea, nombre, descripcion, fecha_vencimiento, estado, lista, id_proyecto) VALUES
    (1, 'Base de datos',  'Estructura de las tablas',     '2026-09-13', 'danger',  'terminado',  1),
    (2, 'Endpoints',      'CRUD de las 5 entidades',      '2026-10-10', 'warning', 'encurso',    1),
    (3, 'Pruebas',        'Pruebas unitarias al 100%',    '2026-11-20', 'success', 'pendientes', 1),
    (4, 'Vistas',         'Pantallas de inicio y login',  '2026-11-01', 'primary', 'encurso',    2);

INSERT INTO comentarios (id_comentario, texto_comentario, fecha_hora, id_usuario, id_tarea) VALUES
    (1, 'Tablas listas para revision', '2026-09-12 13:48:00', 'Ana Demo',     1),
    (2, 'Aprobado',                    '2026-09-13 09:10:00', 'Docente Demo', 1),
    (3, 'Falta el endpoint de login',  '2026-10-05 17:30:00', 'Luis Demo',    2);

INSERT INTO asignaciones (id_asignacion, id_usuario, id_tarea, fecha_asignacion) VALUES
    (1, 1, 1, '2026-09-01'),
    (2, 2, 2, '2026-09-20');
