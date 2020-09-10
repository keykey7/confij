package ch.kk7.confij.source.resource;

import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.source.resource.ConfijResource.ResourceContent;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

class FileResourceTest implements WithAssertions {
	private static Path tmpDir;

	@BeforeAll
	public static void setupTestFiles(@TempDir Path tmpDir) throws IOException {
		for (int i = 0; i < 10; i++) {
			Files.write(tmpDir.resolve("f" + i + ".txt"), ("content#" + i).getBytes());
		}
		for (int i = 0; i < 10; i++) {
			Files.createDirectory(tmpDir.resolve("d" + i));
		}
		for (int i = 0; i < 10; i++) {
			Files.write(tmpDir.resolve("d" + i)
					.resolve("f" + i + ".txt"), ("xxx#" + i).getBytes());
		}
		FileResourceTest.tmpDir = tmpDir;
	}

	ListAssert<String> assertGlob(String glob, Function<FileResource, FileResource> mod) {
		return assertThat(mod.apply(FileResource.ofFile(tmpDir.toString() + glob))
				.read(x -> x)
				.map(ResourceContent::getContent));
	}

	ListAssert<String> assertGlob(String glob) {
		return assertGlob(glob, Function.identity());
	}

	@Test
	void realFiles() {
		assertGlob("/f[13].txt").containsExactly("content#1", "content#3");
		assertGlob("/**1.txt").containsExactly("content#1", "xxx#1");
		assertGlob("/d2/*").containsExactly("xxx#2");
		assertGlob("/*/f1.txt").containsExactly("xxx#1");
		assertGlob("/**").contains("xxx#9", "content#1"); // and more
	}

	@Test
	void searchOnlyPossibleMatches() {
		assertGlob("/*1.txt", x -> x.withMaxFilesTraversed(11)).as("it shouldn't search through all 20 files now")
				.containsExactly("content#1");
	}

	@Test
	void maxFilesTraversed() {
		assertThatThrownBy(() -> FileResource.ofFile(tmpDir.toString() + "/**")
				.withMaxFilesTraversed(15)
				.read(x -> x)).isInstanceOf(ConfijSourceException.class)
				.hasMessageContaining("traversed too many files");
	}

	@Test
	void maxFileMatches() {
		assertThatThrownBy(() -> FileResource.ofFile(tmpDir.toString() + "/*.txt")
				.withMaxFileMatches(1)
				.read(x -> x)).isInstanceOf(ConfijSourceException.class)
				.hasMessageContaining("found too many files");
	}
}
