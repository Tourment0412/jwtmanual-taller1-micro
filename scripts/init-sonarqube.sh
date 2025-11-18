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

# Esperar un momento para que el cambio de contraseña se aplique
sleep 2

# Generar token de acceso (usando la contraseña actualizada)
echo "🔑 Generando token de acceso..."
TOKEN_RESPONSE=$(curl -s -u "admin:@MiguelAngel05" -X POST "http://localhost:9001/api/user_tokens/generate" -d "name=jenkins-token" -d "login=admin" 2>/dev/null)

if echo "$TOKEN_RESPONSE" | grep -q '"token"'; then
    TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    echo "✅ Token generado exitosamente"
    echo "📋 Token de acceso: $TOKEN"
    echo ""
    
    # Guardar token en archivo
    TOKEN_FILE="/tmp/sonarqube-token.txt"
    echo "$TOKEN" > "$TOKEN_FILE"
    echo "💾 Token guardado en: $TOKEN_FILE"
    echo ""
    echo "ℹ️ Usa este token en Jenkins para la integración con SonarQube"
    echo "ℹ️ O actualiza el archivo 00-master-setup.groovy con este token"
else
    echo "⚠️ No se pudo generar el token."
    echo ""
    echo "Respuesta de SonarQube:"
    echo "$TOKEN_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$TOKEN_RESPONSE"
    echo ""
    echo "ℹ️ Posibles causas:"
    echo "   - El token 'jenkins-token' ya existe (revócalo primero)"
    echo "   - Problema de autenticación"
    echo "   - SonarQube no está completamente inicializado"
    echo ""
    echo "💡 Puedes generar un token manualmente desde:"
    echo "   http://localhost:9001/account/security"
fi

echo ""
echo "✅ Inicialización de SonarQube completada"
echo "🌐 Accede a SonarQube en: http://localhost:9001"
echo "👤 Usuario: admin"
echo "🔑 Contraseña: @MiguelAngel05"
