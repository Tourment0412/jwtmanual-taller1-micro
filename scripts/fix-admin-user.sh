#!/bin/bash

# Script para crear/actualizar el usuario admin en la base de datos
# ⚠️ NOTA: El microservicio NO usa BCrypt, las contraseñas están en texto plano

set -e

echo "═══════════════════════════════════════════════"
echo "🔧 SOLUCIONANDO USUARIO ADMIN PARA TESTS E2E"
echo "═══════════════════════════════════════════════"
echo ""

# Configuración de la base de datos
CONTAINER_NAME="postgres-domain"
DB_HOST="localhost"
DB_PORT="5433"
DB_NAME="mydb"
DB_USER="user"
DB_PASSWORD="pass"
DOCKER_CMD="podman"  # Cambiar a 'docker' si usas Docker

# Detectar automáticamente si usar docker o podman
if command -v podman &> /dev/null && podman ps &> /dev/null; then
    DOCKER_CMD="podman"
elif command -v docker &> /dev/null && docker ps &> /dev/null; then
    DOCKER_CMD="docker"
else
    echo "❌ Error: Ni Docker ni Podman están disponibles o ejecutándose"
    exit 1
fi
echo "🐳 Usando: $DOCKER_CMD"
echo ""

# Verificar que PostgreSQL esté ejecutándose
if ! $DOCKER_CMD ps | grep -q "$CONTAINER_NAME"; then
    echo "❌ Error: El contenedor de PostgreSQL ($CONTAINER_NAME) no está ejecutándose"
    echo "💡 Inicia los servicios primero con:"
    echo "   cd /ruta/a/jwtmanual-taller1-micro"
    echo "   podman-compose -f docker-compose.unified.yml up -d"
    exit 1
fi

echo "✅ PostgreSQL está ejecutándose"
echo ""

# ⚠️ IMPORTANTE: El microservicio NO usa BCrypt
# Ver: UsuarioServiceImp.java línea 256
# Las contraseñas se almacenan en TEXTO PLANO
PASSWORD_PLAIN='admin123'

echo "⚠️ NOTA IMPORTANTE:"
echo "   El microservicio NO encripta contraseñas (TODO en UsuarioServiceImp.java:256)"
echo "   Las contraseñas se almacenan en TEXTO PLANO"
echo "   Esto es un problema de seguridad para producción"
echo ""

echo "📊 Verificando usuario admin actual..."
ADMIN_EXISTS=$($DOCKER_CMD exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" -t -c \
    "SELECT COUNT(*) FROM usuarios WHERE usuario = 'admin';" 2>/dev/null | tr -d ' ')

if [ "$ADMIN_EXISTS" = "0" ]; then
    echo "ℹ️ El usuario admin NO existe, se creará..."
    ACTION="Insertando"
else
    echo "ℹ️ El usuario admin ya existe, se actualizará..."
    ACTION="Actualizando"
fi
echo ""

echo "🔧 $ACTION usuario admin en la base de datos..."
$DOCKER_CMD exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" <<EOF
INSERT INTO usuarios (usuario, clave, codigo_recuperacion, fecha_codigo, correo, numero_telefono, rol)
VALUES (
    'admin',
    '$PASSWORD_PLAIN',
    NULL,
    NULL,
    'admin@example.com',
    '+1234567890',
    0
)
ON CONFLICT (usuario) DO UPDATE 
SET clave = '$PASSWORD_PLAIN',
    rol = 0,
    correo = 'admin@example.com',
    numero_telefono = '+1234567890';
EOF

if [ $? -eq 0 ]; then
    echo "✅ Usuario admin creado/actualizado correctamente"
else
    echo "❌ Error al crear/actualizar usuario admin"
    exit 1
fi
echo ""

echo "📋 Verificando usuario admin..."
$DOCKER_CMD exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" <<EOF
SELECT 
    usuario AS "Usuario",
    clave AS "Contraseña",
    correo AS "Correo",
    CASE 
        WHEN rol = 0 THEN 'ADMIN'
        WHEN rol = 1 THEN 'USER'
        ELSE 'UNKNOWN'
    END AS "Rol"
FROM usuarios 
WHERE usuario = 'admin';
EOF
echo ""

echo "═══════════════════════════════════════════════"
echo "✅ USUARIO ADMIN CONFIGURADO CORRECTAMENTE"
echo "═══════════════════════════════════════════════"
echo ""
echo "📝 Credenciales de admin:"
echo "   Usuario:    admin"
echo "   Contraseña: admin123"
echo "   Rol:        ADMIN (0)"
echo ""
echo "🧪 Para probar que funciona:"
echo ""
echo "   # Opción 1: Probar login con curl"
echo "   curl -X POST http://localhost:8082/api/sesiones \\"
echo "     -H \"Content-Type: application/json\" \\"
echo "     -d '{\"usuario\":\"admin\",\"clave\":\"admin123\"}'"
echo ""
echo "   # Opción 2: Ejecutar tests E2E"
echo "   cd automation-tests"
echo "   mvn clean test"
echo ""
echo "   # Opción 3: Re-ejecutar pipeline en Jenkins"
echo "   http://localhost:8083/job/jwtmanual-pipeline/"
echo ""
echo "🎯 Ahora los 4 tests E2E que fallaban deberían pasar ✅"
echo "═══════════════════════════════════════════════"
