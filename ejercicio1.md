# Jenkins Pipelines - Ejemplos de Código

## Pipeline Básico

```groovy
pipeline {
  agent any
  tools {
    // Nombre dado a la instalación de Maven en "Tools configuration"
    maven "Default Maven"
  }
  stages {
    stage('Git fetch') { 
      steps {
        // Get some code from a GitHub repository
        git branch: 'main', url: 'https://github.com/ualhmis/MavenEjercicios'
      }
    }
    stage('Compile, Test, Package') { 
      steps {
        // When necessary, use '-f path-to/pom.xml' to give the path to pom.xml
        // Run goal 'package'. It includes compile, test and package.
        sh "mvn  -f sesion07Maven/pom.xml clean package" 
      }
      post { 
        // Record the test results and archive the jar file.
        success {
          junit '**/target/surefire-reports/TEST-*.xml'
          archiveArtifacts '**/target/*.jar'
        }
      }
    }
  }
}
```

## Pipeline Completo con Análisis y Documentación

```groovy
pipeline {
  agent any
  tools {
    // Nombre dado a la instalación de Maven en "Tools configuration"
    maven "maven default"
  }
  stages {
    stage('Git fetch') {
      steps {
        // Get some code from a GitHub repository
        git credentialsId: 'cc8b2b37-731e-4927-8c9e-1d98aa85388f', branch: 'main', url: 'https://github.com/ualhmis2025-equipillo/sesion05torneos.git'
      }
    }
    stage('Compile, Test, Package') {
      steps {
        // When necessary, use '-f path-to/pom.xml' to give the path to pom.xml
        // Run goal 'package'. It includes compile, test and package.
        sh "mvn clean package"
      }
      post {
        // Record the test results and archive the jar file.
        success {
          junit '**/target/surefire-reports/TEST-*.xml'
          archiveArtifacts '**/target/*.jar'
          jacoco( 
            execPattern: '**/target/jacoco.exec',
            classPattern: '**/target/classes',
            sourcePattern: '**/src/',
            exclusionPattern: '**/test/'
          )
          publishCoverage adapters: [jacocoAdapter('**/target/site/jacoco/jacoco.xml')]  
        }
      }
    }
    stage('Analysis') {
      steps {
        // Warnings next generation plugin required
        sh "mvn site"
      }
      post {
        success {
          dependencyCheckPublisher pattern: '**/target/site/es/dependency-check-report.xml'
          recordIssues enabledForFailure: true, tool: checkStyle()
          recordIssues enabledForFailure: true, tool: pmdParser()
          recordIssues enabledForFailure: true, tool: cpd()
          recordIssues enabledForFailure: true, tool: findBugs()
          recordIssues enabledForFailure: true, tool: spotBugs()
        }
      }
    }
    stage('SonarQube analysis') {
        steps {
            withSonarQubeEnv(credentialsId: 'sonar_server', installationName: 'sesion08') { 
                sh 'mvn sonar:sonar' 
            }
        }
    }
    stage('Documentation') {
      steps {
        sh "mvn javadoc:javadoc javadoc:aggregate"
      }
      post{
        success {
          step $class: 'JavadocArchiver', javadocDir: 'target/site/apidocs', keepAll: false
          publishHTML(target: [reportName: 'Maven Site', reportDir: 'target/site', reportFiles: 'index.html', keepAll: false])
        }
      }
    }
  }
}
```
