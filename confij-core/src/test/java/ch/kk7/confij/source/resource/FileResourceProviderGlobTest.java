package ch.kk7.confij.source.resource;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

class FileResourceProviderGlobTest implements WithAssertions {
	private FileResourceProvider provider;

	@BeforeEach
	public void init() {
		provider = new FileResourceProvider();
	}

	@Test
	void oneGlob() {
		assertThat(provider.extractGlob("/fuu/bar/a*.txt")).satisfies(x -> {
			assertThat(x.getBasePath()).isEqualTo(Paths.get("/fuu/bar"));
			assertThat(x.getMaxDepth()).isEqualTo(1);
			assertThat(x.getPathMatcher()
					.matches(Paths.get("/fuu/bar/abc.txt"))).isTrue();
			assertThat(x.getPathMatcher()
					.matches(Paths.get("/fuu/bar/a.txt"))).isTrue();
			assertThat(x.getPathMatcher()
					.matches(Paths.get("/fuu/bar/b.txt"))).isFalse();
			assertThat(x.getPathMatcher()
					.matches(Paths.get("a.txt"))).isFalse();
		});
	}

	@Test
	void globAtRoot() {
		assertThat(provider.extractGlob("x**.txt")).satisfies(x -> {
			assertThat(x.getBasePath()).isEqualTo(Paths.get(""));
		});
	}

	@Test
	void escapedGlob() {
		assertThat(provider.extractGlob("fuu/BA\\*")).satisfies(x -> {
			assertThat(x.getBasePath()).isEqualTo(Paths.get("fuu/BA\\*"));
		});
	}

	@Test
	void multiGlob() {
		assertThat(provider.extractGlob("fuu/**/xxx/?.x")).satisfies(x -> {
			assertThat(x.getBasePath()).isEqualTo(Paths.get("fuu"));
			assertThat(x.getPathMatcher()
					.matches(Paths.get("fuu/a/b/c/d/e/xxx/xxx/f.x"))).isTrue();
			assertThat(x.getMaxDepth()).isEqualTo(Integer.MAX_VALUE);
		});
	}
}
