# 🚀 Guía Rápida de Ejecución - Setup CI/CD Completo

Esta guía te permitirá levantar todo el ambiente de desarrollo, CI/CD y testing en minutos.

---

## 📋 Índice

1. [Pre-requisitos](#pre-requisitos)
2. [Levantar el Ambiente Completo](#levantar-el-ambiente-completo)
3. [Configuración Inicial de SonarQube](#configuración-inicial-de-sonarqube)
4. [Ejecutar Pipeline de Jenkins](#ejecutar-pipeline-de-jenkins)
5. [Ver Reportes](#ver-reportes)
6. [Comandos Útiles](#comandos-útiles)
7. [Solución de Problemas Comunes](#solución-de-problemas-comunes)

---

## 🔧 Pre-requisitos

Antes de comenzar, asegúrate de tener instalado:

- **Docker** o **Podman** (v20+)
- **Docker Compose** o **Podman Compose**
- **Git**
- **Java 21** (para desarrollo local)
- **Maven 3.9+** (para desarrollo local)

```bash
# Verificar versiones
docker --version
docker compose version
git --version
java --version
mvn --version
```

---

## 🚀 Levantar el Ambiente Completo

### Paso 1: Clonar los Repositorios

```bash
# Navegar al directorio de trabajo
cd ~/Documentos/GitHub

# Clonar microservicio principal
git clone https://github.com/Tourment0412/jwtmanual-taller1-micro.git
cd jwtmanual-taller1-micro

# Clonar tests de automatización (en directorio paralelo)
cd ~/Documentos/GitHub
git clone https://github.com/MiguelA05/automation-tests.git

# Clonar orquestador (si es necesario)
cd ~/Documentos/GitHub
git clone <URL-del-orquestador> orquestador-solicitudes-micro

# Clonar servicio de notificaciones (si es necesario)
cd ~/Documentos/GitHub
git clone <URL-de-notificaciones> notifications-service-micro
```

### Paso 2: Verificar el Archivo `.env`

El archivo `.env` debe existir en `jwtmanual-taller1-micro/`. Si no existe, créalo:

```bash
cd ~/Documentos/GitHub/jwtmanual-taller1-micro

cat > .env << 'EOF'
# RabbitMQ (usar el exchange del orquestador y DLX existente)
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_VHOST=foro
RABBITMQ_USERNAME=orchestrator_user
RABBITMQ_PASSWORD=orch_pass

# Publicacion y consumo (alineado a definitions.json)
AMQP_EXCHANGE=orquestador.events
AMQP_EXCHANGE_TYPE=topic
AMQP_ROUTING_KEY=notifications.created
AMQP_QUEUE=notifications.queue
AMQP_DLX_NAME=dlx
AMQP_DLX_TYPE=topic

# Desactivar autodeclaracion de topologia (el broker ya la trae)
MESSAGING_DECLARE_INFRA=false
WORKER_DECLARE_INFRA=false

# Retries del worker
WORKER_MAX_RETRIES=3
WORKER_RETRY_DELAY_1=5
WORKER_RETRY_DELAY_2=30
WORKER_RETRY_DELAY_3=120

DEFAULT_CHANNEL=email

# JWT
SECRET_KEY=your-secret-key-here-change-in-production
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30

# Base de datos
DB_URL=postgresql+psycopg2://notifications:notifications@postgres-notifications:5432/notifications

# Email (SMTP/SendGrid)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=your-app-password-here
FROM_EMAIL=your-email@gmail.com
FROM_NAME=Your App Name

# Twilio (SMS/WhatsApp)
TWILIO_ACCOUNT_SID=your-twilio-account-sid-here
TWILIO_AUTH_TOKEN=your-twilio-auth-token-here
TWILIO_FROM_NUMBER=+1234567890
TWILIO_WHATSAPP_FROM=+1234567890
WHATSAPP_WEBHOOK_URL=http://localhost:8080/webhook/whatsapp
EOF
```

### Paso 3: Crear Volúmenes y Ajustar Permisos

```bash
# Crear directorios para volúmenes de SonarQube
mkdir -p ~/sonarqube_data ~/sonarqube_extensions ~/sonarqube_logs

# Ajustar permisos para SonarQube (requiere UID 1000)
sudo chown -R 1000:1000 ~/sonarqube_data
sudo chown -R 1000:1000 ~/sonarqube_extensions
sudo chown -R 1000:1000 ~/sonarqube_logs

# Si usas Podman, también ajusta permisos para Jenkins
mkdir -p ~/jenkins_home ~/jenkins_m2
sudo chown -R 1000:1000 ~/jenkins_home
sudo chown -R 1000:1000 ~/jenkins_m2
```

### Paso 4: Levantar Todos los Servicios

```bash
cd ~/Documentos/GitHub/jwtmanual-taller1-micro

# Levantar todos los contenedores (puede tomar 3-5 minutos)
docker compose -f docker-compose.unified.yml up -d --build

# O si usas podman
podman-compose -f docker-compose.unified.yml up -d --build
```

**Servicios que se levantarán:**
- **PostgreSQL** (dominio): `localhost:5433`
- **PostgreSQL** (notificaciones): `localhost:5434`
- **PostgreSQL** (SonarQube): `localhost:5435`
- **RabbitMQ**: `localhost:5672` (Management UI: `localhost:15672`)
- **Microservicio JWT**: `localhost:8080`
- **Orquestador**: `localhost:3000`
- **Notificaciones API**: `localhost:8001`
- **SonarQube**: `localhost:9001`
- **Jenkins**: `localhost:8083`

### Paso 5: Verificar que los Servicios Están Corriendo

```bash
# Ver el estado de todos los contenedores
docker compose -f docker-compose.unified.yml ps

# Ver logs en tiempo real (útil para debugging)
docker compose -f docker-compose.unified.yml logs -f

# Ver logs de un servicio específico
docker compose -f docker-compose.unified.yml logs -f jenkins
docker compose -f docker-compose.unified.yml logs -f sonarqube
```

---

## ⚙️ Configuración Inicial de SonarQube

### Paso 1: Esperar a que SonarQube Esté Listo

```bash
# Verificar que SonarQube está UP (puede tomar 2-3 minutos)
curl -s http://localhost:9001/api/system/status | grep -q '"status":"UP"' && echo "✅ SonarQube está listo" || echo "⏳ Esperando a SonarQube..."

# O monitorear los logs
docker compose -f docker-compose.unified.yml logs -f sonar | grep "SonarQube is operational"
```

### Paso 2: Ejecutar Script de Inicialización

```bash
cd ~/Documentos/GitHub/jwtmanual-taller1-micro

# Dar permisos de ejecución al script
chmod +x init-sonarqube.sh

# Ejecutar el script (cambia contraseña y genera token)
./init-sonarqube.sh
```

**Salida esperada:**
```
✅ SonarQube está disponible
✅ Contraseña cambiada exitosamente
✅ Token generado: squ_XXXXXXXXXXXXXXXXXXXXXXXX
```

**⚠️ IMPORTANTE:** Copia el token generado, lo necesitarás para Jenkins.

### Paso 3: Configurar Webhook en SonarQube

```bash
cd ~/Documentos/GitHub/jwtmanual-taller1-micro

# Dar permisos de ejecución
chmod +x configure-sonarqube-webhook.sh

# Ejecutar configuración del webhook
./configure-sonarqube-webhook.sh
```

**Salida esperada:**
```
✅ Webhook creado exitosamente
📋 Detalles del webhook:
   Nombre: jenkins-webhook
   URL: http://jenkins:8080/sonarqube-webhook/
```

### Paso 4: Actualizar Token en Jenkins (si es necesario)

Si el token generado es diferente al que está en `jenkins.yaml`:

```bash
cd ~/Documentos/GitHub/cicdjenkins/jenkins

# Editar jenkins.yaml y actualizar el token
nano jenkins.yaml
```

Busca la sección de `credentials` y actualiza el `secret`:

```yaml
credentials:
  system:
    domainCredentials:
      - credentials:
          - string:
              scope: GLOBAL
              id: "sonar-token"
              description: "Token para SonarQube"
              secret: "squ_TU_NUEVO_TOKEN_AQUI"  # ← Actualizar aquí
```

Luego, reconstruye Jenkins:

```bash
cd ~/Documentos/GitHub/jwtmanual-taller1-micro
docker compose -f docker-compose.unified.yml up -d --build jenkins
```

---

## 🔄 Ejecutar Pipeline de Jenkins

### Opción 1: Desde la UI de Jenkins

1. Abre tu navegador: http://localhost:8083
2. El pipeline `jwtmanual-pipeline` ya está creado
3. Click en **"Build Now"**
4. Monitorea la ejecución en tiempo real

### Opción 2: Desde la Terminal

```bash
# Ver logs de Jenkins en tiempo real
docker compose -f docker-compose.unified.yml logs -f jenkins

# Ejecutar tests localmente antes del pipeline (opcional)
cd ~/Documentos/GitHub/jwtmanual-taller1-micro
mvn clean verify
```

---

## 📊 Ver Reportes

### Reportes de Cobertura (JaCoCo)

```bash
# Después de ejecutar mvn verify
cd ~/Documentos/GitHub/jwtmanual-taller1-micro
open target/site/jacoco/index.html  # macOS
xdg-open target/site/jacoco/index.html  # Linux
```

O desde Jenkins:
1. Ir al build específico
2. Click en **"Reporte de Cobertura (service)"**

### Reportes de SonarQube

1. Abrir: http://localhost:9001
2. Login: `admin` / `Admin123456!`
3. Ver proyecto: **"JWT Manual Taller 1 Microservice"**

Métricas clave:
- **Coverage**: >60% (gracias a los 57 tests unitarios)
- **Duplications**: <3%
- **Maintainability Rating**: A
- **Reliability Rating**: A

### Reportes de Tests E2E (Allure)

Desde Jenkins:
1. Ir al build específico
2. Click en **"Reporte Allure (E2E)"**

---

## 🛠️ Comandos Útiles

### Gestión de Contenedores

```bash
cd ~/Documentos/GitHub/jwtmanual-taller1-micro

# Detener todos los servicios
docker compose -f docker-compose.unified.yml down

# Detener y eliminar volúmenes (⚠️ borra datos)
docker compose -f docker-compose.unified.yml down -v

# Reiniciar un servicio específico
docker compose -f docker-compose.unified.yml restart jenkins
docker compose -f docker-compose.unified.yml restart sonarqube

# Ver logs de múltiples servicios
docker compose -f docker-compose.unified.yml logs -f jenkins sonarqube jwtmanual-taller1-micro

# Reconstruir un servicio específico
docker compose -f docker-compose.unified.yml up -d --build jenkins
```

### Limpieza Completa (Reset)

```bash
cd ~/Documentos/GitHub/jwtmanual-taller1-micro

# 1. Detener todos los contenedores
docker compose -f docker-compose.unified.yml down -v

# 2. Eliminar imágenes
docker images | grep jwtmanual | awk '{print $3}' | xargs docker rmi -f
docker images | grep jenkins | awk '{print $3}' | xargs docker rmi -f

# 3. Limpiar volúmenes (opcional)
docker volume prune -f

# 4. Volver a levantar
docker compose -f docker-compose.unified.yml up -d --build
```

### Tests Locales

```bash
cd ~/Documentos/GitHub/jwtmanual-taller1-micro

# Ejecutar solo tests unitarios
mvn test

# Ejecutar tests con cobertura
mvn clean verify

# Ejecutar un test específico
mvn test -Dtest=UsuarioServiceImpTest

# Ver reporte de cobertura
xdg-open target/site/jacoco/index.html
```

### Análisis de SonarQube Local

```bash
cd ~/Documentos/GitHub/jwtmanual-taller1-micro

# Ejecutar análisis local
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=jwtmanual-taller1-micro \
  -Dsonar.host.url=http://localhost:9001 \
  -Dsonar.login=squ_TU_TOKEN_AQUI
```

---

## 🐛 Solución de Problemas Comunes

### Problema 1: SonarQube no inicia - "max virtual memory areas"

**Error:**
```
max virtual memory areas vm.max_map_count [65530] is too low
```

**Solución:**
```bash
# Temporal (hasta reiniciar)
sudo sysctl -w vm.max_map_count=262144

# Permanente
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
sudo sysctl -p
```

### Problema 2: Jenkins no puede conectar con SonarQube

**Síntomas:** Pipeline falla en etapa "SonarQube Analysis"

**Solución:**
```bash
# 1. Verificar que SonarQube está UP
curl http://localhost:9001/api/system/status

# 2. Verificar el token en jenkins.yaml
cd ~/Documentos/GitHub/cicdjenkins/jenkins
grep "sonar-token" -A 3 jenkins.yaml

# 3. Regenerar token si es necesario
cd ~/Documentos/GitHub/jwtmanual-taller1-micro
./init-sonarqube.sh

# 4. Reconstruir Jenkins
docker compose -f docker-compose.unified.yml up -d --build jenkins
```

### Problema 3: Tests E2E fallan - "Connection refused"

**Síntomas:** Tests de `automation-tests` no pueden conectar al microservicio

**Solución:**
```bash
# 1. Verificar que el microservicio está corriendo
curl http://localhost:8080/

# 2. Verificar que la BD está lista
docker compose -f docker-compose.unified.yml logs postgres-domain | grep "ready to accept"

# 3. Verificar configuración de red
docker compose -f docker-compose.unified.yml exec jwtmanual-taller1-micro curl http://jwtmanual-taller1-micro:8080/

# 4. Revisar logs del microservicio
docker compose -f docker-compose.unified.yml logs jwtmanual-taller1-micro
```

### Problema 4: "Permission denied" en volúmenes

**Error:**
```
Error: cannot set up namespace using "/usr/bin/newuidmap": exit status 1
```

**Solución:**
```bash
# Ajustar permisos de volúmenes
sudo chown -R 1000:1000 ~/sonarqube_data
sudo chown -R 1000:1000 ~/jenkins_home

# O usar volúmenes nombrados (preferido)
# Ya configurado en docker-compose.unified.yml
```

### Problema 5: Puerto ya en uso

**Error:**
```
Error starting userland proxy: listen tcp 0.0.0.0:8080: bind: address already in use
```

**Solución:**
```bash
# Ver qué proceso usa el puerto
sudo lsof -i :8080
sudo netstat -tulpn | grep :8080

# Matar el proceso (reemplaza PID)
sudo kill -9 <PID>

# O cambiar el puerto en docker-compose.unified.yml
# Buscar el servicio y cambiar "8080:8080" a "8081:8080"
```

### Problema 6: Quality Gate se queda en "PENDING"

**Síntomas:** Pipeline se queda esperando Quality Gate indefinidamente

**Solución:**
```bash
# 1. Verificar que el webhook está configurado
curl -u admin:Admin123456! http://localhost:9001/api/webhooks/list

# 2. Reconfigurar webhook si no existe
cd ~/Documentos/GitHub/jwtmanual-taller1-micro
./configure-sonarqube-webhook.sh

# 3. Verificar conectividad Jenkins ← SonarQube
docker compose -f docker-compose.unified.yml exec sonar curl -X POST http://jenkins:8080/sonarqube-webhook/
```

### Problema 7: Maven "Cannot find symbol" en tests

**Error:**
```
cannot find symbol: method setClave(String)
```

**Solución:**
```bash
# 1. Limpiar y recompilar
cd ~/Documentos/GitHub/jwtmanual-taller1-micro
mvn clean compile

# 2. Si persiste, verificar que Lombok esté funcionando
mvn dependency:tree | grep lombok

# 3. Reinstalar dependencias
rm -rf ~/.m2/repository/org/projectlombok
mvn clean install
```

### Problema 8: RabbitMQ - "PLAIN login refused"

**Error:**
```
PLAIN login refused: user 'orchestrator_user' - invalid credentials
```

**Solución:**
```bash
# 1. Verificar que definitions.json está en el lugar correcto
cd ~/Documentos/GitHub/jwtmanual-taller1-micro
ls -la definitions.json

# 2. Verificar que rabbitmq.conf está configurado
cat rabbitmq.conf

# 3. Reiniciar RabbitMQ para aplicar configuración
docker compose -f docker-compose.unified.yml restart rabbitmq

# 4. Verificar usuarios en RabbitMQ
docker compose -f docker-compose.unified.yml exec rabbitmq rabbitmqctl list_users
```

---

## 📚 Documentación Adicional

Para más detalles sobre componentes específicos:

- **Jenkins**: Ver `JENKINS_INTEGRATION.md`
- **SonarQube**: Ver `SONARQUBE_INTEGRATION.md`
- **Tests E2E**: Ver `automation-tests/README.md`

---

## ✅ Checklist de Verificación

Usa este checklist después de levantar el ambiente:

```bash
# ✅ Servicios básicos
[ ] PostgreSQL (dominio) - curl localhost:5433
[ ] PostgreSQL (notificaciones) - curl localhost:5434
[ ] RabbitMQ - curl localhost:15672
[ ] Microservicio JWT - curl localhost:8080/

# ✅ CI/CD
[ ] SonarQube - curl http://localhost:9001/api/system/status
[ ] Jenkins - curl http://localhost:8083/

# ✅ Configuraciones
[ ] Token de SonarQube generado
[ ] Webhook de SonarQube configurado
[ ] Pipeline de Jenkins creado
[ ] Tests unitarios (57) pasan - mvn test

# ✅ Reportes accesibles
[ ] JaCoCo - target/site/jacoco/index.html
[ ] SonarQube Dashboard - http://localhost:9001
[ ] Jenkins Pipeline - http://localhost:8083
```

---

## 🎯 Flujo de Trabajo Recomendado

```bash
# 1. Desarrollo local
cd ~/Documentos/GitHub/jwtmanual-taller1-micro
mvn clean verify  # Ejecutar tests localmente

# 2. Commit y push
git add .
git commit -m "feat: nueva funcionalidad"
git push origin main

# 3. Jenkins automáticamente ejecutará:
#    - Build
#    - Tests unitarios
#    - Análisis SonarQube
#    - Tests E2E
#    - Reportes

# 4. Revisar resultados
# - Jenkins: http://localhost:8083
# - SonarQube: http://localhost:9001
```

---

## 📞 Recursos y Ayuda

- **Logs de Jenkins**: http://localhost:8083/log/all
- **Logs de SonarQube**: `docker compose logs sonarqube`
- **Estado de servicios**: `docker compose ps`
- **Health checks**: Ver `docker-compose.unified.yml`

---

**¡Ambiente listo para desarrollo y CI/CD! 🎉**

