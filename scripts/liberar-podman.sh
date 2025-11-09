#!/bin/bash

# Script para liberar el bloqueo de Podman
# Ejecuta este script si ves "Error: beginning transaction: database is locked"

echo "🔓 Intentando liberar el bloqueo de Podman..."
echo ""

# 1. Cerrar Podman Desktop
echo "1️⃣ Cerrando Podman Desktop..."
pkill -9 podman-desktop 2>/dev/null && echo "   ✅ Podman Desktop cerrado" || echo "   ℹ️  Podman Desktop no estaba corriendo"

# 2. Detener servicio de Podman
echo "2️⃣ Deteniendo servicio de Podman..."
pkill -9 -f "podman system service" 2>/dev/null && echo "   ✅ Servicio detenido" || echo "   ℹ️  Servicio no estaba corriendo"

# 3. Esperar un momento
sleep 3

# 4. Intentar liberar el lock manualmente
echo "3️⃣ Intentando liberar locks..."
LOCK_FILE="$HOME/.local/share/containers/storage/libpod/bolt_state.db"
if [ -f "$LOCK_FILE" ]; then
    echo "   📍 Archivo de lock encontrado: $LOCK_FILE"
    # No eliminamos el archivo directamente, solo verificamos
fi

# 5. Verificar si Podman responde ahora
echo "4️⃣ Verificando acceso a Podman..."
if podman ps > /dev/null 2>&1; then
    echo "   ✅ Podman está accesible ahora"
    echo ""
    echo "✅ Bloqueo liberado. Puedes ejecutar limpiar-todo.sh desde la raíz del workspace ahora"
else
    echo "   ⚠️  Podman aún no responde"
    echo ""
    echo "💡 Soluciones alternativas:"
    echo "   1. Cierra Podman Desktop completamente desde la aplicación"
    echo "   2. Reinicia el servicio de Podman: systemctl --user restart podman"
    echo "   3. O reinicia tu sesión de usuario"
fi




