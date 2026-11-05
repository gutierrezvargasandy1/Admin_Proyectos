-- =====================================================================
-- V1: Esquema inicial del Gestor de Proyectos (MariaDB 10.11)
-- Reemplaza el dump original (dump-dbgestor-202406261453.sql):
--   * Charset utf8mb4 (antes latin1: no soportaba todos los acentos/emojis)
--   * Llaves foraneas con ON DELETE CASCADE (antes no existian -> datos huerfanos)
--   * Correo de usuario UNICO y campos obligatorios NOT NULL
--   * Sin DROP TABLE ni datos reales
-- =====================================================================

CREATE TABLE usuario (
    id_usuario          INT           NOT NULL AUTO_INCREMENT,
    nombre              VARCHAR(100)  NOT NULL,
    correo_electronico  VARCHAR(100)  NOT NULL,
    contrasena          VARCHAR(100)  NOT NULL COMMENT 'Hash BCrypt, nunca texto plano',
    rol                 VARCHAR(100)  DEFAULT NULL,
    imagen_usuario      VARCHAR(255)  DEFAULT NULL,
    PRIMARY KEY (id_usuario),
    CONSTRAINT uk_usuario_correo UNIQUE (correo_electronico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE proyectos (
    id_proyecto         INT           NOT NULL AUTO_INCREMENT,
    nombre              VARCHAR(100)  NOT NULL,
    descripcion         VARCHAR(255)  DEFAULT NULL,
    fecha_inicio        DATE          DEFAULT NULL,
    fecha_finalizacion  DATE          DEFAULT NULL,
    estado_proyecto     VARCHAR(100)  DEFAULT NULL,
    fondo               VARCHAR(100)  DEFAULT NULL,
    PRIMARY KEY (id_proyecto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tareas (
    id_tarea            INT           NOT NULL AUTO_INCREMENT,
    nombre              VARCHAR(100)  NOT NULL,
    descripcion         VARCHAR(255)  DEFAULT NULL,
    fecha_vencimiento   DATE          DEFAULT NULL,
    estado              VARCHAR(255)  DEFAULT NULL,
    lista               VARCHAR(255)  DEFAULT NULL,
    id_proyecto         INT           NOT NULL,
    PRIMARY KEY (id_tarea),
    CONSTRAINT fk_tareas_proyecto FOREIGN KEY (id_proyecto)
        REFERENCES proyectos (id_proyecto) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Nota: comentarios.id_usuario guarda el NOMBRE del autor (VARCHAR) por compatibilidad
-- con la API actual, por eso no lleva llave foranea hacia usuario.
CREATE TABLE comentarios (
    id_comentario       INT           NOT NULL AUTO_INCREMENT,
    texto_comentario    VARCHAR(250)  NOT NULL,
    fecha_hora          DATETIME      DEFAULT NULL,
    id_usuario          VARCHAR(100)  DEFAULT NULL,
    id_tarea            INT           NOT NULL,
    PRIMARY KEY (id_comentario),
    CONSTRAINT fk_comentarios_tarea FOREIGN KEY (id_tarea)
        REFERENCES tareas (id_tarea) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE asignaciones (
    id_asignacion       INT           NOT NULL AUTO_INCREMENT,
    id_usuario          INT           NOT NULL,
    id_tarea            INT           NOT NULL,
    fecha_asignacion    DATE          DEFAULT NULL,
    PRIMARY KEY (id_asignacion),
    CONSTRAINT fk_asignaciones_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_asignaciones_tarea FOREIGN KEY (id_tarea)
        REFERENCES tareas (id_tarea) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Generador de ids que usa Hibernate (GenerationType.AUTO) en la API.
-- Arranca en 1000 para no chocar con los ids de los datos semilla.
CREATE TABLE hibernate_sequence (
    next_val            BIGINT        NOT NULL,
    PRIMARY KEY (next_val)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO hibernate_sequence (next_val) VALUES (1000);
