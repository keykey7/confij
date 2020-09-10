package ch.kk7.confij.source.resource;

import ch.kk7.confij.common.Util;
import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.source.any.ConfijAnyResource;
import ch.kk7.confij.template.ValueResolver.StringResolver;
import com.google.auto.service.AutoService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.NonFinal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
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
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@With
@Value
@NonFinal
@FieldNameConstants
@AllArgsConstructor
public class FileResource implements ConfijResource {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(FileResource.class);
	private static Pattern globPattern = Pattern.compile("(^|.*[^\\\\])([*?]|\\[.+]|\\{.+}).*");
	@NonNull String fileTemplate;
	@NonNull String charsetTemplate;
	int maxFileMatches;
	int maxFilesTraversed;

	public FileResource(String fileTemplate) {
		this(fileTemplate, Defaults.CHARSET_NAME, 50, 10000);
	}

	public static FileResource ofFile(String file) {
		return new FileResource(file);
	}

	public static FileResource ofFile(File file) {
		return ofFile(file.toString());
	}

	public static FileResource ofPath(Path file) {
		return ofFile(file.toFile());
	}

	protected static PathAndMatcher extractGlob(String path) {
		String[] parts = path.split("/", -1);
		int globAt = -1;
		int maxDepth = -1;
		for (int i = 0; i < parts.length; i++) {
			if (globAt == -1 &&
					globPattern.matcher(parts[i])
							.matches()) { // still searching for first glob
				globAt = i;
				maxDepth = 0;
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

	@Override
	@SneakyThrows
	public Stream<ResourceContent> read(StringResolver resolver) {
		String fileStr = Util.getSchemeSpecificPart(resolver.resolve(fileTemplate));
		String charsetStr = resolver.resolve(charsetTemplate);
		Charset charset = Charset.forName(charsetStr);
		PathAndMatcher query = extractGlob(fileStr);
		List<Path> matchingFiles;
		if (query.getPathMatcher() == null) { // a file must exist if not a glob
			matchingFiles = Collections.singletonList(query.getBasePath());
		} else {
			matchingFiles = getFilesMatching(query);
		}
		return matchingFiles.stream()
				.map(file -> new ResourceContent(fileToStr(file, charset), file.toString()));
	}

	protected String fileToStr(Path path, Charset charset) {
		// TODO: make sure the file is not being written to
		try(FileInputStream fis = new FileInputStream(path.toFile())) {
			Scanner s = new Scanner(fis, charset.name()).useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		} catch (FileNotFoundException e) {
			throw new ConfijSourceException("failed to read from file '{}' because it doesn't exist", path.toAbsolutePath(), e);
		} catch (IOException e) {
			throw new ConfijSourceException("failed to read from file '{}'", path.toAbsolutePath(), e);
		}
	}

	@Value
	@NonFinal
	protected static class PathAndMatcher {
		Path basePath;
		PathMatcher pathMatcher;
		String originalPath;
		int maxDepth;
	}

	@ToString
	@AutoService(ConfijAnyResource.class)
	public static class AnyFileResource implements ConfijAnyResource {
		public static final String SCHEME = "file";

		@Override
		public Optional<FileResource> maybeHandle(String pathTemplate) {
			if (Util.getScheme(pathTemplate)
					.orElse(SCHEME)
					.equals(SCHEME)) {
				return Optional.of(FileResource.ofFile(pathTemplate));
			}
			return Optional.empty();
		}
	}
}
