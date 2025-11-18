#!/bin/bash

# Script para inicializar SonarQube con token de acceso
# Este script se ejecuta después de que SonarQube esté disponible

echo "🔧 Inicializando SonarQube..."

# Esperar a que SonarQube esté completamente disponible
echo "⏳ Esperando a que SonarQube esté disponible..."
until curl -s http://localhost:9001/api/system/status | grep -q '"status":"UP"'; do
    echo "⏳ SonarQube aún no está listo, esperando 10 segundos..."
    sleep 10
done

echo "✅ SonarQube está disponible!"

# Cambiar la contraseña por defecto de admin
echo "🔐 Cambiando contraseña de admin..."
curl -u admin:admin -X POST "http://localhost:9001/api/users/change_password?login=admin&previousPassword=admin&password=@MiguelAngel05" || echo "⚠️ La contraseña ya fue cambiada o no se pudo cambiar"

# Generar token de acceso
echo "🔑 Generando token de acceso..."
TOKEN_RESPONSE=$(curl -u admin:admin123 -X POST "http://localhost:9001/api/user_tokens/generate?name=jenkins-token" 2>/dev/null)

if echo "$TOKEN_RESPONSE" | grep -q "token"; then
    TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    echo "✅ Token generado exitosamente"
    echo "📋 Token de acceso: $TOKEN"
    echo ""
    echo "ℹ️ Usa este token en Jenkins para la integración con SonarQube"
    echo "ℹ️ O actualiza el archivo 00-master-setup.groovy con este token"
else
    echo "⚠️ No se pudo generar el token. Es posible que ya exista."
    echo "ℹ️ Puedes generar un token manualmente desde:"
    echo "   http://localhost:9001/admin/users"
fi

echo ""
echo "✅ Inicialización de SonarQube completada"
echo "🌐 Accede a SonarQube en: http://localhost:9001"
echo "👤 Usuario: admin"
echo "🔑 Contraseña: @MiguelAngel05"
