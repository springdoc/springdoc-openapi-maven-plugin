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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;


@Mojo(name = "generate", requiresProject = true, defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class SpringDocMojo extends AbstractMojo {

	private static final String GET = "GET";

	private static final java.util.regex.Pattern yamlPattern = Pattern.compile(".ya?ml");

	@Parameter(defaultValue = "http://localhost:8080/v3/api-docs", property = "apiDocsUrl", required = true)
	private String apiDocsUrl;

	@Parameter(defaultValue = "openapi.json", property = "outputFileName", required = true)
	private String outputFileName;

	/**
	 * Determine file format of API document
	 * Expected json or yaml
	 */
	@Parameter(defaultValue = "json", property = "format", required = true)
	private String format;

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

	@Component
	private MavenProjectHelper projectHelper;

	public void execute() {
		validateSettings();
		final String result = getApiFromUrl();

		if (StringUtils.isNotEmpty(result))
			generateOutputFile(result);

		if (attachArtifact)
			addArtifactToMaven();
	}

	void validateSettings() {
		apiDocsUrl = getValidatedApiDocsUrl();
		outputFileName = getValidatedOutputFileName();
		format = getValidatedFormat();
	}

	void generateOutputFile(String result) {
		outputDir.mkdirs();
		writeFile(getValidatedOutputFileName(), prettyPrint(result));
	}

	String prettyPrint(String result) {
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if (StringUtils.equalsIgnoreCase(format, "json")) {
			final JsonElement jsonElement = new JsonParser().parse(result);
			return gson.toJson(jsonElement);
		}

		return result;
	}

	void writeFile(String outputFileName, String result) {
		final String outputDirPath = outputDir.getAbsolutePath() + "/";
		final byte[] resultBytes = result.getBytes(StandardCharsets.UTF_8);

		try {
			Files.write(
					Paths.get(outputDirPath + "/" + outputFileName), resultBytes);
		}
		catch (IOException e) {
			getLog().error("An error generating file has occurred", e);
		}

	}

	String getValidatedFormat() {
		format = StringUtils.lowerCase(format);
		switch (format) {
			case "yaml":
			case "json":
				return format;
			default:
				return "json";
		}
	}

	String getValidatedOutputFileName() {
		final Matcher fileMatcher = yamlPattern.matcher(outputFileName);
		if (format.equalsIgnoreCase("yaml")) {
			if (!fileMatcher.matches())
				return StringUtils.replace(outputFileName, ".json", ".yaml");
		}

		return outputFileName;
	}

	String getValidatedApiDocsUrl() {
		final Matcher urlMatcher = yamlPattern.matcher(apiDocsUrl);
		if (format.equalsIgnoreCase("yaml")) {
			if (!urlMatcher.matches())
				return "http://localhost:8080/v3/api-docs.yaml";
		}

		return apiDocsUrl;
	}

	String getApiFromUrl() {
		try {
			final URL urlForGetRequest = new URL(apiDocsUrl);
			final HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
			conection.setRequestMethod(GET);
			final int responseCode = conection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK)
				return readFullyAsString(conection.getInputStream());
			else
				getLog().error("An error has occurred: Response code " + responseCode);
		}
		catch (IOException e) {
			getLog().error("An error has occurred", e);
		}

		return "";
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