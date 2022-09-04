package org.springdoc.maven.plugin;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * One export target is one entry that should be taken from the
 * openapi documentation.
 * <p>
 * Any amount of export targets can be defined. They provide a
 * simple {@link #path}, which should be fetched from and a {@link #outputFileName}
 * which the results of the url should be written to.
 */
public class ExportTarget {

	/**
	 * The url to fetch from.
	 */
	@Parameter(property = "path", required = true)
	private String path;

	/**
	 * File name of the generated api doc.
	 */
	@Parameter(property = "outputFileName", required = true)
	private String outputFileName;

	/**
	 * Creates a new ExportTarget.
	 * <p>
	 * This method bypasses the need to create two constructors,
	 * whilst keeping its fields private, without requiring constructors
	 *
	 * @param path           the path, relative to the url provided in the {@link SpringDocMojo#baseUrl}
	 * @param outputFileName the file, that should be created with the contents of the {@link #path}.
	 *                       This will be relative to the folder {@link SpringDocMojo#outputDir}
	 * @return a new ExportTarget
	 */
	public static ExportTarget build(String path, String outputFileName) {
		ExportTarget exportTarget = new ExportTarget();
		exportTarget.path = path;
		exportTarget.outputFileName = outputFileName;
		return exportTarget;
	}

	public static URL joinToUrl(String url, String... paths) {
		try {
			return new URL(joinToString(url, paths));
		} catch (MalformedURLException e) {
			throw new IllegalStateException("The provided url " + url + Arrays.toString(paths) + " is not a valid URL.", e);
		}
	}

	/**
	 * Joins the provided base and additions, separating entries using the "/"
	 * char, making sure that no slashes are doubled.
	 *
	 * @param base      the base of the string
	 * @param additions all additional paths
	 * @return a joined string, with no double slashes
	 */
	public static String joinToString(String base, String... additions) {
		StringBuilder result = new StringBuilder(base);
		if (result.toString().endsWith("/")) {
			result = new StringBuilder(result.substring(0, result.length() - 1));
		}

		for (String path : additions) {
			String currentPath = path;
			if (!currentPath.startsWith("/")) {
				currentPath = "/" + currentPath;
			}
			if (currentPath.endsWith("/")) {
				currentPath = currentPath.substring(0, result.length() - 1);
			}

			result.append(currentPath);
		}

		return result.toString();
	}

	/**
	 * Returns the set path
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}


	/**
	 * Opens a connection to the {@link #path}, relative to the provided
	 * baseUrl.
	 *
	 * @param baseUrl the base url
	 * @return an open HttpUrlConnection to the {@link #path}, relative to
	 * the baseUrl
	 */
	public HttpURLConnection openConnection(String baseUrl) {
		URL url = joinToUrl(baseUrl, path);
		try {
			return (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			throw new IllegalStateException("Connection to " + url + " could not be established.", e);
		}
	}

	/**
	 * This method creates a path to the {@link #outputFileName}, relative to
	 * the provided baseDir.
	 * <p>
	 * If the parent does not exist, it will be created, assuming that the
	 * created path will be filled later.
	 * <p>
	 * If the  {@link #outputFileName} is more complex than just a name (like
	 * for example "documentation/generated/openapi.yml", this approach will
	 * allow for any amount of sub folders.
	 *
	 * @param baseDir the base dir, under which the output file name should
	 *                be created.
	 * @return a Path to the output file
	 */
	public Path accessOutputFileFrom(String baseDir) {
		String totalPath = joinToString(baseDir, outputFileName);
		Path path = Paths.get(totalPath);

		path.toFile().getParentFile().mkdirs();
		return path;
	}
}
