package org.springdoc.maven.plugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
	private static final String DEFAULT_OUTPUT_EXTENSION= "json";

	/**
	 * The DEFAULT OUTPUT FILE.
	 */
	private static final String DEFAULT_OUTPUT_FILE = DEFAULT_OUTPUT_FILE_NAME + "." + DEFAULT_OUTPUT_EXTENSION;

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
	 * Classifier of the attached artifact. Use a different one in each execution
	 * that attaches its file, otherwise each attached file replaces the previous one.
	 */
	@Parameter(defaultValue = DEFAULT_OUTPUT_FILE_NAME, property = "springdoc.attachArtifactClassifier")
	private String attachArtifactClassifier;

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
	 * Connect and read timeout of the request, in seconds. 0 means no timeout.
	 */
	@Parameter(defaultValue = "60", property = "springdoc.timeout")
	private int timeout;

	/**
	 * The Project helper.
	 */
	@Component
	private MavenProjectHelper projectHelper;

	public void execute() throws MojoFailureException {
		if (skip) {
			getLog().info("Skip execution as per configuration");
			return;
		}
		File outputFile = new File(outputDir, outputFileName);
		try {
			String result = ApiDocsClient.fetch(apiDocsUrl, headers, timeout);
			Files.createDirectories(outputDir.toPath());
			Files.write(outputFile.toPath(), result.getBytes(StandardCharsets.UTF_8));
		}
		catch (Exception e) {
			String message = "Unable to generate the OpenAPI description from " + apiDocsUrl + ": " + e.getMessage();
			if (failOnError) {
				throw new MojoFailureException(message, e);
			}
			getLog().error(message);
			getLog().debug(e);
			return;
		}
		if (attachArtifact) {
			addArtifactToMaven(outputFile);
		}
	}

	/**
	 * Add artifact to maven.
	 *
	 * @param outputFile the generated file
	 */
	private void addArtifactToMaven(File outputFile) {
		String extension = getFileExtension();
		projectHelper.attachArtifact(project, extension, attachArtifactClassifier, outputFile);
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
