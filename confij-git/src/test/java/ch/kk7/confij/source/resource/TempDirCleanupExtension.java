package ch.kk7.confij.source.resource;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class TempDirCleanupExtension implements BeforeAllCallback, AfterTestExecutionCallback {
	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		afterTestExecution(context);
	}

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
		Arrays.stream(Objects.requireNonNull(tempDir.toFile()
				.listFiles()))
				.filter(x -> x.getName()
						.startsWith(GitResourceProvider.TEMP_DIR_PREFIX))
				.map(File::toPath)
				.flatMap(x -> {
					try {
						return Files.walk(x);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
				.map(Path::toFile)
				.sorted(Comparator.reverseOrder())
				.forEach(File::delete);
	}
}
