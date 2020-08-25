package ch.kk7.confij.source.resource;

import com.google.auto.service.AutoService;
import lombok.ToString;

import java.net.URI;
import java.net.URL;
import java.util.stream.Stream;

import static ch.kk7.confij.source.resource.ConfijSourceFetchingException.unableToFetch;

@ToString
@AutoService(ConfijResourceProvider.class)
public class ClasspathResourceProvider extends URLResourceProvider {
	public static final String SCHEME = "classpath";

	@Override
	public Stream<String> read(URI path) {
		URL classpathUrl = ClassLoader.getSystemResource(path.getSchemeSpecificPart());
		if (classpathUrl == null) {
			// TODO: print suggestions of alternative resources (on same path, or with same name, or / instead of dot...)
			throw unableToFetch(path.getSchemeSpecificPart(), "no such file on system classpath");
		}
		return Stream.of(read(classpathUrl));
	}

	@Override
	public boolean canHandle(URI path) {
		return SCHEME.equals(path.getScheme());
	}
}
