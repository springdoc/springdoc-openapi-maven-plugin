package org.springdoc.maven.plugin;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ExportTarget {

	/**
	 * The url to fetch from.
	 */
	@Parameter(property = "url", required = true)
	private String url;

	/**
	 * File name of the generated api doc.
	 */
	@Parameter(property = "outputFileName", required = true)
	private String outputFileName;

	public static ExportTarget build(String url, String outputFileName) {
		ExportTarget exportTarget = new ExportTarget();
		exportTarget.url = url;
		exportTarget.outputFileName = outputFileName;
		return exportTarget;
	}

	public String getUrl() {
		return url;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public HttpURLConnection openConnection() {
		try {
			URL url = new URL(getUrl());
			try {
				return (HttpURLConnection) url.openConnection();
			} catch (IOException e) {
				throw new IllegalStateException("Connection to " + getUrl() + " could not be established.", e);
			}
		} catch (MalformedURLException e) {
			throw new IllegalStateException("The provided url " + getUrl() + " is not a valid URL.", e);
		}
	}
}
