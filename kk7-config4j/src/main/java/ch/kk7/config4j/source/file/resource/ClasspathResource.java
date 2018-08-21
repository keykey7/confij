package ch.kk7.config4j.source.file.resource;

import java.net.URI;
import java.net.URL;

public class ClasspathResource extends URLResource {
	public static final String SCHEME = "classpath";

	@Override
	public String read(URI path) {
		URL classpathUrl = ClassLoader.getSystemResource(path.getSchemeSpecificPart());
		if (classpathUrl == null) {
			// TODO: print suggestions of alternative resources (on same path, or with same name, or / instead of dot...)
			throw ResourceFetchingException.unableToFetch(path.getSchemeSpecificPart(), "no such file on system classpath");
		}
		return read(classpathUrl);
	}

	@Override
	public boolean canHandle(URI path) {
		return SCHEME.equals(path.getScheme());
	}
}
