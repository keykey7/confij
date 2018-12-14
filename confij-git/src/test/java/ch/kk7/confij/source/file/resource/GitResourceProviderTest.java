package ch.kk7.confij.source.file.resource;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

class GitResourceProviderTest implements WithAssertions {

	private GitResourceProvider git;
	private static final String GITHUB_SSH = "ssh://git@github.com/github/testrepo.git";
	private static final String GITHUB_HTTP = "https://github.com/github/testrepo.git";

	@BeforeEach
	public void initGit() throws IOException {
		git = new GitResourceProvider();
		dirCleanup();
	}

	@AfterEach
	public void dirCleanup() throws IOException {
		dirCleanup(GITHUB_SSH);
		dirCleanup(GITHUB_HTTP);
	}

	public void dirCleanup(String uri) throws IOException {
		File localDir = git.uriToGitSettings(GitResourceProvider.toUri(uri, ""))
				.getLocalDir();
		if (localDir.exists()) {
			Files.walk(localDir.toPath())
					.map(Path::toFile)
					.sorted(Comparator.reverseOrder())
					.forEach(File::delete);
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {GITHUB_HTTP})
	// TODO: @ValueSource(strings = {GITHUB_SSH, GITHUB_HTTP})
	public void cloneGithubTestrepo(String githubUri) {
		URI uri = GitResourceProvider.toUri(githubUri, "test/alloc.c");
		assertThat(git.read(uri)).contains("Linus Torvalds");
		// once more: expecting a fetch instead of clone
		assertThat(git.read(uri)).contains("Linus Torvalds");
	}
}
