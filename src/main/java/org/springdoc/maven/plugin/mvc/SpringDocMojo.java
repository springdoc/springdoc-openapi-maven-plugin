package org.springdoc.maven.plugin.mvc;

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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

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

	private static final String GET = "GET";

	public void execute() {
		try {
			URL urlForGetRequest = new URL(apiDocsUrl);
			HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
			conection.setRequestMethod(GET);
			int responseCode = conection.getResponseCode();
			String result = null;
			if (responseCode == HttpURLConnection.HTTP_OK) {
				result = this.readFullyAsString(conection.getInputStream());
			} else {
				getLog().error("An error has occured: Response code " + responseCode);
			}
			Files.write(Paths.get(outputDir.getAbsolutePath() + "/" + outputFileName),
					result.getBytes());
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
}
