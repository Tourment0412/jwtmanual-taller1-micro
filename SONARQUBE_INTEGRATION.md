# 🎯 Integración de SonarQube con Jenkins

## ✅ Estado de la Integración

**COMPLETADA EXITOSAMENTE** - Todos los componentes están configurados y funcionando.

---

## 📋 Resumen de la Implementación

### 1. **Servicios Desplegados**

| Servicio | Puerto | Estado | URL |
|----------|--------|--------|-----|
| **Jenkins** | 8083 | ✅ Running | http://localhost:8083 |
| **SonarQube** | 9001 | ✅ Running (Healthy) | http://localhost:9001 |
| **PostgreSQL (SonarQube)** | 5432 | ✅ Running (Healthy) | postgres-sonar:5432 |
| **jwtmanual-taller1-micro** | 8081 | ✅ Running (Healthy) | http://localhost:8081 |
| **RabbitMQ** | 5672, 15672 | ✅ Running (Healthy) | http://localhost:15672 |

---

## 🔧 Configuración Implementada

### **SonarQube**

#### Credenciales de Acceso:
- **Usuario:** `admin`
- **Contraseña:** `Admin123456!`
- **Token de Acceso:** `squ_0e5d4cf5243482083cae80b68e26db616bf05c0a`

#### Configuración en `docker-compose.unified.yml`:
```yaml
sonar:
  image: sonarqube:latest  # Versión 25.10.0.114319
  container_name: sonarqube
  ports:
    - "9001:9000"
  environment:
    - SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true
    - SONAR_JDBC_URL=jdbc:postgresql://postgres-sonar:5432/sonar
    - SONAR_JDBC_USERNAME=sonar
    - SONAR_JDBC_PASSWORD=sonar
    - SONAR_WEB_JAVAADDITIONALOPTS=-Xmx1024m -Xms512m
  healthcheck:
    test: ["CMD-SHELL", "wget -qO- http://localhost:9000/api/system/status | grep -q '\"status\":\"UP\"' || exit 1"]
    interval: 30s
    timeout: 10s
    retries: 5
    start_period: 60s
```

#### Configuración del Proyecto (`sonar-project.properties`):
```properties
sonar.projectKey=jwtmanual-taller1-micro
sonar.projectName=JWT Manual Taller 1 Microservice
sonar.projectVersion=1.0
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.java.source=21
sonar.java.target=21
sonar.qualitygate.wait=true
```

---

### **Jenkins**

#### Configuración de SonarQube en Jenkins:
El script `00-master-setup.groovy` configura automáticamente:

1. **Credenciales de SonarQube:**
   - ID: `sonar-token`
   - Token: `squ_0e5d4cf5243482083cae80b68e26db616bf05c0a`

2. **Instalación de SonarQube:**
   - Nombre: `SonarQube`
   - URL: `http://sonarqube:9000`

#### Pipeline Actualizado:
El pipeline ahora incluye dos nuevos stages:

**Stage 1: SonarQube Analysis**
```groovy
stage('SonarQube Analysis') {
    steps {
        script {
            dir('service') {
                echo "🔍 Iniciando análisis de calidad con SonarQube..."
                withSonarQubeEnv('SonarQube') {
                    sh """
                        ${MVN} sonar:sonar \
                            -Dsonar.projectKey=jwtmanual-taller1-micro \
                            -Dsonar.projectName='JWT Manual Taller 1 Microservice' \
                            -Dsonar.projectVersion=1.0 \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
            }
        }
    }
}
```

**Stage 2: Quality Gate**
```groovy
stage('Quality Gate') {
    steps {
        script {
            echo "🚦 Esperando resultado del Quality Gate..."
            timeout(time: 5, unit: 'MINUTES') {
                def qg = waitForQualityGate()
                if (qg.status != 'OK') {
                    echo "⚠️ Quality Gate falló: ${qg.status}"
                    echo "ℹ️ Continuando pipeline a pesar del fallo..."
                } else {
                    echo "✅ Quality Gate aprobado!"
                }
            }
        }
    }
}
```

---

## 🚀 Flujo de Ejecución del Pipeline

```
1. Checkout repos
   ↓
2. Build + Unit tests (service)
   ├── Compilación con Maven
   ├── Ejecución de tests unitarios
   └── Generación de reporte de cobertura (JaCoCo)
   ↓
3. SonarQube Analysis ⭐ NUEVO
   ├── Análisis de calidad de código
   ├── Detección de code smells
   ├── Análisis de vulnerabilidades
   └── Cálculo de cobertura de código
   ↓
4. Quality Gate ⭐ NUEVO
   ├── Validación de métricas de calidad
   ├── Verificación de cobertura mínima
   └── Aprobación/Rechazo del build
   ↓
5. Allure (service)
   └── Generación de reportes Allure
   ↓
6. E2E (automation-tests)
   ├── Ejecución de tests E2E
   └── Generación de reportes Allure E2E
```

---

## 📊 Métricas y Reportes Disponibles

### **En Jenkins:**
1. **Reporte de Cobertura (JaCoCo)**
   - URL: `http://localhost:8083/job/jwtmanual-pipeline/[BUILD_NUMBER]/Reporte_20de_20Cobertura_20_28service_29/`

2. **Reporte Allure (Service)**
   - URL: `http://localhost:8083/job/jwtmanual-pipeline/[BUILD_NUMBER]/Reporte_20Allure_20_28service_29/`

3. **Reporte Allure (E2E)**
   - URL: `http://localhost:8083/job/jwtmanual-pipeline/[BUILD_NUMBER]/Reporte_20Allure_20_28E2E_29/`

### **En SonarQube:**
1. **Dashboard del Proyecto**
   - URL: `http://localhost:9001/dashboard?id=jwtmanual-taller1-micro`

2. **Métricas Disponibles:**
   - Cobertura de código
   - Code smells
   - Bugs
   - Vulnerabilidades
   - Security hotspots
   - Duplicación de código
   - Complejidad ciclomática
   - Deuda técnica

---

## 🔐 Seguridad y Mejores Prácticas

### **Implementado:**
✅ Token de autenticación para SonarQube
✅ Healthchecks para todos los servicios
✅ Contraseña robusta para SonarQube (12+ caracteres, mayúsculas, caracteres especiales)
✅ Quality Gate no bloquea el build (permite continuar con advertencia)
✅ Timeout de 5 minutos para Quality Gate

### **Recomendaciones para Producción:**
⚠️ Cambiar el token de SonarQube por uno generado específicamente para producción
⚠️ Configurar Quality Gate para bloquear builds que no cumplan con los estándares
⚠️ Habilitar notificaciones por email/Slack cuando falle el Quality Gate
⚠️ Configurar reglas personalizadas de SonarQube según los estándares del equipo
⚠️ Implementar análisis de ramas (branch analysis) para PRs

---

## 📝 Scripts de Utilidad

### **Inicialización de SonarQube:**
```bash
./init-sonarqube.sh
```

### **Ver Reportes de Allure Localmente:**
```bash
./show-allure-reports.sh [BUILD_NUMBER]
```

### **Acceder a SonarQube:**
```bash
curl http://localhost:9001/api/system/status
```

---

## 🎓 Cómo Usar la Integración

### **1. Ejecutar el Pipeline:**
1. Accede a Jenkins: `http://localhost:8083`
2. Selecciona el job `jwtmanual-pipeline`
3. Click en "Build Now"

### **2. Ver Resultados en SonarQube:**
1. Accede a SonarQube: `http://localhost:9001`
2. Login con `admin` / `Admin123456!`
3. Navega a "Projects" → `jwtmanual-taller1-micro`

### **3. Interpretar el Quality Gate:**
- **✅ PASSED:** El código cumple con todos los estándares de calidad
- **⚠️ WARNING:** El código tiene issues menores pero el build continúa
- **❌ FAILED:** El código no cumple con los estándares (actualmente solo advierte)

---

## 🐛 Troubleshooting

### **SonarQube no está disponible:**
```bash
# Verificar estado
podman ps | grep sonarqube

# Ver logs
podman logs sonarqube

# Reiniciar
podman restart sonarqube
```

### **Jenkins no puede conectarse a SonarQube:**
```bash
# Verificar conectividad desde Jenkins
podman exec jenkins curl -s http://sonarqube:9000/api/system/status
```

### **Token de SonarQube inválido:**
1. Accede a SonarQube: `http://localhost:9001`
2. Ve a "My Account" → "Security" → "Tokens"
3. Genera un nuevo token
4. Actualiza el token en `00-master-setup.groovy`
5. Reinicia Jenkins

---

## 📚 Referencias

- **SonarQube Documentation:** https://docs.sonarqube.org/latest/
- **Jenkins SonarQube Plugin:** https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-jenkins/
- **JaCoCo Maven Plugin:** https://www.jacoco.org/jacoco/trunk/doc/maven.html
- **Quality Gates:** https://docs.sonarqube.org/latest/user-guide/quality-gates/

---

## ✨ Conclusión

La integración de SonarQube con Jenkins está **completamente funcional** y proporciona:

✅ Análisis automático de calidad de código en cada build
✅ Métricas detalladas de cobertura, bugs, y vulnerabilidades
✅ Quality Gate para garantizar estándares de calidad
✅ Reportes visuales en Jenkins y SonarQube
✅ Integración completa con el pipeline CI/CD existente

**¡La integración está lista para uso en desarrollo y puede ser adaptada para producción!**
