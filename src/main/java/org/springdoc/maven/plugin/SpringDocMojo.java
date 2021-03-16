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

import org.apache.maven.plugin.AbstractMojo;
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
 */
@Mojo(name = "generate", requiresProject = true, defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class SpringDocMojo extends AbstractMojo {

	/**
	 * The URL from where the api doc is retrieved.
	 */
	@Parameter(defaultValue = "http://localhost:8080/v3/api-docs", property = "springdoc.apiDocsUrl", required = true)
	private String apiDocsUrl;

	/**
	 * File name of the generated api doc.
	 */
	@Parameter(defaultValue = "openapi.json", property = "springdoc.outputFileName", required = true)
	private String outputFileName;

	/**
	 * Output directory for the generated api doc.
	 */
	@Parameter(defaultValue = "${project.build.directory}", property = "springdoc.outputDir", required = true)
	private File outputDir;

	/**
	 * Output file type of the generated api doc (yaml or json).
	 */
	@Parameter(defaultValue = "json", property = "springdoc.outputFileType" , required = true)
	private String outputFileType;

	/**
	 * Attach generated documentation as artifact to the Maven project.
	 * If true documentation will be deployed along with other artifacts.
	 */
	@Parameter(defaultValue = "false", property = "springdoc.attachArtifact")
	private boolean attachArtifact;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	/**
	 * Skip execution if set to true. Default is false.
	 */
	@Parameter(defaultValue = "false", property = "springdoc.skip")
	private boolean skip;

	@Component
	private MavenProjectHelper projectHelper;

	private static final String GET = "GET";

	public void execute() {
		if (skip) {
			getLog().info("Skip execution as per configuration");
			return;
		}
		try {
			URL urlForGetRequest = new URL(apiDocsUrl);
			HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
			conection.setRequestMethod(GET);
			int responseCode = conection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String result = this.readFullyAsString(conection.getInputStream());
				outputDir.mkdirs();
				Files.write(Paths.get(outputDir.getAbsolutePath() + "/" + outputFileName), result.getBytes(StandardCharsets.UTF_8));
				if (attachArtifact) addArtifactToMaven();
			} else {
				getLog().error("An error has occured: Response code " + responseCode);
			}
		} catch (Exception e) {
			getLog().error("An error has occured", e);
		}
	}


	private String readFullyAsString(InputStream inputStream) throws IOException {
		return readFully(inputStream).toString(StandardCharsets.UTF_8.name());
	}

	private ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return baos;
	}

	private void addArtifactToMaven() {
		File swaggerFile = new File(outputDir.getAbsolutePath() + '/' + outputFileName);
		projectHelper.attachArtifact(project, outputFileType, "openapi", swaggerFile);
	}
}
