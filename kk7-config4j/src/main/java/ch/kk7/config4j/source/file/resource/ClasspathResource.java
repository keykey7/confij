package ch.kk7.config4j.source.file.resource;

import java.net.URL;

import static ch.kk7.config4j.source.file.resource.ResourceFetchingException.unableToFetch;

public class ClasspathResource extends URLResource {
	public static final String SCHEME = "classpath";

	@Override
	public String read(String path) {
		URL classpathUrl = ClassLoader.getSystemResource(path);
		if (classpathUrl == null) {
			// TODO: print suggestions of alternative resources (on same path, or with same name, or / instead of dot...)
			throw unableToFetch(path, "no such file on system classpath");
		}
		return read(classpathUrl);
	}
}
