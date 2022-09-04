package org.springdoc.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Generate a openapi specification file.
 *
 * @author bnasslashen
 */
@Mojo(name = "generate", requiresProject = true, defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class SpringDocMojo extends AbstractMojo {

	/**
	 * The DEFAULT OUTPUT FILE NAME.
	 */
	private static final String DEFAULT_OUTPUT_FILE_NAME = "openapi";

	/**
	 * The DEFAULT OUTPUT EXTENSION.
	 */
	private static final String DEFAULT_OUTPUT_EXTENSION = ".json";

	/**
	 * The constant GET.
	 */
	private static final String GET = "GET";

	/**
	 * Output directory for the generated api doc.
	 */
	@Parameter(defaultValue = "${project.build.directory}", property = "springdoc.outputDir", required = true)
	private File outputDir;

	/**
	 * Attach generated documentation as artifact to the Maven project.
	 * If true documentation will be deployed along with other artifacts.
	 */
	@Parameter(defaultValue = "false", property = "springdoc.attachArtifact")
	private boolean attachArtifact;

	/**
	 * The Project.
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	/**
	 * Skip execution if set to true. Default is false.
	 */
	@Parameter(defaultValue = "false", property = "springdoc.skip")
	private boolean skip;

	/**
	 * Headers to send in request
	 */
	@Parameter(property = "headers")
	private Map<String, String> headers;

	/**
	 * All files, that should be exported from the documentation.
	 * <p>
	 * These are individual exports. Allowing you to define as many
	 * or as little files to be exported from the urls as you like
	 */
	@Parameter(property = "exports", required = true)
	private List<ExportTarget> exports;

	/**
	 * The base url, which all exports are relative to.
	 * <p>
	 * By default, it will be local host on port 8080, because this is the default value of spring.
	 */
	@Parameter(property = "baseUrl", defaultValue = "http://localhost:8080/", required = true)
	private String baseUrl;

	/**
	 * The Project helper.
	 */
	@Component
	private MavenProjectHelper projectHelper;

	public void execute() {
		if (skip) {
			getLog().info("Skip execution as per configuration");
			return;
		}
		try {
			exports.forEach(this::doExecute);
		} catch (IllegalStateException e) {
			getLog().error("An error has occurred: " + e.getMessage(), e.getCause());
		}
	}

	private void doExecute(ExportTarget container) throws IllegalStateException {
		Path targetPath = container.accessOutputFileFrom(outputDir.getAbsolutePath());
		HttpURLConnection connection = container.openConnection(baseUrl);
		getLog().info("Fetching " + container.getPath() + " as " + targetPath);

		if (headers.size() > 0) {
			headers.forEach(connection::setRequestProperty);
		}
		int responseCode;
		try {
			connection.setRequestMethod(GET);
			responseCode = connection.getResponseCode();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		if (responseCode == HttpURLConnection.HTTP_OK) {
			String result = this.readFullyAsString(connection);
			try {
				Files.write(targetPath, result.getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				throw new IllegalStateException("Error writing " + targetPath, e);
			}
			if (attachArtifact) addArtifactToMaven(targetPath);
		} else {
			getLog().error("An error has occurred: Response code " + responseCode);
		}
	}

	/**
	 * Read fully as string.
	 *
	 * @param connection the input stream
	 * @return the string
	 * @throws IOException the io exception
	 */
	private String readFullyAsString(HttpURLConnection connection) {
		try {
			return readFully(connection.getInputStream()).toString(StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			throw new IllegalStateException("Could not read from the connection", e);
		}
	}

	/**
	 * Read fully byte array output stream.
	 *
	 * @param inputStream the input stream
	 * @return the byte array output stream
	 * @throws IOException the io exception
	 */
	private ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return baos;
	}

	/**
	 * Add artifact to maven.
	 */
	private void addArtifactToMaven(Path outputFile) {
		String extension = getFileExtension(outputFile.getFileName().toString());
		projectHelper.attachArtifact(project, extension, DEFAULT_OUTPUT_FILE_NAME, outputFile.toFile());
	}

	/**
	 * Gets file extension.
	 *
	 * @return the file extension
	 */
	private String getFileExtension(String outputFileName) {
		String extension = DEFAULT_OUTPUT_EXTENSION;
		int i = outputFileName.lastIndexOf('.');
		if (i > 0)
			extension = outputFileName.substring(i + 1);
		return extension;
	}
}
