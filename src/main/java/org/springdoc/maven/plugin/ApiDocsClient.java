/*
 * Copyright 2019-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springdoc.maven.plugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Downloads the OpenAPI description from the running application.
 *
 * @author bnasslahsen
 */
final class ApiDocsClient {

	/**
	 * The constant GET.
	 */
	private static final String GET = "GET";

	/**
	 * Instantiates a new Api docs client.
	 */
	private ApiDocsClient() {
	}

	/**
	 * Fetches the OpenAPI description.
	 *
	 * @param apiDocsUrl the URL of the OpenAPI description
	 * @param headers the HTTP headers to send, may be null
	 * @param timeoutSeconds the connect and read timeout in seconds, 0 for no timeout
	 * @return the response body
	 * @throws IOException if the request fails or the response code is not 200
	 */
	static String fetch(String apiDocsUrl, Map<String, String> headers, int timeoutSeconds) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(apiDocsUrl).openConnection();
		try {
			int timeoutMillis = (int) TimeUnit.SECONDS.toMillis(timeoutSeconds);
			connection.setConnectTimeout(timeoutMillis);
			connection.setReadTimeout(timeoutMillis);
			connection.setRequestMethod(GET);
			if (headers != null) {
				headers.forEach(connection::setRequestProperty);
			}
			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new IOException("HTTP " + responseCode + " from " + apiDocsUrl);
			}
			try (InputStream inputStream = connection.getInputStream()) {
				return readFully(inputStream);
			}
		}
		finally {
			connection.disconnect();
		}
	}

	/**
	 * Reads the input stream fully as a UTF-8 string.
	 *
	 * @param inputStream the input stream
	 * @return the string
	 * @throws IOException the io exception
	 */
	private static String readFully(InputStream inputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return baos.toString(StandardCharsets.UTF_8.name());
	}

}
