package ch.kk7.confij.source.resource;

import com.google.auto.service.AutoService;
import lombok.ToString;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Paths;

import static ch.kk7.confij.source.resource.ConfijSourceFetchingException.unableToFetch;

@ToString
@AutoService(ConfijResourceProvider.class)
public class FileResourceProvider extends URLResourceProvider {
	public static final String SCHEME = "file";

	@Override
	public String read(URI fileUri) {
		String path = fileUri.getSchemeSpecificPart();
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
