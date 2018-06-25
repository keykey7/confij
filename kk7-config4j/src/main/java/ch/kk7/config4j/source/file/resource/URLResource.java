package ch.kk7.config4j.source.file.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

import static ch.kk7.config4j.source.file.resource.ResourceFetchingException.unableToFetch;

public class URLResource extends AbstractResource {
	@Override
	public String read(String path) {
		try {
			return read(new URL(path));
		} catch (MalformedURLException e) {
			throw unableToFetch("not a valid URL", path, e);
		}
	}

	String read(URL url) {
		try (InputStream inputStream = Objects.requireNonNull(url, "null URL")
				.openStream()) {
			return new Scanner(inputStream, getCharset().name()).useDelimiter("\\A")
					.next();
		} catch (IOException e) {
			throw unableToFetch("cannot read input stream", url.toString(), e);
		}
	}
}
