[![Build Status](https://ci-cd.springdoc.org:8443/buildStatus/icon?job=springdoc-openapi-maven-plugin-IC)](https://ci-cd.springdoc.org:8443/view/springdoc-openapi-maven/job/springdoc-openapi-maven-plugin-IC/)

[![Build Status](https://ci-cd.springdoc.org:8443/view/springdoc-openapi-maven/job/springdoc-openapi-maven-plugin-IC/badge/icon)](https://ci-cd.springdoc.org:8443/view/springdoc-openapi-maven/job/springdoc-openapi-maven-plugin-IC/)

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
   <version>${spring-boot-maven-plugin.version}</version>
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
   <version>last-release-version</version>
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
*  outputDir: The output directory, where to generate the OpenAPI description. The directory name shouldn't start with "/".
    * The default value is: ${project.build.directory}
*   outputFileName: The file name that contains the OpenAPI description.  
    * The default value is: openapi.json
*   skip: Skip execution if set to true.
    * The default value is: false
*   headers: List of headers to send in request
    * The default value is empty
*   failOnError: Fail build on error, if set to `true`. Default is `false`.
    * The default value is false

```xml
<plugin>
 <groupId>org.springdoc</groupId>
 <artifactId>springdoc-openapi-maven-plugin</artifactId>
 <version>last-release-version</version>
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
  <outputDir>home/springdoc/maven-output</outputDir>
  <skip>false</skip>
  <failOnError>true</failOnError>
  <headers>
    <header1key>header1value</header1key>
    <header2key>header2value</header2key>
  </headers>
 </configuration>
</plugin>
```

# **Thank you for the support**

* Thanks a lot [JetBrains](https://www.jetbrains.com/?from=springdoc-openapi) for supporting springdoc-openapi project.

![JenBrains logo](https://springdoc.org/img/jetbrains.svg)
