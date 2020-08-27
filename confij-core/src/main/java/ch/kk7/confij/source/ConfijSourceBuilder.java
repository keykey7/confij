package ch.kk7.confij.source;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public interface ConfijSourceBuilder {
	Optional<ConfijSource> fromURI(URIish path);

	@Value
	@NonFinal
	class URIish {
		String scheme;

		@NonNull String schemeSpecificPart;

		String fragment;

		public static URIish create(@NonNull String uri) {
			final String scheme;
			String path;
			final String fragment;
			String[] schemeParts = uri.split(":", 2);
			if (schemeParts.length == 1) {
				scheme = null;
				path = schemeParts[0];
			} else {
				scheme = schemeParts[0];
				path = schemeParts[1];
			}
			String[] pathParts = path.split("#", 2);
			if (pathParts.length == 1) {
				fragment = null;
			} else {
				path = pathParts[0];
				fragment = pathParts[1];
			}
			return new URIish(scheme, path, fragment);
		}

		public URL toURL() throws MalformedURLException {
			try {
				return new URI(scheme, schemeSpecificPart, fragment).toURL();
			} catch (URISyntaxException e) {
				throw new MalformedURLException(e.getMessage());
			}
		}
	}
}
