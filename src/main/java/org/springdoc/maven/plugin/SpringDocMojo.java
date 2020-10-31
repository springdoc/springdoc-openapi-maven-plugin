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
 * Goal which touches a timestamp file.
 *
 */

@Mojo(name = "generate", requiresProject = true, defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class SpringDocMojo extends AbstractMojo {

	@Parameter(defaultValue = "http://localhost:8080/v3/api-docs", property = "apiDocsUrl", required = true)
	private String apiDocsUrl;

	@Parameter(defaultValue = "openapi.json", property = "outputFileName", required = true)
	private String outputFileName;

	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDir;

	/**
	 * Attach generated documentation as artifact to the Maven project.
	 * If true documentation will be deployed along with other artifacts.
	 */
	@Parameter(defaultValue = "false", property = "attachArtifact")
	private boolean attachArtifact;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	/**
	 * Skip execution if set to true. Default is false.
	 */
	@Parameter(defaultValue = "false", property = "skip")
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
			String result = null;
			if (responseCode == HttpURLConnection.HTTP_OK) {
				result = this.readFullyAsString(conection.getInputStream());
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
		int length = 0;
		while ((length = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return baos;
	}

	private void addArtifactToMaven() {
		File swaggerFile = new File(outputDir.getAbsolutePath() + '/' + outputFileName);
		projectHelper.attachArtifact(project, "json", "openapi", swaggerFile);
	}
}
