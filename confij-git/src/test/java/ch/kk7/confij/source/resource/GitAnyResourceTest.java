package ch.kk7.confij.source.resource;

import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.source.resource.GitResource.GitAnyResource;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GitAnyResourceTest implements WithAssertions {
	private final GitAnyResource gitAny = new GitAnyResource();

	@ParameterizedTest
	@ValueSource(strings = {"git:http://example.com/not/git/scheme.git/file.txt",
			"git:git@example.com/missing.git/host.yaml",
			"git:http://example.com/path/where//file/starts.yaml",
	})
	void canHandle(String path) {
		assertThat(gitAny.maybeHandle(path)).isNotEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {"http://example.com/not/git/scheme.git/file.txt", // not git scheme
			"", // kinda empty
			".", // still kinda empty
			"..", // you're kidding me right?
	})
	void notGitUri(String noGitPath) {
		assertThat(gitAny.maybeHandle(noGitPath)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"git://example.com/missing/host.yaml", // no real host
			"git:http://example.com/dunno/where/file/starts.yaml", //no url/file separation
			"git:", // kinda empty
			"git:.", // still kinda empty
			"git:..", // you're kidding me right?
			"git://.",
	})
	void invalidGitUri(String noGitPath) {
		assertThatThrownBy(() -> gitAny.maybeHandle(noGitPath)).isInstanceOf(ConfijException.class);
	}
}
