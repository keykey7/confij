package ch.kk7.confij.source.resource;

import ch.kk7.confij.logging.ConfijLogger;
import com.google.auto.service.AutoService;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.kk7.confij.source.resource.ConfijSourceFetchingException.unableToFetch;

@ToString
@AutoService(ConfijResourceProvider.class)
public class FileResourceProvider extends URLResourceProvider {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(FileResourceProvider.class);

	public static final String SCHEME = "file";

	int maxFileMatches = 100;

	int warnFilesTraversed = 1000;

	Pattern globPattern = Pattern.compile("(^|.*[^\\\\])([*?]|\\[.+]|\\{.+}).*");

	@SneakyThrows
	protected List<Path> getFilesMatching(@NonNull Path basePath, @NonNull PathMatcher pathMatcher, String originalPath) {
		List<Path> matchingFiles = new ArrayList<>();
		final int[] fileCounter = new int[]{0};
		Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				if (pathMatcher.matches(file)) {
					if (matchingFiles.size() >= maxFileMatches) {
						throw new ConfijSourceFetchingException("found too many files (>={}) matching glob expression. " +
								"your expression '{}' seems too lax.", maxFileMatches, originalPath);
					}
					matchingFiles.add(file);
					fileCounter[0]++;
				}
				return FileVisitResult.CONTINUE;
			}
		});
		if (fileCounter[0] >= warnFilesTraversed) {
			LOGGER.info("traversed {} files to get {} files matching glob {}: that was an expensive operation", fileCounter[0],
					matchingFiles.size(), originalPath);
		}
		Collections.sort(matchingFiles);
		return matchingFiles;
	}

	@Value
	@NonFinal
	private static class PathAndMatcher {
		Path basePath;
		PathMatcher pathMatcher;
	}

	protected PathAndMatcher extractGlob(String path) {
		String[] parts = path.split("/", -1);
		int globAt = -1;
		for (int i = 0; i < parts.length; i++) {
			if (globPattern.matcher(parts[i])
					.matches()) {
				globAt = i;
				break;
			}
		}
		if (globAt == -1) {
			return new PathAndMatcher(Paths.get(path), null);
		}
		String beforeGlob = Stream.of(parts)
				.limit(globAt)
				.collect(Collectors.joining("/"));
		String globExpression = Stream.of(parts)
				.skip(globAt)
				.collect(Collectors.joining("/"));
		// TODO: support other pathMatchers like regex
		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + globExpression);
		return new PathAndMatcher(Paths.get(beforeGlob), pathMatcher);
	}

	protected String read(Path path) {
		URL url;
		try {
			url = path.toUri()
					.toURL();
		} catch (MalformedURLException e) {
			throw unableToFetch(path.toAbsolutePath()
					.toString(), "not a valid URL", e);
		}
		return read(url);
	}

	@Override
	public Stream<String> read(URI fileUri) {
		String path = fileUri.getSchemeSpecificPart();
		PathAndMatcher query = extractGlob(path);

		List<Path> matchingFiles;
		if (query.getPathMatcher() == null) { // a file must exist if not a glob
			matchingFiles = Collections.singletonList(query.getBasePath());
		} else {
			matchingFiles = getFilesMatching(query.getBasePath(), query.getPathMatcher(), path);
		}
		return matchingFiles.stream()
				.map(this::read);
	}

	@Override
	public boolean canHandle(URI path) {
		return !path.isAbsolute() || SCHEME.equals(path.getScheme());
	}
}
