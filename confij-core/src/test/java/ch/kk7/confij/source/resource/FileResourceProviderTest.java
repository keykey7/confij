package ch.kk7.confij.source.resource;

import ch.kk7.confij.source.ConfijSourceBuilder.URIish;
import ch.kk7.confij.source.ConfijSourceException;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FileResourceProviderTest implements WithAssertions {
	private static Path tmpDir;
	private FileResourceProvider provider;

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
		FileResourceProviderTest.tmpDir = tmpDir;
	}

	@BeforeEach
	public void init() {
		provider = new FileResourceProvider();
	}

	public ListAssert<String> assertGlob(String glob) {
		return assertThat(provider.read(URIish.create(tmpDir.toString() + glob)));
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
		provider.setMaxFilesTraversed(11); // it shouldn't search through all 20 files now:
		assertGlob("/*1.txt").containsExactly("content#1");
	}

	@Test
	void maxFilesTraversed(@TempDir Path tmpDir) {
		provider.setMaxFilesTraversed(15);
		URIish urIish = URIish.create(tmpDir.toString() + "/**");
		assertThatThrownBy(() -> provider.read(urIish)).isInstanceOf(ConfijSourceException.class)
				.hasMessageContaining("traversed too many files");
	}

	@Test
	void maxFileMatches(@TempDir Path tmpDir) {
		provider.setMaxFileMatches(1);
		URIish urIish = URIish.create(tmpDir.toString() + "/*.txt");
		assertThatThrownBy(() -> provider.read(urIish)).isInstanceOf(ConfijSourceException.class)
				.hasMessageContaining("found too many files");
	}
}
