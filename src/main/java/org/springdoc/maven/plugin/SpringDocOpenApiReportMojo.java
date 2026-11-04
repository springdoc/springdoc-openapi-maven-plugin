package org.springdoc.maven.plugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;

/**
 * Generates a static Swagger UI page from an OpenAPI specification
 * and integrates it into the Maven project site under Project Reports.
 *
 * <p>The spec is loaded from a file produced by the {@code generate} goal
 * (default {@code ${project.build.directory}/openapi.json}).
 * If the file does not exist, the mojo falls back to fetching the spec
 * from {@code apiDocsUrl} (requires the application to be running).</p>
 *
 * <p>Usage in a consumer POM:</p>
 * <pre>
 * &lt;reporting&gt;
 *   &lt;plugins&gt;
 *     &lt;plugin&gt;
 *       &lt;groupId&gt;org.springdoc&lt;/groupId&gt;
 *       &lt;artifactId&gt;springdoc-openapi-maven-plugin&lt;/artifactId&gt;
 *       &lt;reportSets&gt;
 *         &lt;reportSet&gt;
 *           &lt;reports&gt;
 *             &lt;report&gt;report&lt;/report&gt;
 *           &lt;/reports&gt;
 *         &lt;/reportSet&gt;
 *       &lt;/reportSets&gt;
 *     &lt;/plugin&gt;
 *   &lt;/plugins&gt;
 * &lt;/reporting&gt;
 * </pre>
 *
 * @author springdoc
 */
@Mojo(name = "report", defaultPhase = LifecyclePhase.SITE, threadSafe = true)
public class SpringDocOpenApiReportMojo extends AbstractMavenReport {

	private static final String GET = "GET";

	/**
	 * Skip report generation.
	 */
	@Parameter(defaultValue = "false", property = "springdoc.report.skip")
	private boolean skip;

	/**
	 * Path to a previously generated OpenAPI spec file.
	 * This is the primary source; if the file exists it is used directly.
	 * Typically produced by the {@code generate} goal.
	 */
	@Parameter(defaultValue = "${project.build.directory}/openapi.json", property = "springdoc.apiDocsFile")
	private File apiDocsFile;

	/**
	 * Fallback URL to fetch the OpenAPI spec from when the file does not exist.
	 * The application must be running at this URL for the fallback to succeed.
	 */
	@Parameter(defaultValue = "http://localhost:8080/v3/api-docs", property = "springdoc.apiDocsUrl", required = true)
	private String apiDocsUrl;

	/**
	 * HTTP headers sent when fetching the spec from {@code apiDocsUrl}.
	 */
	@Parameter(property = "headers")
	private Map<String, String> headers;

	/**
	 * Name shown in the site's Project Reports navigation.
	 */
	@Parameter(property = "springdoc.report.name", defaultValue = "OpenAPI Documentation")
	private String siteReportName;

	/**
	 * Description shown in the site's Project Reports navigation.
	 */
	@Parameter(property = "springdoc.report.description",
			defaultValue = "OpenAPI specification rendered with Swagger UI")
	private String siteReportDescription;

	/**
	 * Subdirectory under the site output directory where
	 * the Swagger UI page and spec file are written.
	 */
	@Parameter(property = "springdoc.report.directory", defaultValue = "springdoc-openapi")
	private String siteReportDirectory;

	/**
	 * Swagger UI version tag used for CDN references (unpkg.com).
	 * Pinned by default for reproducible sites; {@code "5"} (latest 5.x) is also accepted.
	 */
	@Parameter(property = "springdoc.swaggerUiVersion", defaultValue = "5.34.1")
	private String swaggerUiVersion;

	@Override
	public String getOutputName() {
		return siteReportDirectory + "/index";
	}

	@Override
	public String getName(Locale locale) {
		return siteReportName;
	}

	@Override
	public String getDescription(Locale locale) {
		return siteReportDescription;
	}

	@Override
	public boolean canGenerateReport() {
		return !skip;
	}

	@Override
	public boolean isExternalReport() {
		return true;
	}

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		getLog().info("Generating OpenAPI site report");

		String specJson = loadSpecJson();
		// The spec is inlined as a JavaScript object literal, so only JSON is supported
		if (!specJson.trim().startsWith("{")) {
			throw new MavenReportException(
					"The OpenAPI spec is not JSON (YAML is not supported by the report goal). "
							+ "Point 'apiDocsFile' or 'apiDocsUrl' to a JSON spec, e.g. /v3/api-docs.");
		}

		File reportOutputDir = new File(
				getReportOutputDirectory(), siteReportDirectory);
		if (!reportOutputDir.mkdirs() && !reportOutputDir.isDirectory()) {
			throw new MavenReportException(
					"Could not create report output directory: " + reportOutputDir);
		}

		writeSwaggerUiPage(reportOutputDir, specJson);
		writeSpecFile(reportOutputDir, specJson);

		getLog().info("OpenAPI report generated at " + reportOutputDir.getAbsolutePath());
	}

	/**
	 * Loads the OpenAPI spec JSON from the configured file,
	 * falling back to the configured URL if the file does not exist.
	 */
	private String loadSpecJson() throws MavenReportException {
		if (apiDocsFile != null && apiDocsFile.isFile()) {
			getLog().info("Reading OpenAPI spec from " + apiDocsFile.getAbsolutePath());
			try {
				return new String(
						Files.readAllBytes(apiDocsFile.toPath()), StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new MavenReportException(
						"Failed to read OpenAPI spec from " + apiDocsFile, e);
			}
		}

		getLog().info("Spec file not found at " + apiDocsFile
				+ ", fetching from " + apiDocsUrl);
		try {
			return fetchFromUrl();
		} catch (IOException e) {
			throw new MavenReportException(
					"Could not load OpenAPI spec from file (" + apiDocsFile
							+ ") or URL (" + apiDocsUrl + "). "
							+ "Run the 'generate' goal first or ensure the application is running.",
					e);
		}
	}

	private String fetchFromUrl() throws IOException {
		URL url = new URL(apiDocsUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				connection.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		connection.setRequestMethod(GET);

		int responseCode = connection.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			throw new IOException("HTTP " + responseCode + " from " + apiDocsUrl);
		}

		try (InputStream is = connection.getInputStream()) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int len;
			while ((len = is.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			return baos.toString(StandardCharsets.UTF_8.name());
		}
	}

	private void writeSwaggerUiPage(File outputDir, String specJson)
			throws MavenReportException {
		String cdnBase = "https://unpkg.com/swagger-ui-dist@" + swaggerUiVersion;
		// Prevent </script> in JSON values from closing the script tag early
		String safeSpecJson = specJson.replace("</", "<\\/");

		StringBuilder html = new StringBuilder(safeSpecJson.length() + 1024);
		html.append("<!DOCTYPE html>\n");
		html.append("<html lang=\"en\">\n");
		html.append("<head>\n");
		html.append("  <meta charset=\"UTF-8\">\n");
		html.append("  <title>").append(escapeHtml(siteReportName)).append("</title>\n");
		html.append("  <link rel=\"stylesheet\" type=\"text/css\" href=\"")
				.append(cdnBase).append("/swagger-ui.css\">\n");
		html.append("  <style>\n");
		html.append("    html { box-sizing: border-box; overflow-y: scroll; }\n");
		html.append("    *, *:before, *:after { box-sizing: inherit; }\n");
		html.append("    body { margin: 0; background: #fafafa; }\n");
		html.append("  </style>\n");
		html.append("</head>\n");
		html.append("<body>\n");
		html.append("  <div id=\"swagger-ui\"></div>\n");
		html.append("  <script src=\"").append(cdnBase)
				.append("/swagger-ui-bundle.js\"></script>\n");
		html.append("  <script src=\"").append(cdnBase)
				.append("/swagger-ui-standalone-preset.js\"></script>\n");
		html.append("  <script>\n");
		html.append("    window.onload = function() {\n");
		html.append("      SwaggerUIBundle({\n");
		html.append("        spec: ").append(safeSpecJson).append(",\n");
		html.append("        dom_id: '#swagger-ui',\n");
		html.append("        deepLinking: true,\n");
		html.append("        presets: [\n");
		html.append("          SwaggerUIBundle.presets.apis,\n");
		html.append("          SwaggerUIStandalonePreset\n");
		html.append("        ],\n");
		html.append("        plugins: [\n");
		html.append("          SwaggerUIBundle.plugins.DownloadUrl\n");
		html.append("        ],\n");
		html.append("        layout: \"StandaloneLayout\"\n");
		html.append("      });\n");
		html.append("    }\n");
		html.append("  </script>\n");
		html.append("</body>\n");
		html.append("</html>\n");

		File indexFile = new File(outputDir, "index.html");
		try {
			Files.write(indexFile.toPath(),
					html.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new MavenReportException("Failed to write Swagger UI page", e);
		}
	}

	private void writeSpecFile(File outputDir, String specJson)
			throws MavenReportException {
		File specFile = new File(outputDir, "openapi.json");
		try {
			Files.write(specFile.toPath(),
					specJson.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new MavenReportException("Failed to write OpenAPI spec file", e);
		}
	}

	private static String escapeHtml(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}
}
