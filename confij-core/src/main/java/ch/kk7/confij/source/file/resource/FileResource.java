package ch.kk7.confij.source.file.resource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Paths;

import static ch.kk7.confij.source.file.resource.ResourceFetchingException.unableToFetch;

public class FileResource extends URLResource {
	public static final String SCHEME = "file";

	@Override
	public String read(URI maybeFileUri) {
		String path = maybeFileUri.getSchemeSpecificPart();
		final File file;
		try {
			file = Paths.get(path)
					.toFile();
		} catch (Exception e) {
			throw unableToFetch(path, "not a valid path");
		}
		if (!file.exists()) {
			throw unableToFetch(file.getAbsolutePath(), "file does not exist");
		}
		if (!file.isFile()) {
			throw unableToFetch(file.getAbsolutePath(), "not a file");
		}
		if (!file.canRead()) {
			throw unableToFetch(file.getAbsolutePath(), "cannot read file");
		}
		try {
			return read(file.toURI()
					.toURL());
		} catch (MalformedURLException e) {
			throw unableToFetch(file.getAbsolutePath(), "not a valid URL", e);
		}
	}

	@Override
	public boolean canHandle(URI path) {
		return !path.isAbsolute() || SCHEME.equals(path.getScheme());
	}
}
