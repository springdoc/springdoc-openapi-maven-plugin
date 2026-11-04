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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SpringDocMojo}.
 *
 * @author bnasslahsen
 */
class SpringDocMojoTest {

	private static final String SPEC = "{\"openapi\":\"3.1.0\",\"info\":{\"title\":\"Résumé\"}}";

	@TempDir
	Path outputDir;

	private HttpServer server;

	private ExecutorService executor;

	private String receivedAuthorization;

	private final RecordingProjectHelper projectHelper = new RecordingProjectHelper();

	@BeforeEach
	void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/v3/api-docs", exchange -> {
			receivedAuthorization = exchange.getRequestHeaders().getFirst("Authorization");
			byte[] body = SPEC.getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(body);
			}
		});
		server.createContext("/slow", exchange -> {
			try {
				Thread.sleep(5000);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			exchange.sendResponseHeaders(200, -1);
			exchange.close();
		});
		executor = Executors.newCachedThreadPool();
		server.setExecutor(executor);
		server.start();
	}

	@AfterEach
	void stopServer() {
		server.stop(0);
		executor.shutdownNow();
	}

	@Test
	void writesTheDescriptionAndSendsHeaders() throws Exception {
		SpringDocMojo mojo = mojo("/v3/api-docs", "openapi.json");
		set(mojo, "headers", Collections.singletonMap("Authorization", "Bearer token"));

		mojo.execute();

		assertEquals(SPEC, new String(Files.readAllBytes(outputDir.resolve("openapi.json")), StandardCharsets.UTF_8));
		assertEquals("Bearer token", receivedAuthorization);
		assertTrue(projectHelper.attached.isEmpty());
	}

	@Test
	void createsTheOutputDirectory() throws Exception {
		SpringDocMojo mojo = mojo("/v3/api-docs", "openapi.json");
		set(mojo, "outputDir", outputDir.resolve("a/b").toFile());

		mojo.execute();

		assertTrue(Files.isRegularFile(outputDir.resolve("a/b/openapi.json")));
	}

	@Test
	void failsOnceWithTheCauseWhenFailOnError() throws Exception {
		SpringDocMojo mojo = mojo("/missing", "openapi.json");
		set(mojo, "failOnError", true);

		MojoFailureException e = assertThrows(MojoFailureException.class, mojo::execute);

		assertEquals("Unable to generate the OpenAPI description from " + url("/missing")
				+ ": HTTP 404 from " + url("/missing"), e.getMessage());
		assertTrue(e.getCause() instanceof IOException);
	}

	@Test
	void onlyLogsWithoutFailOnError() throws Exception {
		SpringDocMojo mojo = mojo("/missing", "openapi.json");

		mojo.execute();

		assertFalse(Files.exists(outputDir.resolve("openapi.json")));
	}

	@Test
	void timesOut() throws Exception {
		SpringDocMojo mojo = mojo("/slow", "openapi.json");
		set(mojo, "timeout", 1);
		set(mojo, "failOnError", true);

		long start = System.nanoTime();
		MojoFailureException e = assertThrows(MojoFailureException.class, mojo::execute);

		assertTrue(e.getMessage().contains("timed out"), e.getMessage());
		assertTrue(System.nanoTime() - start < 4_000_000_000L);
	}

	@Test
	void skips() throws Exception {
		SpringDocMojo mojo = mojo("/v3/api-docs", "openapi.json");
		set(mojo, "skip", true);

		mojo.execute();

		assertFalse(Files.exists(outputDir.resolve("openapi.json")));
	}

	@Test
	void attachesWithTheClassifierAndTheFileExtension() throws Exception {
		SpringDocMojo mojo = mojo("/v3/api-docs", "openapi-admin.yaml");
		set(mojo, "attachArtifact", true);
		set(mojo, "attachArtifactClassifier", "openapi-admin");

		mojo.execute();

		assertEquals(Collections.singletonList("yaml:openapi-admin:openapi-admin.yaml"), projectHelper.attached);
	}

	@Test
	void attachesAsJsonWithoutFileExtension() throws Exception {
		SpringDocMojo mojo = mojo("/v3/api-docs", "openapi");
		set(mojo, "attachArtifact", true);

		mojo.execute();

		assertEquals(Collections.singletonList("json:openapi:openapi"), projectHelper.attached);
	}

	private SpringDocMojo mojo(String path, String outputFileName) throws Exception {
		SpringDocMojo mojo = new SpringDocMojo(projectHelper);
		set(mojo, "apiDocsUrl", url(path));
		set(mojo, "outputFileName", outputFileName);
		set(mojo, "outputDir", outputDir.toFile());
		set(mojo, "attachArtifactClassifier", "openapi");
		set(mojo, "timeout", 60);
		set(mojo, "project", new MavenProject());
		return mojo;
	}

	private String url(String path) {
		return "http://localhost:" + server.getAddress().getPort() + path;
	}

	static void set(Object target, String name, Object value) throws ReflectiveOperationException {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	/**
	 * Records the artifacts attached to the project as {@code type:classifier:fileName}.
	 */
	private static class RecordingProjectHelper implements MavenProjectHelper {

		private final List<String> attached = new ArrayList<>();

		@Override
		public void attachArtifact(MavenProject project, String artifactType, String artifactClassifier, File artifactFile) {
			attached.add(artifactType + ":" + artifactClassifier + ":" + artifactFile.getName());
		}

		@Override
		public void attachArtifact(MavenProject project, File artifactFile, String artifactClassifier) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void attachArtifact(MavenProject project, String artifactType, File artifactFile) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addResource(MavenProject project, String resourceDirectory, List<String> includes, List<String> excludes) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addTestResource(MavenProject project, String resourceDirectory, List<String> includes, List<String> excludes) {
			throw new UnsupportedOperationException();
		}

	}

}
