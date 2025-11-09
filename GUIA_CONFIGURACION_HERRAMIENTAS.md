# 🔧 Guía de Configuración de Herramientas de Terceros

Esta guía documenta la configuración de todas las herramientas de terceros utilizadas en el ecosistema de microservicios.

---

## 📋 Índice

1. [PostgreSQL](#postgresql)
2. [RabbitMQ](#rabbitmq)
3. [Loki](#loki)
4. [Grafana](#grafana)
5. [Fluentd](#fluentd)
6. [SonarQube](#sonarqube)
7. [Jenkins](#jenkins)

---

## 🐘 PostgreSQL

### Descripción
Sistema de gestión de bases de datos relacionales utilizado por todos los microservicios para persistencia de datos.

### Configuración en Docker Compose

```yaml
postgres-domain:
  image: postgres:16-alpine
  environment:
    POSTGRES_USER: user
    POSTGRES_PASSWORD: pass
    POSTGRES_DB: mydb
  ports:
    - "5433:5432"
  volumes:
    - postgres-domain-data:/var/lib/postgresql/data
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U user -d mydb"]
    interval: 10s
    timeout: 5s
    retries: 5
```

### Variables de Entorno por Microservicio

#### Domain Service
```properties
spring.datasource.url=jdbc:postgresql://postgres-domain:5432/mydb
spring.datasource.username=user
spring.datasource.password=pass
```

#### Orchestrator Service
```env
DATABASE_URL=postgresql://user:pass@postgres-orchestrator:5432/orchestrator_db
```

#### Notifications Service
```env
POSTGRES_HOST=postgres-notifications
POSTGRES_PORT=5432
POSTGRES_USER=notif_user
POSTGRES_PASSWORD=notif_pass
POSTGRES_DB=notifications_db
```

#### Gestion Perfil Service
```properties
spring.datasource.url=jdbc:postgresql://postgres-gestion-perfil:5432/mydatabase
spring.datasource.username=myuser
spring.datasource.password=mypassword
```

### Conexión Manual

```bash
# Desde el host
psql -h localhost -p 5433 -U user -d mydb

# Desde dentro del contenedor
podman exec -it postgres-domain psql -U user -d mydb
```

### Migraciones

#### Domain Service (Flyway)
```bash
# Las migraciones se ejecutan automáticamente al iniciar el servicio
# Ubicación: src/main/resources/db/migration/
```

#### Orchestrator Service (Prisma)
```bash
cd orquestador-solicitudes-micro
npx prisma migrate dev
npx prisma generate
```

---

## 🐰 RabbitMQ

### Descripción
Message broker utilizado para comunicación asíncrona entre microservicios mediante eventos.

### Configuración en Docker Compose

```yaml
rabbitmq:
  image: rabbitmq:3-management
  environment:
    RABBITMQ_DEFAULT_USER: admin
    RABBITMQ_DEFAULT_PASS: admin_pass
    RABBITMQ_DEFAULT_VHOST: foro
  ports:
    - "5672:5672"    # AMQP
    - "15672:15672"  # Management UI
  volumes:
    - ./definitions.json:/etc/rabbitmq/definitions.json:ro
    - rabbitmq-data:/var/lib/rabbitmq
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "ping"]
    interval: 30s
    timeout: 10s
    retries: 5
```

### Configuración de Topología

El archivo `definitions.json` define:
- **VHosts**: `foro`
- **Exchanges**: 
  - `dominio.events` (topic)
  - `orquestador.events` (topic)
  - `gestorperfil.events` (topic)
- **Queues**:
  - `orquestador.queue`
  - `notifications.queue`
  - `gestorperfil.queue`
- **Bindings**: Routing keys para enrutar eventos

### Usuarios y Permisos

| Usuario | VHost | Configure | Write | Read |
|---------|-------|-----------|-------|------|
| `admin` | foro | .* | .* | .* |
| `domain_user` | foro | ^dominio\.events$ | ^dominio\..* | ^dominio\..* |
| `orchestrator_user` | foro | ^orquestador\.queue$ | ^orquestador\..* | ^orquestador\..* |
| `notifications_user` | foro | ^notifications\.queue$ | ^notifications\..* | ^notifications\..* |
| `gestorperfil_user` | foro | ^gestorperfil\.queue$ | | ^gestorperfil\.queue$\|^perfil\..* |

### Acceso a Management UI

```
URL: http://localhost:15672
Usuario: admin
Contraseña: admin_pass
```

### Configuración en Microservicios

#### Spring Boot (Domain, Gestion Perfil)
```properties
spring.rabbitmq.host=rabbitmq
spring.rabbitmq.port=5672
spring.rabbitmq.username=domain_user
spring.rabbitmq.password=domain_pass
spring.rabbitmq.virtual-host=foro
```

#### Node.js (Orchestrator)
```typescript
const connection = await amqp.connect({
  hostname: 'rabbitmq',
  port: 5672,
  username: 'orchestrator_user',
  password: 'orchestrator_pass',
  vhost: 'foro'
});
```

#### Python (Notifications)
```python
RABBITMQ_HOST = os.getenv('RABBITMQ_HOST', 'rabbitmq')
RABBITMQ_PORT = int(os.getenv('RABBITMQ_PORT', 5672))
RABBITMQ_USER = os.getenv('RABBITMQ_USER', 'notifications_user')
RABBITMQ_PASS = os.getenv('RABBITMQ_PASS', 'notifications_pass')
RABBITMQ_VHOST = os.getenv('RABBITMQ_VHOST', 'foro')
```

### Publicar Evento (Ejemplo)

```java
// Domain Service
EventoDominio evento = EventoDominio.of(
    TipoAccion.USUARIO_REGISTRADO,
    payload
);
eventoPublisher.publicar(evento);
```

### Consumir Evento (Ejemplo)

```java
// Gestion Perfil Service
@RabbitListener(queues = "gestorperfil.queue")
public void handleEvent(Map<String, Object> payload) {
    // Procesar evento
}
```

---

## 📊 Loki

### Descripción
Sistema de agregación de logs utilizado para centralizar los logs de todos los microservicios.

### Configuración en Docker Compose

```yaml
loki:
  image: grafana/loki:2.9.8
  ports:
    - "3100:3100"
  volumes:
    - ./observability/loki/loki-config.yml:/etc/loki/local-config.yaml:ro
  command: -config.file=/etc/loki/local-config.yaml
  healthcheck:
    test: ["CMD", "wget", "--spider", "-q", "http://localhost:3100/ready"]
    interval: 10s
    timeout: 5s
    retries: 5
```

### Configuración de Loki

Archivo: `observability/loki/loki-config.yml`

```yaml
auth_enabled: false
server:
  http_listen_port: 3100
  grpc_listen_port: 9096

ingester:
  lifecycler:
    address: 127.0.0.1
    ring:
      kvstore:
        store: inmemory
      replication_factor: 1
    final_sleep: 0s
  chunk_idle_period: 5m
  chunk_retain_period: 30s

schema_config:
  configs:
    - from: 2020-10-24
      store: boltdb-shipper
      object_store: filesystem
      schema: v11
      index:
        prefix: index_
        period: 24h

storage_config:
  boltdb_shipper:
    active_index_directory: /loki/index
    cache_location: /loki/cache
    shared_store: filesystem
  filesystem:
    directory: /loki/chunks

limits_config:
  enforce_metric_name: false
  reject_old_samples: true
  reject_old_samples_max_age: 168h
```

### Verificación

```bash
# Verificar que Loki está listo
curl http://localhost:3100/ready

# Consultar logs
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={container="gestion-perfil-service"}' \
  --data-urlencode 'start=2025-11-09T00:00:00Z' \
  --data-urlencode 'end=2025-11-09T23:59:59Z'
```

---

## 📈 Grafana

### Descripción
Plataforma de visualización y dashboards para monitoreo de logs y métricas.

### Configuración en Docker Compose

```yaml
grafana:
  image: grafana/grafana:10.4.5
  ports:
    - "3000:3000"
  environment:
    GF_SECURITY_ADMIN_USER: admin
    GF_SECURITY_ADMIN_PASSWORD: admin
    GF_INSTALL_PLUGINS: grafana-piechart-panel
  volumes:
    - grafana-data:/var/lib/grafana
    - ./observability/grafana/provisioning:/etc/grafana/provisioning:ro
    - ./observability/grafana/dashboards:/var/lib/grafana/dashboards:ro
```

### Acceso

```
URL: http://localhost:3000
Usuario: admin
Contraseña: admin
```

### Configuración de Datasource (Loki)

1. Ir a **Configuration > Data Sources**
2. Agregar **Loki**
3. URL: `http://loki:3100`
4. Guardar y probar

### Dashboards

Los dashboards se encuentran en `observability/grafana/dashboards/` y se cargan automáticamente.

---

## 📝 Fluentd

### Descripción
Colector de logs que recopila logs de contenedores y los envía a Loki.

### Configuración en Docker Compose

```yaml
fluentd:
  build:
    context: ./observability/fluentd
  ports:
    - "24224:24224"
    - "24224:24224/udp"
  volumes:
    - ./observability/fluentd/fluent.conf:/fluentd/etc/fluent.conf:ro
    - fluentd-state:/var/log/fluentd
  depends_on:
    loki:
      condition: service_healthy
```

### Configuración de Fluentd

Archivo: `observability/fluentd/fluent.conf`

```xml
<source>
  @type forward
  port 24224
  bind 0.0.0.0
</source>

<match **>
  @type loki
  url http://loki:3100
  <label>
    container ${record["container_name"]}
    service ${record["service"]}
  </label>
  <buffer>
    @type file
    path /var/log/fluentd/buffer
    flush_interval 5s
    retry_type exponential_backoff
    retry_wait 1s
    retry_max_interval 60s
    retry_timeout 60m
  </buffer>
</match>
```

### Configuración de Logging en Docker Compose

```yaml
services:
  gestion-perfil-service:
    logging:
      driver: "fluentd"
      options:
        fluentd-address: localhost:24224
        tag: "gestion-perfil-service"
```

---

## 🔍 SonarQube

### Descripción
Plataforma de análisis de calidad de código y detección de bugs, vulnerabilidades y code smells.

### Configuración en Docker Compose

```yaml
postgres-sonar:
  image: postgres:15
  environment:
    POSTGRES_USER: sonar
    POSTGRES_PASSWORD: sonar
    POSTGRES_DB: sonar
  volumes:
    - postgres-sonar-data:/var/lib/postgresql/data

sonarqube:
  image: sonarqube:latest
  ports:
    - "9001:9000"
  environment:
    SONAR_JDBC_URL: jdbc:postgresql://postgres-sonar:5432/sonar
    SONAR_JDBC_USERNAME: sonar
    SONAR_JDBC_PASSWORD: sonar
  volumes:
    - sonarqube-data:/opt/sonarqube/data
    - sonarqube-extensions:/opt/sonarqube/extensions
    - sonarqube-logs:/opt/sonarqube/logs
  depends_on:
    postgres-sonar:
      condition: service_healthy
```

### Acceso

```
URL: http://localhost:9001
Usuario por defecto: admin
Contraseña por defecto: admin
```

### Análisis de Código

#### Maven (Java)
```bash
# En el proyecto Java
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9001 \
  -Dsonar.login=YOUR_TOKEN
```

#### Node.js
```bash
npm install -g sonarqube-scanner
sonar-scanner \
  -Dsonar.host.url=http://localhost:9001 \
  -Dsonar.login=YOUR_TOKEN
```

#### Python
```bash
pip install sonar-scanner
sonar-scanner \
  -Dsonar.host.url=http://localhost:9001 \
  -Dsonar.login=YOUR_TOKEN
```

---

## 🔄 Jenkins

### Descripción
Servidor de automatización CI/CD para construir, probar y desplegar aplicaciones.

### Configuración en Docker Compose

```yaml
jenkins:
  build:
    context: ./cicdjenkins
  ports:
    - "8083:8080"
    - "50000:50000"
  volumes:
    - jenkins-data:/var/jenkins_home
    - /var/run/podman.sock:/var/run/docker.sock
  environment:
    - JENKINS_OPTS=--httpPort=8080
```

### Acceso Inicial

1. Acceder a `http://localhost:8083`
2. Obtener la contraseña inicial:
   ```bash
   podman exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```
3. Instalar plugins sugeridos
4. Crear usuario administrador

### Configuración de Pipelines

Los pipelines se definen en `Jenkinsfile` en cada microservicio:

```groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Deploy') {
            steps {
                sh 'docker build -t service:latest .'
            }
        }
    }
}
```

---

## 🔐 Variables de Entorno Comunes

### Base de Datos
```bash
POSTGRES_HOST=postgres-domain
POSTGRES_PORT=5432
POSTGRES_USER=user
POSTGRES_PASSWORD=pass
POSTGRES_DB=mydb
```

### RabbitMQ
```bash
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USER=admin
RABBITMQ_PASS=admin_pass
RABBITMQ_VHOST=foro
```

### Observabilidad
```bash
LOKI_URL=http://loki:3100
GRAFANA_URL=http://grafana:3000
```

---

## 📚 Referencias

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [Loki Documentation](https://grafana.com/docs/loki/latest/)
- [Grafana Documentation](https://grafana.com/docs/grafana/latest/)
- [Fluentd Documentation](https://docs.fluentd.org/)
- [SonarQube Documentation](https://docs.sonarqube.org/)
- [Jenkins Documentation](https://www.jenkins.io/doc/)

---

**Última actualización:** 2025-11-09

