pipeline {
    agent any

    tools {
        maven 'Maven-3.9'   // Configurado en jenkins.yaml
        jdk 'jdk21'         // Configurado en jenkins.yaml
    }

    environment {
        SONARQUBE = 'SonarQube-Server' // Nombre de la instalación Sonar en Jenkins
    }

    stages {
        stage('Clonar proyecto') {
            steps {
                // Ajusta la URL a tu repositorio real
                git branch: 'main', url: 'https://github.com/Tourment0412/jwtmanual-taller1-micro.git'
            }
        }

        stage('Compilar y pruebas unitarias') {
            steps {
                sh 'mvn clean verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    // Publicar reportes de cobertura si están disponibles
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'Reporte de Cobertura'
                    ])
                }
            }
        }

        // stage('Generar reporte Allure') {
        //     steps {
        //         sh 'mvn allure:report'
        //     }
        //     post {
        //         always {
        //             publishHTML([
        //                 allowMissing: false,
        //                 alwaysLinkToLastBuild: true,
        //                 keepAll: true,
        //                 reportDir: 'target/site/allure-maven-plugin',
        //                 reportFiles: 'index.html',
        //                 reportName: 'Reporte Allure'
        //             ])
        //         }
        //     }
        // }

        stage('Analizar con SonarQube') {
            steps {
                withSonarQubeEnv("${SONARQUBE}") {
                    sh 'mvn sonar:sonar -Dsonar.projectKey=jwtgeneratortaller1 -Dsonar.host.url=http://sonarqube:9000'
                }
            }
        }

        stage('Verificar calidad') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Construir imagen Docker') {
            steps {
                script {
                    def image = docker.build("jwtgeneratortaller1:${env.BUILD_NUMBER}")
                    // Opcional: push a registry
                    // docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                    //     image.push("${env.BUILD_NUMBER}")
                    //     image.push("latest")
                    // }
                }
            }
        }
    }
}
