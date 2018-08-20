package ch.kk7.config4j.source.file.resource;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;

import static ch.kk7.config4j.source.file.resource.ResourceFetchingException.unableToFetch;

public class FileResource extends URLResource {
	public static final String SCHEME = "file";

	@Override
	public String read(String pathStr) {
		final File file;
		try {
			file = Paths.get(pathStr)
					.toFile();
		} catch (Exception e) {
			throw unableToFetch("not a valid path", pathStr);
		}
		if (!file.exists()) {
			throw unableToFetch("file does not exist", file.getAbsolutePath());
		}
		if (!file.isFile()) {
			throw unableToFetch("not a file", file.getAbsolutePath());
		}
		if (!file.canRead()) {
			throw unableToFetch("cannot read file", file.getAbsolutePath());
		}
		try {
			return read(file.toURI()
					.toURL());
		} catch (MalformedURLException e) {
			throw unableToFetch("not a valid URL", file.getAbsolutePath(), e);
		}
	}
}
