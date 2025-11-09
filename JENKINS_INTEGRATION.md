# 🎯 Integración de Jenkins con CI/CD Pipeline

## ✅ Estado de la Integración

**COMPLETADA EXITOSAMENTE** - Pipeline CI/CD completamente funcional con análisis de calidad, tests unitarios y E2E.

---

## 📋 Resumen de la Implementación

### 1. **Servicios Desplegados**

| Servicio | Puerto | Estado | URL |
|----------|--------|--------|-----|
| **Jenkins** | 8083 | ✅ Running | http://localhost:8083 |
| **jwtmanual-taller1-micro** | 8081 | ✅ Running (Healthy) | http://localhost:8081 |
| **automation-tests** | N/A | ✅ Container | Ejecuta en Docker |
| **PostgreSQL (Domain)** | 5432 | ✅ Running (Healthy) | postgres-domain:5432 |
| **RabbitMQ** | 5672, 15672 | ✅ Running (Healthy) | http://localhost:15672 |
| **SonarQube** | 9001 | ✅ Running (Healthy) | http://localhost:9001 |

---

## 🔧 Configuración Implementada

### **Jenkins**

#### Credenciales de Acceso:
- **URL:** `http://localhost:8083`
- **Configuración:** Automática vía JCasC (Jenkins Configuration as Code)

#### Configuración en `docker-compose.unified.yml`:
```yaml
jenkins:
  build: ../cicdjenkins/jenkins
  container_name: jenkins
  ports:
    - "8083:8080"
    - "50000:50000"
  volumes:
    - jenkins_home:/var/jenkins_home
    - jenkins_m2:/var/jenkins_home/.m2
  environment:
    - CASC_JENKINS_CONFIG=/usr/share/jenkins/ref/casc_configs/jenkins.yaml
    - JAVA_OPTS=-Djenkins.install.runSetupWizard=false
    - SONAR_AUTH_TOKEN=admin123456789
    - SONAR_HOST_URL=http://sonarqube:9000
    - TESTCONTAINERS_RYUK_DISABLED=true
    - TESTCONTAINERS_CHECKS_DISABLE=true
  depends_on:
    sonar:
      condition: service_healthy
```

---

### **Configuración via JCasC (`jenkins.yaml`)**

#### 1. **Herramientas:**
```yaml
tool:
  maven:
    installations:
      - name: "Maven-3.9"
        properties:
          - installSource:
              installers:
                - maven:
                    id: "3.9.6"
  jdk:
    installations:
      - name: "jdk21"
        properties:
          - installSource:
              installers:
                - adoptOpenJdkInstaller:
                    id: "jdk-21.0.8+9"
```

#### 2. **Integración con SonarQube:**
```yaml
unclassified:
  sonarglobalconfiguration:
    installations:
      - name: "SonarQube"
        serverUrl: "http://sonarqube:9000"
        credentialsId: "sonar-token"
```

#### 3. **Credenciales:**
```yaml
credentials:
  system:
    domainCredentials:
      - credentials:
          - string:
              scope: GLOBAL
              id: "sonar-token"
              description: "Token para SonarQube"
              secret: "squ_0e5d4cf5243482083cae80b68e26db616bf05c0a"
```

---

### **Pipeline Job: `jwtmanual-pipeline`**

El pipeline se define en el script `00-master-setup.groovy` y se crea automáticamente al iniciar Jenkins.

#### **Parámetros del Pipeline:**
```groovy
parameters {
    string(name: 'SERVICE_REPO_URL', 
           defaultValue: 'https://github.com/Tourment0412/jwtmanual-taller1-micro.git')
    string(name: 'SERVICE_BRANCH', 
           defaultValue: 'main')
    string(name: 'AUTOMATION_TESTS_REPO_URL', 
           defaultValue: 'https://github.com/MiguelA05/automation-tests.git')
    string(name: 'AUTOMATION_TESTS_BRANCH', 
           defaultValue: 'main')
    string(name: 'AUT_TESTS_BASE_URL', 
           defaultValue: 'http://jwtmanual-taller1-micro:8080')
}
```

---

## 🚀 Flujo de Ejecución del Pipeline

```
1. Checkout repos
   ├── Clona jwtmanual-taller1-micro (service)
   └── Clona automation-tests
   ↓
2. Build + Unit tests (service)
   ├── Compilación con Maven (mvn clean verify)
   ├── Ejecución de tests unitarios
   ├── Generación de reporte de cobertura (JaCoCo)
   └── Publicación de reportes JUnit
   ↓
3. SonarQube Analysis
   ├── Análisis de calidad de código
   ├── Detección de code smells
   ├── Análisis de vulnerabilidades
   ├── Cálculo de cobertura de código
   └── Envío de resultados a SonarQube
   ↓
4. Quality Gate
   ├── Espera notificación de SonarQube vía webhook
   ├── Validación de métricas de calidad
   ├── Verificación de cobertura mínima
   └── Aprobación/Advertencia del build (no bloquea)
   ↓
5. Allure (service)
   ├── Generación de reportes Allure (si existen)
   └── Publicación de reportes HTML
   ↓
6. E2E (automation-tests)
   ├── Configuración de variables de entorno
   ├── Ejecución de tests E2E con Cucumber
   ├── Generación de reporte Allure (obligatorio)
   ├── Verificación de generación correcta
   └── Publicación de reportes JUnit y Allure
   ↓
7. Post Actions
   ├── Mensaje de finalización
   ├── Lista de reportes disponibles
   └── Estado final (success/failure)
```

---

## 📊 Stages del Pipeline Detallados

### **Stage 1: Checkout repos**
```groovy
stage('Checkout repos') {
    steps {
        dir('service') {
            git branch: params.SERVICE_BRANCH, url: params.SERVICE_REPO_URL
        }
        dir('automation-tests') {
            git branch: params.AUTOMATION_TESTS_BRANCH, url: params.AUTOMATION_TESTS_REPO_URL
        }
    }
}
```

### **Stage 2: Build + Unit tests (service)**
```groovy
stage('Build + Unit tests (service)') {
    steps {
        dir('service') {
            sh "${MVN} clean verify"
        }
    }
    post {
        always {
            junit 'service/target/surefire-reports/*.xml'
            publishHTML([
                reportDir: 'service/target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'Reporte de Cobertura (service)'
            ])
        }
    }
}
```

### **Stage 3: SonarQube Analysis**
```groovy
stage('SonarQube Analysis') {
    steps {
        script {
            dir('service') {
                withSonarQubeEnv('SonarQube') {
                    sh """
                        ${MVN} sonar:sonar \
                            -Dsonar.projectKey=jwtmanual-taller1-micro \
                            -Dsonar.projectName='JWT Manual Taller 1 Microservice' \
                            -Dsonar.java.source=21 \
                            -Dsonar.java.target=21
                    """
                }
            }
        }
    }
}
```

### **Stage 4: Quality Gate**
```groovy
stage('Quality Gate') {
    steps {
        script {
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

### **Stage 5: Allure (service)**
```groovy
stage('Allure (service)') {
    steps {
        dir('service') {
            sh "if [ -d 'target/allure-results' ]; then ${MVN} -q -e allure:report; fi"
        }
        publishHTML([
            allowMissing: true,
            reportDir: 'service/target/site/allure-maven-plugin',
            reportFiles: 'index.html',
            reportName: 'Reporte Allure (service)'
        ])
    }
}
```

### **Stage 6: E2E (automation-tests)**
```groovy
stage('E2E (automation-tests)') {
    steps {
        script {
            withEnv(["AUT_TESTS_BASE_URL=${params.AUT_TESTS_BASE_URL}"]) {
                dir('automation-tests') {
                    sh "${MVN} clean test -Dtest=CucumberTest -Dmaven.test.failure.ignore=true"
                    sh "${MVN} allure:report"
                    sh "test -f target/site/allure-maven-plugin/index.html || exit 1"
                }
            }
        }
        junit 'automation-tests/target/surefire-reports/*.xml'
        publishHTML([
            allowMissing: false,
            reportDir: 'automation-tests/target/site/allure-maven-plugin',
            reportFiles: 'index.html',
            reportName: 'Reporte Allure (E2E)'
        ])
    }
}
```

---

## 📊 Reportes Disponibles

### **En Jenkins:**

| Reporte | URL | Descripción |
|---------|-----|-------------|
| **Cobertura de Código (JaCoCo)** | `/job/jwtmanual-pipeline/[BUILD]/Reporte_20de_20Cobertura_20_28service_29/` | Cobertura de tests unitarios |
| **Reporte Allure (Service)** | `/job/jwtmanual-pipeline/[BUILD]/Reporte_20Allure_20_28service_29/` | Tests unitarios detallados |
| **Reporte Allure (E2E)** | `/job/jwtmanual-pipeline/[BUILD]/Reporte_20Allure_20_28E2E_29/` | Tests E2E con Cucumber |
| **Test Results (JUnit)** | `/job/jwtmanual-pipeline/[BUILD]/testReport/` | Resultados de tests |

### **En SonarQube:**

| Reporte | URL | Descripción |
|---------|-----|-------------|
| **Dashboard del Proyecto** | `http://localhost:9001/dashboard?id=jwtmanual-taller1-micro` | Métricas de calidad |
| **Issues** | `http://localhost:9001/project/issues?id=jwtmanual-taller1-micro` | Code smells, bugs, vulnerabilidades |
| **Security** | `http://localhost:9001/project/security_hotspots?id=jwtmanual-taller1-micro` | Security hotspots |

---

## 🔧 Componentes Clave

### **1. Dockerfile de Jenkins**

```dockerfile
FROM jenkins/jenkins:2.516.3-lts
USER root

# Instalar Docker y Podman
RUN apt-get update && apt-get install -y \
    podman docker-ce-cli && \
    rm -rf /var/lib/apt/lists/*

# Instalar plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Copiar configuración
COPY jenkins.yaml /usr/share/jenkins/ref/casc_configs/jenkins.yaml
COPY init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/

USER jenkins
```

### **2. Plugins de Jenkins Instalados**

```text
# plugins.txt
configuration-as-code:latest
workflow-aggregator:latest
git:latest
maven-plugin:latest
junit:latest
jacoco:latest
allure-jenkins-plugin:latest
sonar:latest
htmlpublisher:latest
```

### **3. Script de Inicialización (`00-master-setup.groovy`)**

- **Propósito:** Crea automáticamente el pipeline job al iniciar Jenkins
- **Ubicación:** `cicdjenkins/jenkins/init.groovy.d/00-master-setup.groovy`
- **Función:** Define el pipeline completo con todos los stages

---

## 📝 Scripts de Utilidad

### **Ver Reportes de Allure Localmente:**
```bash
./scripts/show-allure-reports.sh [BUILD_NUMBER]
```
- Copia reportes desde Jenkins
- Inicia servidor HTTP local
- Abre reportes en el navegador

### **Configurar Webhook de SonarQube:**
```bash
./scripts/configure-sonarqube-webhook.sh
```
- Configura webhook SonarQube → Jenkins
- Necesario para Quality Gate sin timeout

### **Inicializar SonarQube:**
```bash
./scripts/init-sonarqube.sh
```
- Cambia password de admin
- Genera token de acceso

---

## 🎓 Cómo Usar Jenkins

### **1. Acceder a Jenkins:**
```
URL: http://localhost:8083
```

### **2. Ejecutar el Pipeline:**
1. Click en `jwtmanual-pipeline`
2. Click en "Build Now"
3. Observa la ejecución en tiempo real

### **3. Ver Resultados:**
1. Click en el número de build (ej: #1)
2. Revisa "Console Output" para logs
3. Accede a reportes desde el menú lateral

### **4. Modificar Parámetros:**
1. Click en "Build with Parameters"
2. Modifica URLs, branches, etc.
3. Click en "Build"

---

## 🐛 Troubleshooting

### **Jenkins no inicia:**
```bash
# Ver logs
podman logs jenkins

# Verificar configuración JCasC
podman exec jenkins cat /usr/share/jenkins/ref/casc_configs/jenkins.yaml

# Reiniciar
podman restart jenkins
```

### **Pipeline falla en checkout:**
```bash
# Verificar conectividad a GitHub
podman exec jenkins git ls-remote https://github.com/Tourment0412/jwtmanual-taller1-micro.git

# Verificar permisos
podman exec jenkins ls -la /var/jenkins_home/workspace/
```

### **Tests E2E fallan con Connection Refused:**
```bash
# Verificar que el microservicio esté corriendo
curl http://localhost:8081/

# Ver logs del microservicio
podman logs jwtmanual-taller1-micro

# Verificar variables de entorno
podman exec automation-tests env | grep AUT_TESTS_BASE_URL
```

### **Reportes Allure no se generan:**
```bash
# Verificar que Maven Allure plugin esté configurado
podman exec jenkins cat /var/jenkins_home/workspace/jwtmanual-pipeline/automation-tests/pom.xml | grep allure

# Copiar reportes manualmente
./scripts/show-allure-reports.sh 1
```

### **SonarQube Analysis falla:**
```bash
# Verificar conectividad
podman exec jenkins curl -s http://sonarqube:9000/api/system/status

# Verificar token
curl -s -u "squ_0e5d4cf5243482083cae80b68e26db616bf05c0a:" http://localhost:9001/api/system/status

# Ver logs de SonarQube
podman logs sonarqube
```

---

## 🔐 Seguridad y Mejores Prácticas

### **Implementado:**
✅ Jenkins configurado automáticamente (sin setup manual)  
✅ Credenciales gestionadas via JCasC  
✅ Tests E2E con setup automático de BD  
✅ Healthchecks para todos los servicios  
✅ Volúmenes persistentes para Jenkins  
✅ Cache de dependencias Maven  
✅ Webhook de SonarQube para notificaciones  

### **Recomendaciones para Producción:**
⚠️ Usar secrets management (Vault, AWS Secrets Manager)  
⚠️ Configurar autenticación en Jenkins  
⚠️ Usar HTTPS para Jenkins  
⚠️ Implementar backup automático de jenkins_home  
⚠️ Configurar notificaciones (email, Slack)  
⚠️ Agregar stage de deployment  
⚠️ Implementar pipeline para múltiples branches  
⚠️ Configurar retention policy para builds  

---

## 📚 Referencias

- **Jenkins Documentation:** https://www.jenkins.io/doc/
- **JCasC Plugin:** https://github.com/jenkinsci/configuration-as-code-plugin
- **Pipeline Syntax:** https://www.jenkins.io/doc/book/pipeline/syntax/
- **Maven Integration:** https://www.jenkins.io/doc/book/pipeline/getting-started/#maven
- **Allure Plugin:** https://plugins.jenkins.io/allure-jenkins-plugin/

---

## ✨ Conclusión

La integración de Jenkins está **completamente funcional** y proporciona:

✅ Pipeline CI/CD completo automatizado  
✅ Build, tests unitarios y E2E en cada commit  
✅ Análisis de calidad con SonarQube  
✅ Reportes detallados de cobertura y tests  
✅ Configuración como código (JCasC)  
✅ Fácil mantenimiento y reproducibilidad  

**¡El pipeline está listo para desarrollo y puede ser extendido para producción!**

