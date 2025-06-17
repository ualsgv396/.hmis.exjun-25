# hmisex2425

**Sergio Gómez Vico**

## Pipeline Jenkins

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
```

## Configuración POM

### Build Plugins

```xml
<build>
  <plugins>
    <plugin>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.13.0</version>
      <configuration>
        <release>21</release>
      </configuration>
    </plugin>

    <plugin>
      <groupId>org.jacoco</groupId>
      <artifactId>jacoco-maven-plugin</artifactId>
      <version>0.8.12</version>
      <executions>
        <execution>
          <id>prepare-agent</id>
          <goals>
            <goal>prepare-agent</goal>
          </goals>
        </execution>
        <!-- attached to Maven test phase -->
        <execution>
          <id>report</id>
          <phase>test</phase>
          <goals>
            <goal>report</goal>
          </goals>
        </execution>
        <execution>
          <id>jacoco-check</id>
          <goals>
            <goal>check</goal>
          </goals>
          <configuration>
            <rules>
              <rule>
                <element>PACKAGE</element>
                <limits>
                  <limit>
                    <counter>LINE</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.9</minimum>
                  </limit>
                </limits>
              </rule>
            </rules>
          </configuration>
        </execution>
      </executions>
    </plugin>

    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-javadoc-plugin</artifactId>
      <version>3.5.0</version>
      <configuration>
        <show>private</show>
      </configuration>
    </plugin>

    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-javadoc-plugin</artifactId>
      <version>3.5.0</version>
      <configuration> 
        <!-- <reportOutputDirectory>
          ${project.reporting.outputDirectory}/../../docs
        </reportOutputDirectory> -->
        <doclet>nl.talsmasoftware.umldoclet.UMLDoclet</doclet>
        <docletArtifact>
          <groupId>nl.talsmasoftware</groupId>
          <artifactId>umldoclet</artifactId>
          <version>2.1.0</version>
        </docletArtifact>
        <additionalOptions>
          <!-- <additionalOption>-umlImageFormat svg_img,png</additionalOption> -->
          <additionalOption>-umlImageFormat svg_img</additionalOption>
          <additionalOption>-private</additionalOption>
          <!-- <additionalOption>-createPumlFiles</additionalOption> -->
          <additionalOption>-umlExcludedTypeReferences</additionalOption> <!-- añade metodos toString() y equals() -->
        </additionalOptions>
      </configuration>
    </plugin>
  </plugins>
</build>
```

### Dependencias

```xml
<dependencies>
  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>5.12.2</version>
    <scope>test</scope>
  </dependency>
  
  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.12.2</version>
    <scope>test</scope>
  </dependency>

  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
    <version>5.12.2</version>
  </dependency>
</dependencies>
```

## Estructura del Pipeline

El pipeline de Jenkins consta de tres etapas principales:

1. **Git fetch**: Descarga el código del repositorio
2. **Compile, Test, Package**: Ejecuta `mvn clean package` y archiva resultados
3. **Documentation**: Genera documentación Javadoc con diagramas UML

### Características destacadas:

- **Cobertura de código**: JaCoCo configurado con 90% de cobertura mínima
- **Documentación UML**: Integración con UMLDoclet para generar diagramas
- **Pruebas parametrizadas**: JUnit 5 con soporte para pruebas parametrizadas
- **Java 21**: Configurado para usar la versión más reciente de Java
