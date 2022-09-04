[![Build Status](https://travis-ci.org/springdoc/springdoc-openapi-maven-plugin.svg?branch=master)](https://travis-ci.org/springdoc/springdoc-openapi-maven-plugin)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=springdoc_springdoc-openapi-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=springdoc_springdoc-openapi-maven-plugin)

## **Introduction to springdoc-openapi-maven-plugin**

The aim of springdoc-openapi-maven-plugin is to generate json and yaml OpenAPI description during runtime. If you want
to get swagger definitions properly, the application should completely running as locally.
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

The provided spring-boot-maven-plugin configuration will start an application context, which in terms allows us to get the api documentation that is only generated on startup.

If you are wondering how you could make this independent (not relying on local database for startup, or issues with the port), please have a look at [The Tip Section](#tip)

## **Custom settings of the springdoc-openapi-maven-plugin**

It possible to customise the following plugin properties:

* attachArtifact: install / deploy the api doc to the repository
    * The default value is: false
* apiDocsUrl: The local url of your (json or yaml).
    * The default value is: http://localhost:8080/v3/api-docs
* outputDir: The output directory, where to generate the OpenAPI description.
    * The default value is: ${project.build.directory}
* outputFileName: The file name that contains the OpenAPI description.
    * The default value is: openapi.json
* skip: Skip execution if set to true.
    * The default value is: false
* headers: List of headers to send in request
    * The default value is empty

```xml

<plugin>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-maven-plugin</artifactId>
    <version>2.0</version>
    <executions>
        <execution>
            <id>integration-test</id>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <baseUrl>http://localhost:8080</baseUrl>
        <exports>
            <export>
                <path>v3/api-docs</path>
                <outputFileName>openapi.json</outputFileName>
            </export>
            <export>
                <path>v3/api-docs.yaml</path>
                <outputFileName>openapi.yaml</outputFileName>
            </export>
        </exports>
        <outputDir>/home/springdoc/maven-output</outputDir>
        <skip>false</skip>
        <headers>
            <header1key>header1value</header1key>
            <header2key>header2value</header2key>
        </headers>
    </configuration>
</plugin>
```

## **Tip**

To make the build process a little more independent, you can adjust the spring-boot-maven-plugin. To do so, you can
change the configuration to this:

```xml
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <configuration>
    <jvmArguments>-Dspring.application.admin.enabled=true</jvmArguments>
    <environmentVariables>
        <SPRING_PROFILES_ACTIVE>integration</SPRING_PROFILES_ACTIVE>
    </environmentVariables>
  </configuration>
  
  ...
</plugin>
```

This will set the active spring profile, "integration", which on the other hand allows you to add a custom application.properties file, called `application-integration.properties`.

This file might look like this:

```properties
server.port=8090
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

By doing so, you can configure the application context, that is started in the integration-test phase.

This allows you to change the port, making the build independent of the fact that you might have another application instance running

If you add (like in this example) a h2 database, you can make the build completely independent, allowing the application to start without any precondition. At the end of the day, we are only interested in the api that is generated at the startup phase.

# **Thank you for the support**

* Thanks a lot [JetBrains](https://www.jetbrains.com/?from=springdoc-openapi) for supporting springdoc-openapi project.

![JenBrains logo](https://springdoc.org/images/jetbrains.svg)
