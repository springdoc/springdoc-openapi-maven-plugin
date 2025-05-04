package org.springdoc.maven.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

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
	private static final String DEFAULT_OUTPUT_FILE_NAME= "openapi";

	/**
	 * The DEFAULT OUTPUT EXTENSION.
	 */
	private static final String DEFAULT_OUTPUT_EXTENSION= ".json";

	/**
	 * The DEFAULT OUTPUT FILE.
	 */
	private static final String DEFAULT_OUTPUT_FILE = DEFAULT_OUTPUT_FILE_NAME+DEFAULT_OUTPUT_EXTENSION;

	/**
	 * The URL from where the api doc is retrieved.
	 */
	@Parameter(defaultValue = "http://localhost:8080/v3/api-docs", property = "springdoc.apiDocsUrl", required = true)
	private String apiDocsUrl;

	/**
	 * File name of the generated api doc.
	 */
	@Parameter(defaultValue = DEFAULT_OUTPUT_FILE, property = "springdoc.outputFileName", required = true)
	private String outputFileName;

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
	 * Fail build on error, if set to true. Default is false.
	 */
	@Parameter(defaultValue = "false", property = "springdoc.failOnError")
	private boolean failOnError;

	/**
	 * Headers to send in request
	 */
	@Parameter(property = "headers")
	private Map<String, String> headers;

	/**
	 * The Project helper.
	 */
	@Component
	private MavenProjectHelper projectHelper;

	/**
	 * The constant GET.
	 */
	private static final String GET = "GET";

	public void execute() throws MojoFailureException {
		if (skip) {
			getLog().info("Skip execution as per configuration");
			return;
		}
		try {
			URL urlForGetRequest = new URL(apiDocsUrl);
			HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
			if (headers.size() > 0) {headers.forEach((k, v) -> connection.setRequestProperty(k, v));}
			connection.setRequestMethod(GET);
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String result = this.readFullyAsString(connection.getInputStream());
				outputDir.mkdirs();
				Files.write(Paths.get(outputDir.getAbsolutePath() + "/" + outputFileName), result.getBytes(StandardCharsets.UTF_8));
				if (attachArtifact) addArtifactToMaven();
			} else {
				String message = "An error has occurred, response code: " + responseCode;
				if(failOnError) {
					throw new MojoFailureException(message);
				} else {
					getLog().error(message);
				}
			}
		} catch (Exception e) {
			getLog().error("An error has occurred", e);
			if(failOnError) {
				throw new MojoFailureException("An error has occurred: " + e.getMessage());
			}
		}
	}


	/**
	 * Read fully as string string.
	 *
	 * @param inputStream the input stream
	 * @return the string
	 * @throws IOException the io exception
	 */
	private String readFullyAsString(InputStream inputStream) throws IOException {
		return readFully(inputStream).toString(StandardCharsets.UTF_8.name());
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
	private void addArtifactToMaven() {
		File swaggerFile = new File(outputDir.getAbsolutePath() + '/' + outputFileName);
		String extension = getFileExtension();
		projectHelper.attachArtifact(project, extension, DEFAULT_OUTPUT_FILE_NAME, swaggerFile);
	}

	/**
	 * Gets file extension.
	 *
	 * @return the file extension
	 */
	private String getFileExtension() {
		String extension = DEFAULT_OUTPUT_EXTENSION;
		int i = outputFileName.lastIndexOf('.');
		if (i > 0)
			extension = outputFileName.substring(i + 1);
		return extension;
	}
}
