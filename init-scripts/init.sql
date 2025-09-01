-- Crear tabla usuarios si no existe
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    usuario VARCHAR(100) NOT NULL,
    clave VARCHAR(200) NOT NULL,
    codigo_recuperacion INTEGER,
    fecha_codigo TIMESTAMP,
    correo VARCHAR(150) UNIQUE NOT NULL,
    rol INTEGER NOT NULL
);

-- Insertar usuario administrador (rol 0)
INSERT INTO usuarios (usuario, clave, codigo_recuperacion, fecha_codigo, correo, rol)
VALUES ('admin', 'admin123', NULL, NULL, 'admin@example.com', 0)
ON CONFLICT (correo) DO NOTHING;

-- Insertar usuarios normales (rol 1)
INSERT INTO usuarios (usuario, clave, codigo_recuperacion, fecha_codigo, correo, rol) VALUES
('juan',   'juan123',   NULL, NULL, 'juan@example.com',   1),
('maria',  'maria123',  NULL, NULL, 'maria@example.com',  1),
('pedro',  'pedro123',  NULL, NULL, 'pedro@example.com',  1),
('laura',  'laura123',  NULL, NULL, 'laura@example.com',  1),
('carlos', 'carlos123', NULL, NULL, 'carlos@example.com', 1)
ON CONFLICT (correo) DO NOTHING;
