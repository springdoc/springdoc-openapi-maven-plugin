/*
 * Copyright 2019-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springdoc.maven.plugin;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.apache.maven.reporting.MavenReportException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springdoc.maven.plugin.SpringDocMojoTest.set;

/**
 * Tests for {@link SpringDocOpenApiReportMojo}.
 *
 * @author bnasslahsen
 */
class SpringDocOpenApiReportMojoTest {

	@TempDir
	Path tempDir;

	@Test
	void writesTheSwaggerUiPageAndTheSpec() throws Exception {
		String spec = "{\"openapi\":\"3.1.0\",\"info\":{\"title\":\"</script>\"}}";
		SpringDocOpenApiReportMojo report = report(spec);

		report.executeReport(Locale.ENGLISH);

		Path dir = tempDir.resolve("site/springdoc-openapi");
		String html = new String(Files.readAllBytes(dir.resolve("index.html")), StandardCharsets.UTF_8);
		assertTrue(html.contains("https://unpkg.com/swagger-ui-dist@5.34.1/swagger-ui-bundle.js"));
		assertTrue(html.contains("spec: {\"openapi\":\"3.1.0\",\"info\":{\"title\":\"<\\/script>\"}},"));
		assertEquals(spec, new String(Files.readAllBytes(dir.resolve("openapi.json")), StandardCharsets.UTF_8));
	}

	@Test
	void rejectsYaml() throws Exception {
		SpringDocOpenApiReportMojo report = report("openapi: 3.1.0\n");

		MavenReportException e = assertThrows(MavenReportException.class, () -> report.executeReport(Locale.ENGLISH));

		assertTrue(e.getMessage().startsWith("The OpenAPI spec is not JSON"), e.getMessage());
	}

	@Test
	void failsWithoutFileOrRunningApplication() throws Exception {
		SpringDocOpenApiReportMojo report = report(null);
		// Port 1 is reserved and normally closed, so the connection is refused at once
		set(report, "apiDocsUrl", "http://localhost:1/v3/api-docs");

		MavenReportException e = assertThrows(MavenReportException.class, () -> report.executeReport(Locale.ENGLISH));

		assertTrue(e.getMessage().startsWith("Could not load OpenAPI spec from file"), e.getMessage());
	}

	private SpringDocOpenApiReportMojo report(String spec) throws Exception {
		Path specFile = tempDir.resolve("openapi.json");
		if (spec != null) {
			Files.write(specFile, spec.getBytes(StandardCharsets.UTF_8));
		}
		SpringDocOpenApiReportMojo report = new SpringDocOpenApiReportMojo();
		set(report, "apiDocsFile", specFile.toFile());
		set(report, "siteReportName", "OpenAPI Documentation");
		set(report, "siteReportDirectory", "springdoc-openapi");
		set(report, "swaggerUiVersion", "5.34.1");
		set(report, "timeout", 5);
		report.setReportOutputDirectory(tempDir.resolve("site").toFile());
		return report;
	}

}
