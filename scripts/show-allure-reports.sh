#!/bin/bash

# Script único para mostrar reportes de Allure después de la ejecución de la pipeline
# Uso: ./show-allure-reports.sh [build_number]

BUILD_NUMBER=${1:-"1"}
PORT=8084
LOCAL_REPORTS_DIR="/tmp/allure-reports-build-$BUILD_NUMBER"

echo "🎯 Mostrando reportes de Allure del build #$BUILD_NUMBER"
echo ""

# Función para limpiar procesos anteriores
cleanup() {
    echo ""
    echo "🛑 Deteniendo servidor web..."
    pkill -f "python3 -m http.server $PORT" 2>/dev/null || true
    echo "✅ Servidor detenido"
    exit 0
}

# Capturar Ctrl+C para limpiar
trap cleanup SIGINT

# Verificar si los reportes existen localmente, si no, copiarlos desde Jenkins
if [ ! -f "$LOCAL_REPORTS_DIR/e2e-reports/index.html" ]; then
    echo "📊 Copiando reportes desde Jenkins..."
    
    # Crear directorio local
    mkdir -p "$LOCAL_REPORTS_DIR"
    
    # Copiar reportes E2E
    echo "🎯 Copiando reportes E2E..."
    mkdir -p "$LOCAL_REPORTS_DIR/e2e-reports"
    if ! podman cp jenkins:/var/jenkins_home/workspace/jwtmanual-pipeline/automation-tests/target/site/allure-maven-plugin/. "$LOCAL_REPORTS_DIR/e2e-reports/"; then
        echo "❌ Error: No se pudieron copiar los reportes E2E del build #$BUILD_NUMBER"
        echo "💡 Asegúrate de que:"
        echo "   - El build #$BUILD_NUMBER exista"
        echo "   - Los reportes se hayan generado correctamente"
        echo "   - El contenedor 'jenkins' esté ejecutándose"
        exit 1
    fi
    
    # Copiar reportes de cobertura si existen
    if podman exec jenkins test -d /var/jenkins_home/workspace/jwtmanual-pipeline/service/target/site/jacoco/; then
        echo "📈 Copiando reportes de cobertura..."
        mkdir -p "$LOCAL_REPORTS_DIR/coverage-reports"
        podman cp jenkins:/var/jenkins_home/workspace/jwtmanual-pipeline/service/target/site/jacoco/. "$LOCAL_REPORTS_DIR/coverage-reports/" 2>/dev/null || echo "⚠️ Error copiando reportes de cobertura"
    fi
    
    echo "✅ Reportes copiados exitosamente"
    echo ""
fi

# Verificar que Python esté disponible
if ! command -v python3 &> /dev/null; then
    echo "❌ Error: Python3 no está disponible. Instálalo para servir los reportes."
    exit 1
fi

# Detener cualquier servidor anterior en el mismo puerto
pkill -f "python3 -m http.server $PORT" 2>/dev/null || true

# Cambiar al directorio de reportes
cd "$LOCAL_REPORTS_DIR/e2e-reports" || { echo "❌ Error: No se pudo cambiar al directorio de reportes"; exit 1; }

echo "🌐 Iniciando servidor web en puerto $PORT..."
echo "📊 Reportes disponibles en:"
echo "   🎯 E2E Allure: http://localhost:$PORT/"
if [ -d "$LOCAL_REPORTS_DIR/coverage-reports" ]; then
    echo "   📈 Cobertura: http://localhost:$PORT/../coverage-reports/"
fi
echo ""
echo "🚀 Abriendo reporte en el navegador..."
sleep 2
xdg-open "http://localhost:$PORT/" 2>/dev/null &

echo ""
echo "💡 El servidor está ejecutándose. Presiona Ctrl+C para detener."
echo "📋 Para acceder manualmente: http://localhost:$PORT/"
echo ""

# Iniciar servidor web (redirigir stderr para ocultar BrokenPipeError)
python3 -m http.server $PORT 2>&1 | grep -v "BrokenPipeError" | grep -v "Traceback" | grep -v "Exception occurred"