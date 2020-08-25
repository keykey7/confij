package ch.kk7.confij.source.resource;

import com.google.auto.service.AutoService;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import java.util.stream.Stream;

import static ch.kk7.confij.source.resource.ConfijSourceFetchingException.unableToFetch;

@ToString
@AutoService(ConfijResourceProvider.class)
public class URLResourceProvider extends AbstractResourceProvider {
	@Override
	public Stream<String> read(URI path) {
		try {
			return Stream.of(read(path.toURL()));
		} catch (MalformedURLException e) {
			throw unableToFetch(path.toString(), "not a valid URL", e);
		}
	}

	String read(URL url) {
		try (InputStream inputStream = url.openStream()) {
			return new Scanner(inputStream, getCharset().name()).useDelimiter("\\A")
					.next();
		} catch (IOException e) {
			throw unableToFetch(url.toString(), "cannot read", e);
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
