#!/bin/bash

# Script para limpiar completamente y reconstruir todos los servicios
# Este script resuelve el problema de que docker-compose up nunca termina

set -e

COMPOSE_FILE="docker-compose.unified.yml"

echo "🧹 Limpiando contenedores, imágenes y volúmenes..."
echo ""

# Detener todos los contenedores
echo "1️⃣ Deteniendo contenedores..."
podman compose -f $COMPOSE_FILE down -v 2>/dev/null || echo "   (algunos contenedores ya estaban detenidos)"

# Eliminar contenedores huérfanos
echo "2️⃣ Eliminando contenedores huérfanos..."
podman container prune -f 2>/dev/null || echo "   (no hay contenedores para eliminar)"

# Eliminar imágenes del proyecto
echo "3️⃣ Eliminando imágenes del proyecto..."
podman images --format "{{.Repository}}:{{.Tag}}" | grep -E "(jwtmanual|domain|orchestrator|notifications|health-check|automation|fluentd)" | xargs -r podman rmi -f 2>/dev/null || echo "   (algunas imágenes ya fueron eliminadas)"

# Limpiar volúmenes no utilizados
echo "4️⃣ Limpiando volúmenes no utilizados..."
podman volume prune -f 2>/dev/null || echo "   (no hay volúmenes para limpiar)"

echo ""
echo "✅ Limpieza completada"
echo ""

# Reconstruir todo
echo "🔨 Reconstruyendo imágenes y levantando servicios..."
echo ""

# Construir sin levantar para ver errores de build
echo "📦 Construyendo imágenes..."
podman compose -f $COMPOSE_FILE build --no-cache 2>&1 | tee /tmp/build.log

# Verificar errores en el build
if grep -i "error\|failed\|fatal" /tmp/build.log > /dev/null 2>&1; then
    echo ""
    echo "⚠️  Se encontraron errores durante la construcción:"
    grep -i "error\|failed\|fatal" /tmp/build.log | head -20
    echo ""
    echo "📋 Revisa el log completo en /tmp/build.log"
    exit 1
fi

echo ""
echo "✅ Construcción completada sin errores"
echo ""

# Levantar en modo detached para que termine inmediatamente
echo "🚀 Levantando servicios en modo detached..."
podman compose -f $COMPOSE_FILE up -d 2>&1 | tee /tmp/startup.log

# Verificar errores en el startup
if grep -i "error\|failed\|fatal" /tmp/startup.log > /dev/null 2>&1; then
    echo ""
    echo "⚠️  Se encontraron errores durante el inicio:"
    grep -i "error\|failed\|fatal" /tmp/startup.log | head -20
    echo ""
    echo "📋 Revisa el log completo en /tmp/startup.log"
fi

echo ""
echo "✅ Servicios levantados"
echo ""

# Esperar un momento y mostrar estado
sleep 5

echo "📊 Estado de los servicios:"
podman compose -f $COMPOSE_FILE ps

echo ""
echo "📋 Para ver logs:"
echo "   podman compose -f $COMPOSE_FILE logs -f"
echo ""
echo "📋 Para ver logs de un servicio específico:"
echo "   podman compose -f $COMPOSE_FILE logs -f domain-service"
echo ""
echo "🛑 Para detener los servicios:"
echo "   podman compose -f $COMPOSE_FILE down"




