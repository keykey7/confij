package ch.kk7.confij.source.file.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

import static ch.kk7.confij.source.file.resource.ResourceFetchingException.unableToFetch;

public class URLResource extends AbstractResource {
	@Override
	public String read(URI path) {
		try {
			return read(path.toURL());
		} catch (MalformedURLException e) {
			throw unableToFetch(path.toString(), "not a valid URL", e);
		}
	}

	String read(URL url) {
		try (InputStream inputStream = Objects.requireNonNull(url, "null URL")
				.openStream()) {
			return new Scanner(inputStream, getCharset().name()).useDelimiter("\\A")
					.next();
		} catch (IOException e) {
			throw unableToFetch(url.toString(), "cannot read input stream", e);
		}
	}

	@Override
	public boolean canHandle(URI path) {
		try {
			//noinspection ResultOfMethodCallIgnored
			path.toURL();
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}
}
