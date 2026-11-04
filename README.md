<p align="center">
  <a href="https://springdoc.org">
    <img src="https://springdoc.org/img/banner-logo.svg" alt="springdoc-openapi" width="420">
  </a>
</p>

<h3 align="center">📦 Generate the OpenAPI description of your Spring Boot application at build time, with Maven</h3>

<p align="center">
  <a href="https://github.com/springdoc/springdoc-openapi-maven-plugin/actions/workflows/snapshot.yml?query=branch%3Amaster"><img src="https://img.shields.io/github/actions/workflow/status/springdoc/springdoc-openapi-maven-plugin/snapshot.yml?branch=master&style=flat-square&logo=githubactions&logoColor=white&label=build" alt="Build"></a>
  <a href="https://central.sonatype.com/artifact/org.springdoc/springdoc-openapi-maven-plugin"><img src="https://img.shields.io/maven-central/v/org.springdoc/springdoc-openapi-maven-plugin?style=flat-square&logo=apachemaven&label=Maven%20Central&color=blue" alt="Maven Central"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/springdoc/springdoc-openapi-maven-plugin?style=flat-square&logo=apache&color=blue" alt="License"></a>
  <a href="https://github.com/springdoc/springdoc-openapi-maven-plugin"><img src="https://img.shields.io/github/stars/springdoc/springdoc-openapi-maven-plugin?style=flat-square&logo=github&color=yellow" alt="GitHub stars"></a>
  <a href="https://stackoverflow.com/questions/tagged/springdoc"><img src="https://img.shields.io/stackexchange/stackoverflow/t/springdoc?style=flat-square&logo=stackoverflow&logoColor=white&label=Stack%20Overflow&color=orange" alt="Stack Overflow"></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Maven-plugin-C71A36?style=flat-square&logo=apachemaven&logoColor=white" alt="Maven plugin">
  <img src="https://img.shields.io/badge/Java-8%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 8+">
  <img src="https://img.shields.io/badge/OpenAPI-JSON%20%7C%20YAML-6BA539?style=flat-square&logo=openapiinitiative&logoColor=white" alt="OpenAPI JSON and YAML">
  <img src="https://img.shields.io/badge/Swagger%20UI-site%20report-85EA2D?style=flat-square&logo=swagger&logoColor=black" alt="Swagger UI site report">
</p>

<p align="center">
  <a href="https://springdoc.org/plugins.html"><b>📖 Documentation</b></a> ·
  <a href="#getting-started">🚀 Getting started</a> ·
  <a href="#configuration">⚙️ Configuration</a> ·
  <a href="#site-report">📊 Site report</a> ·
  <a href="https://github.com/springdoc/springdoc-openapi">🍃 springdoc-openapi</a>
</p>

<h3 align="center">💖 Sponsored by</h3>

<p align="center">
  <a href="https://opensource.mercedes-benz.com/"><img src="https://springdoc.org/img/mercedes-benz.webp" alt="Mercedes-Benz" height="56"></a>&nbsp;&nbsp;&nbsp;
  <a href="https://www.dm-jobs.com/dmTECH/?locale=de_DE&wt_mc=.display.github.sponsoring.logo"><img src="https://springdoc.org/img/dmTECH_Logo.webp" alt="dmTECH" height="56"></a>&nbsp;&nbsp;&nbsp;
  <a href="https://www.contrastsecurity.com/"><img src="https://springdoc.org/img/contrastsecurity.svg" alt="Contrast Security" height="40"></a>&nbsp;&nbsp;&nbsp;
  <a href="https://www.lvm.de/"><img src="https://springdoc.org/img/lvm-logo.webp" alt="LVM Versicherung" height="48"></a>&nbsp;&nbsp;&nbsp;
  <a href="https://gdnext.com/"><img src="https://springdoc.org/img/gdnext.webp" alt="GDNext" height="40"></a>
</p>

<p align="center">
  <a href="https://github.com/sponsors/springdoc"><img src="https://img.shields.io/badge/Sponsor-GitHub%20Sponsors-EA4AAA?style=for-the-badge&logo=githubsponsors&logoColor=white" alt="Sponsor on GitHub"></a>
  <a href="https://opencollective.com/springdoc"><img src="https://img.shields.io/badge/Support-Open%20Collective-7FADF2?style=for-the-badge&logo=opencollective&logoColor=white" alt="Support on Open Collective"></a>
</p>

<p align="center"><sub>If you ❤️ springdoc-openapi, <a href="https://springdoc.org/sponsor.html">become a sponsor</a>: it covers the project expenses and helps it grow.</sub></p>

---

**springdoc-openapi-maven-plugin** writes the OpenAPI description of your application to a file during the build,
so you can commit it, diff it, publish it with your artifacts, or feed it to code generators. ✨

It works together with the `spring-boot-maven-plugin`: the application is started before the integration tests,
the plugin downloads the description that [springdoc-openapi](https://github.com/springdoc/springdoc-openapi)
serves at `/v3/api-docs`, and the application is stopped again. Because the description comes from the running
application, it is exactly what springdoc-openapi serves at runtime.

<a id="getting-started"></a>

## 🚀 Getting started

Add both plugins to the `<build><plugins>` section of your `pom.xml`:

```xml
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
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
  <version>1.5</version>
  <executions>
    <execution>
      <id>integration-test</id>
      <goals>
        <goal>generate</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Then run:

```shell
mvn verify
```

👉 The description is written to `target/openapi.json`.

## 🧭 Compatibility

The plugin only calls an HTTP endpoint, so it works with any application that serves an OpenAPI description:

| Spring Boot | springdoc-openapi |
|:---:|:---:|
| 🍃 4.x | 3.x |
| 🍃 3.x | 2.x |
| 🍂 2.x and 1.x | 1.x |

The plugin runs on Java 8 and later.

## 🎯 Goals

| Goal | Default phase | Description |
|---|---|---|
| `springdoc-openapi:generate` | `integration-test` | Downloads the OpenAPI description from the running application and writes it to a file |
| `springdoc-openapi:report` | `site` | Adds a Swagger UI page to the Maven site, see [Site report](#site-report) (since 1.6) |

<a id="configuration"></a>

## ⚙️ Configuration

Each parameter of the `generate` goal can also be set from the command line with its user property, for
example `mvn verify -Dspringdoc.skip=true`.

| Parameter | User property | Default | Description |
|---|---|---|---|
| `apiDocsUrl` | `springdoc.apiDocsUrl` | `http://localhost:8080/v3/api-docs` | URL of the OpenAPI description (JSON or YAML) |
| `outputFileName` | `springdoc.outputFileName` | `openapi.json` | Name of the generated file |
| `outputDir` | `springdoc.outputDir` | `${project.build.directory}` | Directory of the generated file |
| `attachArtifact` | `springdoc.attachArtifact` | `false` | Attach the file to the project, so it is installed and deployed with the other artifacts (classifier `openapi`) |
| `headers` | | none | HTTP headers sent with the request, for example for authentication |
| `skip` | `springdoc.skip` | `false` | Skip the execution |
| `failOnError` | `springdoc.failOnError` | `false` | Fail the build if the description cannot be downloaded or written |

```xml
<plugin>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-maven-plugin</artifactId>
  <version>1.5</version>
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
    <outputDir>${project.build.directory}/openapi</outputDir>
    <attachArtifact>true</attachArtifact>
    <failOnError>true</failOnError>
    <headers>
      <Authorization>Bearer my-token</Authorization>
    </headers>
  </configuration>
</plugin>
```

> [!TIP]
> Set `failOnError` to `true` on CI. By default, a failure is only logged, and the build goes on without the file.

### 💡 Recipes

- 📄 **YAML**: point `apiDocsUrl` to `http://localhost:8080/v3/api-docs.yaml` and set `outputFileName` to `openapi.yaml`.
- 🗂️ **Groups**: add one `<execution>` per [group](https://springdoc.org/faq.html#_how_can_i_define_multiple_openapi_definitions_in_one_spring_boot_project),
  each with its own `apiDocsUrl` (for example `http://localhost:8080/v3/api-docs/admin`) and `outputFileName`.
- ✨ **Pretty-printed JSON**: the file is saved exactly as served, so enable
  `springdoc.writer-with-default-pretty-printer=true` in the application, for example in the `spring-boot-maven-plugin`
  configuration:

  ```xml
  <arguments>
    <argument>--springdoc.writer-with-default-pretty-printer=true</argument>
  </arguments>
  ```
- 🔌 **Another port**: change the port of the application and `apiDocsUrl` together, for example
  `-Dspring-boot.run.arguments=--server.port=9090 -Dspringdoc.apiDocsUrl=http://localhost:9090/v3/api-docs`.

<a id="site-report"></a>

## 📊 Site report

> [!NOTE]
> The `report` goal is available since version 1.6.

The `report` goal adds an **OpenAPI Documentation** page, rendered with Swagger UI, under **Project Reports**
in the Maven site. It reads the file written by the `generate` goal (by default `target/openapi.json`), and if
that file does not exist, it fetches the description from `apiDocsUrl`.

Add the plugin to the `<reporting>` section of your `pom.xml`:

```xml
<reporting>
  <plugins>
    <plugin>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-maven-plugin</artifactId>
      <version>1.6</version>
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

Then generate the description and the site in the same build:

```shell
mvn verify site
```

👉 The page is written to `target/site/springdoc-openapi/index.html`.

| Parameter | User property | Default | Description |
|---|---|---|---|
| `apiDocsFile` | `springdoc.apiDocsFile` | `${project.build.directory}/openapi.json` | File written by the `generate` goal |
| `apiDocsUrl` | `springdoc.apiDocsUrl` | `http://localhost:8080/v3/api-docs` | URL used when `apiDocsFile` does not exist |
| `headers` | | none | HTTP headers sent with the request to `apiDocsUrl` |
| `siteReportName` | `springdoc.report.name` | `OpenAPI Documentation` | Name in the Project Reports menu |
| `siteReportDescription` | `springdoc.report.description` | `OpenAPI specification rendered with Swagger UI` | Description in the Project Reports page |
| `siteReportDirectory` | `springdoc.report.directory` | `springdoc-openapi` | Directory of the page in the site |
| `swaggerUiVersion` | `springdoc.swaggerUiVersion` | `5.34.1` | Swagger UI version, loaded from unpkg.com |
| `skip` | `springdoc.report.skip` | `false` | Skip the report |

> [!IMPORTANT]
> The report only supports a JSON description (`/v3/api-docs`), not YAML. The Swagger UI files are loaded from
> unpkg.com, so the page needs internet access when it is viewed.

## 💬 Getting help

- 📖 Read the [plugin documentation](https://springdoc.org/plugins.html) and the
  [springdoc-openapi documentation](https://springdoc.org)
- 🙋 Ask questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/springdoc) with the `springdoc` tag
- 🐛 Report bugs in [GitHub issues](https://github.com/springdoc/springdoc-openapi-maven-plugin/issues), with a minimal reproducer

> [!NOTE]
> springdoc-openapi is a community project, not maintained by the Spring team.

## 🤝 Contributing

Contributions are welcome! See [CONTRIBUTING.adoc](CONTRIBUTING.adoc) and the
[Code of Conduct](https://github.com/springdoc/springdoc-openapi/blob/main/CODE_OF_CONDUCT.md).

To build the plugin and install it in your local Maven repository:

```shell
./mvnw install
```

Thanks to all our [contributors](https://github.com/springdoc/springdoc-openapi-maven-plugin/graphs/contributors) 🙏

<a href="https://github.com/springdoc/springdoc-openapi-maven-plugin/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=springdoc/springdoc-openapi-maven-plugin" alt="Contributors">
</a>

Special thanks to [JetBrains](https://www.jetbrains.com/?from=springdoc-openapi) for their support.

<p align="center">
  <a href="https://www.jetbrains.com/?from=springdoc-openapi"><img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jetbrains.svg" alt="JetBrains logo" height="56"></a>
</p>

## 📄 License

springdoc-openapi-maven-plugin is Open Source software released under the [Apache 2.0 license](LICENSE).
