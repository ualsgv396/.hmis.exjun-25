# hmisex2425


Sergio Gómez Vico
 ## titulo
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
        git credentialsId: 'IDsgv396', branch: 'main', url: 'https://github.com/ualsgv396/.hmis.exjun-25.git'
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
          junit '/target/surefire-reports/TEST-*.xml'
          archiveArtifacts '/target/*.jar'
          jacoco( 
            execPattern: '/target/jacoco.exec',
            classPattern: '/target/classes',
            sourcePattern: '/src/',
            exclusionPattern: '/test/'
          )
          publishCoverage adapters: [jacocoAdapter('/target/site/jacoco/jacoco.xml')]  
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
