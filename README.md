[![Build Status](https://travis-ci.org/springdoc/springdoc-openapi-maven-plugin.svg?branch=master)](https://travis-ci.org/springdoc/springdoc-openapi-maven-plugin)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=springdoc_springdoc-openapi-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=springdoc_springdoc-openapi-maven-plugin)

## **Introduction to springdoc-openapi-maven-plugin**

The aim of springdoc-openapi-maven-plugin is to generate json and yaml OpenAPI description during runtime. If you want to get swagger definitions properly, the application should completely running as locally.
The plugin works during integration-tests phase, and generate the OpenAPI description. 
The plugin works in conjunction with spring-boot-maven plugin. 

You can test it during the integration tests phase using the maven command:

```shell
mvn verify
```

In order to use this functionality, you need to add the plugin declaration on the plugins section of your pom.xml:

```xml
<plugins>
  <plugin>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-maven-plugin</artifactId>
   <version>2.3.4.RELEASE</version>
   <configuration>
      <jvmArguments>-Dspring.application.admin.enabled=true</jvmArguments>
   </configuration>
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
   <version>1.1</version>
   <executions>
    <execution>
     <id>integration-test</id>
     <goals>
      <goal>generate</goal>
     </goals>
    </execution>
   </executions>
  </plugin>
</plugins>
```
			
## **Custom settings of the springdoc-openapi-maven-plugin**

It possible to customise the following plugin properties:
*   attachArtifact: install / deploy the api doc to the repository
    * The default value is: false
*   apiDocsUrl: The local url of your (json or yaml). 
    * The default value is: http://localhost:8080/v3/api-docs
*  outputDir: The output directory, where to generate the OpenAPI description.
    * The default value is: ${project.build.directory}
*   outputFileName: The file name that contains the OpenAPI description.  
    * The default value is: openapi.json
*   skip: Skip execution if set to true.
    * The default value is: false
*   headers: List of headers to send in request
    * The default value is empty

```xml
<plugin>
 <groupId>org.springdoc</groupId>
 <artifactId>springdoc-openapi-maven-plugin</artifactId>
 <version>1.1</version>
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
  <skip>false</skip>
  <headers>
    <header1key>header1value</header1key>
    <header2key>header2value</header2key>
  </headers>
 </configuration>
</plugin>
```

# **Thank you for the support**

* Thanks a lot [JetBrains](https://www.jetbrains.com/?from=springdoc-openapi) for supporting springdoc-openapi project.

![JenBrains logo](https://springdoc.org/images/jetbrains.svg)
