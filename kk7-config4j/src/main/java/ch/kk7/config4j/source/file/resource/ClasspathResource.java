package ch.kk7.config4j.source.file.resource;

import java.net.URL;

import static ch.kk7.config4j.source.file.resource.ResourceFetchingException.unableToFetch;

public class ClasspathResource extends URLResource {
	@Override
	public String read(String path) {
		URL classpathUrl = ClassLoader.getSystemResource(path);
		if (classpathUrl == null) {
			throw unableToFetch("no such file on system classpath", path);
		}
		return read(classpathUrl);
	}
}
