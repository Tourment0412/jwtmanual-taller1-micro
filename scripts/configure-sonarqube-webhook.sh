#!/bin/bash

# Script para configurar webhook en SonarQube para notificar a Jenkins

SONARQUBE_HOST="http://localhost:9001"
SONARQUBE_USER="admin"
SONARQUBE_PASSWORD="@MiguelAngel05"
JENKINS_HOST="http://jenkins:8080"
WEBHOOK_NAME="jenkins-webhook"
WEBHOOK_URL="${JENKINS_HOST}/sonarqube-webhook/"

echo "🔧 Configurando webhook en SonarQube..."

# Verificar que SonarQube esté disponible
echo "⏳ Verificando que SonarQube esté disponible..."
if ! curl -s -u "${SONARQUBE_USER}:${SONARQUBE_PASSWORD}" "${SONARQUBE_HOST}/api/system/status" | grep -q '"status":"UP"'; then
    echo "❌ SonarQube no está disponible"
    exit 1
fi
echo "✅ SonarQube está disponible"

# Verificar si el webhook ya existe
echo "🔍 Verificando si el webhook ya existe..."
EXISTING_WEBHOOK=$(curl -s -u "${SONARQUBE_USER}:${SONARQUBE_PASSWORD}" \
    "${SONARQUBE_HOST}/api/webhooks/list" | grep -o "\"name\":\"${WEBHOOK_NAME}\"")

if [ -n "$EXISTING_WEBHOOK" ]; then
    echo "⚠️ El webhook '${WEBHOOK_NAME}' ya existe. Eliminándolo..."
    # Obtener el key del webhook existente
    WEBHOOK_KEY=$(curl -s -u "${SONARQUBE_USER}:${SONARQUBE_PASSWORD}" \
        "${SONARQUBE_HOST}/api/webhooks/list" | \
        grep -oP "\"key\":\"[^\"]+\"" | head -1 | cut -d'"' -f4)
    
    if [ -n "$WEBHOOK_KEY" ]; then
        curl -s -X POST -u "${SONARQUBE_USER}:${SONARQUBE_PASSWORD}" \
            "${SONARQUBE_HOST}/api/webhooks/delete?webhook=${WEBHOOK_KEY}"
        echo "✅ Webhook existente eliminado"
    fi
fi

# Crear el webhook
echo "🔗 Creando webhook..."
RESPONSE=$(curl -s -X POST -u "${SONARQUBE_USER}:${SONARQUBE_PASSWORD}" \
    "${SONARQUBE_HOST}/api/webhooks/create" \
    -d "name=${WEBHOOK_NAME}" \
    -d "url=${WEBHOOK_URL}")

if echo "$RESPONSE" | grep -q "\"webhook\""; then
    echo "✅ Webhook creado exitosamente"
    echo "📋 Detalles del webhook:"
    echo "   Nombre: ${WEBHOOK_NAME}"
    echo "   URL: ${WEBHOOK_URL}"
    echo ""
    echo "🎯 Ahora Jenkins recibirá notificaciones cuando SonarQube complete el análisis"
else
    echo "❌ Error creando webhook"
    echo "Respuesta: $RESPONSE"
    exit 1
fi

# Verificar que el webhook se creó correctamente
echo "🔍 Verificando webhook..."
curl -s -u "${SONARQUBE_USER}:${SONARQUBE_PASSWORD}" \
    "${SONARQUBE_HOST}/api/webhooks/list" | grep -q "\"name\":\"${WEBHOOK_NAME}\""

if [ $? -eq 0 ]; then
    echo "✅ Webhook verificado correctamente"
else
    echo "⚠️ No se pudo verificar el webhook"
fi

echo ""
echo "✨ Configuración completada!"

