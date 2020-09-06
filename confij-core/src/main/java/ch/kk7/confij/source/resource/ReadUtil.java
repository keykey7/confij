package ch.kk7.confij.source.resource;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static ch.kk7.confij.source.resource.ConfijSourceFetchingException.unableToFetch;

@UtilityClass
public class ReadUtil {
	public Charset STANDARD_CHARSET = StandardCharsets.UTF_8;

	String readUrl(String urlStr, String charsetStr) {
		final URL url;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + urlStr, e);
		}
		final Charset charset = Charset.forName(charsetStr);
		return readUrl(url, charset);
	}

	String readUrl(URL url, Charset charset) {
		try (InputStream inputStream = url.openStream()) {
			Scanner s = new Scanner(inputStream, charset.name()).useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		} catch (IOException e) {
			throw unableToFetch(url.toString(), "cannot read", e);
		}
	}
}
