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

```groovy

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>ual.hmis</groupId>
  <artifactId>torneosdeportivos</artifactId>
  <version>0.0.1-SNAPSHOT</version>
	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
	</properties>

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
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<version>3.5.1</version>
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
					<artifactId>maven-site-plugin</artifactId>
					<version>4.0.0-M7</version>
					<configuration>
						<locales>es,en</locales>
					</configuration>
			</plugin>
			<plugin>
            	<groupId>org.apache.maven.plugins</groupId>
            	<artifactId>maven-checkstyle-plugin</artifactId>
            	<version>3.1.1</version>
        	</plugin>

			<plugin>
            	<groupId>org.apache.maven.plugins</groupId>
            	<artifactId>maven-jxr-plugin</artifactId>
            	<version>3.3.0</version>
        	</plugin>
        	<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-pmd-plugin</artifactId>
				<version>3.20.0</version>
				<configuration>
					<linkXref>true</linkXref>
					<sourceEncoding>utf-8</sourceEncoding>
					<minimumTokens>100</minimumTokens>
					<targetJdk>1.8</targetJdk>
					<skipEmptyReport>false</skipEmptyReport>
					<rulesets>
						<ruleset>/rulesets/java/braces.xml</ruleset>
						<ruleset>/rulesets/java/naming.xml</ruleset>
					</rulesets>
				</configuration>
			</plugin>
			<plugin>
				<groupId>com.github.spotbugs</groupId>
				<artifactId>spotbugs-maven-plugin</artifactId>
				<version>4.7.3.4</version>
				<configuration>
					<effort>Max</effort>
					<threshold>Low</threshold>
					<failOnError>true</failOnError>
					<includeFilterFile>${session.executionRootDirectory}/spotbugs-security-include.xml</includeFilterFile>
					<excludeFilterFile>${session.executionRootDirectory}/spotbugs-security-exclude.xml</excludeFilterFile>
					<plugins>
						<plugin>
							<groupId>com.h3xstream.findsecbugs</groupId>
							<artifactId>findsecbugs-plugin</artifactId>
							<version>1.12.0</version>
						</plugin>
					</plugins>
				</configuration>
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
      		<plugin>
      			<groupId>org.sonarsource.scanner.maven</groupId>
      			<artifactId>sonar-maven-plugin</artifactId>
      			<version>3.9.0.2155</version>
    		</plugin>
		</plugins>
	</build>

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
			<groupId>org.junit.platform</groupId>
			<artifactId>junit-platform-suite-engine</artifactId>
			<version>1.12.2</version>
			<scope>test</scope>
		</dependency>

		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter-params</artifactId>
			<version>5.12.2</version>
		</dependency>
		 <!-- https://mvnrepository.com/artifact/nl.talsmasoftware/umldoclet -->
    	<dependency>
      		<groupId>nl.talsmasoftware</groupId>
      		<artifactId>umldoclet</artifactId>
      		<version>2.1.0</version>
      		<scope>provided</scope>
    	</dependency>
    	
	</dependencies>
</project>
```
