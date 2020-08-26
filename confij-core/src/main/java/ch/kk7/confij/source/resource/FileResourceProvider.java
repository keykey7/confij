package ch.kk7.confij.source.resource;

import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.source.ConfijSourceBuilder.URIish;
import com.google.auto.service.AutoService;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.NonFinal;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.kk7.confij.source.resource.ConfijSourceFetchingException.unableToFetch;

@ToString
@Setter
@FieldNameConstants
@AutoService(ConfijResourceProvider.class)
public class FileResourceProvider extends URLResourceProvider {
	public static final String SCHEME = "file";

	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(FileResourceProvider.class);

	int maxFileMatches = 50;

	int maxFilesTraversed = 10000;

	Pattern globPattern = Pattern.compile("(^|.*[^\\\\])([*?]|\\[.+]|\\{.+}).*");

	@Value
	@NonFinal
	protected static class PathAndMatcher {
		Path basePath;

		PathMatcher pathMatcher;

		String originalPath;

		int maxDepth;
	}

	@SneakyThrows
	protected List<Path> getFilesMatching(@NonNull PathAndMatcher query) {
		List<Path> matchingFiles = new ArrayList<>();
		final int[] fileCounter = new int[]{0};
		// TODO: configurable symlink follow
		Files.walkFileTree(query.getBasePath(), EnumSet.noneOf(FileVisitOption.class), query.getMaxDepth(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				if (query.getPathMatcher()
						.matches(file)) {
					if (matchingFiles.size() >= maxFileMatches) {
						throw new ConfijSourceFetchingException("found too many files (>={}) matching glob expression. " +
								"your expression '{}' seems too lax. if this was intentional, try to increase '{}'", maxFileMatches,
								query.getOriginalPath(), Fields.maxFileMatches);
					}
					if (++fileCounter[0] >= maxFilesTraversed) {
						throw new ConfijSourceFetchingException("traversed too many files (>={}) in query of a maching glob expression. " +
								"your expression '{}' seems too expensive. if this was intentional, try to increase '{}'", fileCounter[0],
								query.getOriginalPath(), Fields.maxFilesTraversed);
					}
					matchingFiles.add(file);
				}
				return FileVisitResult.CONTINUE;
			}
		});
		LOGGER.debug("traversed {} files to find {} files matching glob '{}'", fileCounter[0], matchingFiles.size(),
				query.getOriginalPath());
		matchingFiles.sort(Comparator.comparingInt(Path::getNameCount)
				.thenComparing(Comparator.naturalOrder()));
		return matchingFiles;
	}

	protected PathAndMatcher extractGlob(String path) {
		String[] parts = path.split("/", -1);
		int globAt = -1;
		int maxDepth = -1;
		for (int i = 0; i < parts.length; i++) {
			if (globAt == -1) { // still searching for first glob
				if (globPattern.matcher(parts[i])
						.matches()) {
					globAt = i;
					maxDepth = 0;
				}
			}
			if (globAt != -1) { // already found first glob
				if (parts[i].contains("**")) {
					maxDepth = Integer.MAX_VALUE;
					break;
				} else {
					maxDepth++;
				}
			}
		}
		if (globAt == -1) {
			return new PathAndMatcher(Paths.get(path), null, path, -1);
		}
		String beforeGlob = Stream.of(parts)
				.limit(globAt)
				.collect(Collectors.joining("/"));
		// TODO: support other pathMatchers like regex
		final PathMatcher pathMatcher = FileSystems.getDefault()
				.getPathMatcher("glob:" + path);
		return new PathAndMatcher(Paths.get(beforeGlob), pathMatcher, path, maxDepth);
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
	public Stream<String> read(URIish fileUri) {
		String path = fileUri.getSchemeSpecificPart();
		PathAndMatcher query = extractGlob(path);

		List<Path> matchingFiles;
		if (query.getPathMatcher() == null) { // a file must exist if not a glob
			matchingFiles = Collections.singletonList(query.getBasePath());
		} else {
			matchingFiles = getFilesMatching(query);
		}
		return matchingFiles.stream()
				.map(this::read);
	}

	@Override
	public boolean canHandle(URIish path) {
		return path.getScheme() == null || SCHEME.equals(path.getScheme());
	}
}
