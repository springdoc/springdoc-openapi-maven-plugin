[![Build Status](https://travis-ci.org/springdoc/springdoc-openapi-maven-plugin.svg?branch=master)](https://travis-ci.org/springdoc/springdoc-openapi-maven-plugin)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=org.springdoc%3Aspringdoc-openapi-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.springdoc%3Aspringdoc-openapi-maven-plugin)

## **Introduction to springdoc-openapi-maven-plugin**

The aim of springdoc-openapi-maven-plugin is to generate json and yaml OpenAPI description  during build time. 
The plugin works during integration-tests phase, and generate the OpenAPI description. 
The plugin works in conjunction with spring-boot-maven plugin. 

You can test it during the integration tests phase using the maven command:

```properties
mvn verify
```

In order to use this functionality, you need to add the plugin declaration on the plugins section of your pom.xml:

```xml
<plugin>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-maven-plugin</artifactId>
 <version>2.1.8.RELEASE</version>
 <executions>
  <execution>
   <id>pre-integration-test</id>
   <goals>
    <goal>start</goal>
   </goals>
  </execution>
  <execution>
   <id>post-integration-test</id>
   <goals>
    <goal>stop</goal>
   </goals>
  </execution>
 </executions>
</plugin>
<plugin>
 <groupId>org.springdoc</groupId>
 <artifactId>springdoc-openapi-maven-plugin</artifactId>
 <version>0.2</version>
 <executions>
  <execution>
   <id>integration-test</id>
   <goals>
    <goal>generate</goal>
   </goals>
  </execution>
 </executions>
<plugin>
```
			
## **Custom settings of the springdoc-openapi-maven-plugin**

It possible to customise the following plugin properties:
*   apiDocsUrl: The local url of your (json or yaml). 
    * The default value is: http://localhost:8080/v3/api-docs
*  outputDir: The output directory, where to generate the OpenAPI description.
    * The default value is: ${project.build.directory}
*   outputFileName: The file name that contains the OpenAPI description.  
    * The default value is: openapi.json

```xml
<plugin>
 <groupId>org.springdoc</groupId>
 <artifactId>springdoc-openapi-maven-plugin</artifactId>
 <version>0.2</version>
 <executions>
  <execution>
   <id>integration-test</id>
   <goals>
    <goal>generate</goal>
   </goals>
  </execution>
 </executions>
 <configuration>
  <apiDocsUrl>http://localhost:8080/v3/api-docs</apiDocsUrl>
  <outputFileName>openapi.json</outputFileName>
  <outputDir>/home/springdoc/maven-output</outputDir>
 </configuration>
</plugin>
```
