[![Build](https://img.shields.io/github/actions/workflow/status/springdoc/springdoc-openapi-maven-plugin/snapshot.yml?branch=master&label=build)](https://github.com/springdoc/springdoc-openapi-maven-plugin/actions/workflows/snapshot.yml?query=branch%3Amaster)

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

## **Site Report: Swagger UI in Maven Project Reports**

The plugin provides a `report` goal that generates a static Swagger UI page and places it in the Maven site under **Project Reports**.

The report reads the OpenAPI spec from the file produced by the `generate` goal (default `target/openapi.json`). If the file does not exist, it falls back to fetching from `apiDocsUrl`.

**Recommended workflow:** run `mvn verify` first (to generate the spec), then `mvn site`.

The report only supports a JSON spec (`/v3/api-docs`), not YAML.

Add the plugin to the `<reporting>` section of your POM:

```xml
<reporting>
  <plugins>
    <plugin>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-maven-plugin</artifactId>
      <version>last-release-version</version>
      <reportSets>
        <reportSet>
          <reports>
            <report>report</report>
          </reports>
        </reportSet>
      </reportSets>
    </plugin>
  </plugins>
</reporting>
```

### Report configuration properties

| Property | Default | Description |
|---|---|---|
| `springdoc.report.skip` | `false` | Skip report generation |
| `springdoc.apiDocsFile` | `${project.build.directory}/openapi.json` | Path to a pre-generated OpenAPI spec file |
| `springdoc.apiDocsUrl` | `http://localhost:8080/v3/api-docs` | Fallback URL when spec file is absent |
| `springdoc.report.name` | `OpenAPI Documentation` | Name in Project Reports navigation |
| `springdoc.report.description` | `OpenAPI specification rendered with Swagger UI` | Description in Project Reports |
| `springdoc.report.directory` | `springdoc-openapi` | Subdirectory under the site output |
| `springdoc.swaggerUiVersion` | `5.34.1` | Swagger UI version for CDN (unpkg.com) |

### Example with custom configuration

```xml
<reporting>
  <plugins>
    <plugin>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-maven-plugin</artifactId>
      <version>last-release-version</version>
      <configuration>
        <siteReportName>API Reference</siteReportName>
        <swaggerUiVersion>5.18.2</swaggerUiVersion>
      </configuration>
      <reportSets>
        <reportSet>
          <reports>
            <report>report</report>
          </reports>
        </reportSet>
      </reportSets>
    </plugin>
  </plugins>
</reporting>
```

# **Thank you for the support**

* Thanks a lot [JetBrains](https://www.jetbrains.com/?from=springdoc-openapi) for supporting springdoc-openapi project.

![JenBrains logo](https://springdoc.org/img/jetbrains.svg)
