package ch.kk7.confij.source.resource;

import ch.kk7.confij.source.ConfijSourceBuilder;
import ch.kk7.confij.source.ConfijSourceException;
import org.assertj.core.api.WithAssertions;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;

class GitResourceProviderFileTest implements WithAssertions {
	private GitResourceProvider git;
	private GitTestrepo testGit;
	private ConfijSourceBuilder.URIish fileUri;

	@BeforeEach
	public void initGit(@TempDir File tempDir) throws Exception {
		git = new GitResourceProvider();
		testGit = new GitTestrepo(tempDir);
		StoredConfig config = testGit.getRepository()
				.getConfig();
		config.setInt("gc", null, "auto", 0);
		config.setInt("gc", null, "autoPackLimit", 0);
		config.setBoolean("receive", null, "autogc", false);

		fileUri = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE);
	}

	private String gitRead(ConfijSourceBuilder.URIish uri) {
		return git.read(uri)
				.findAny()
				.orElseThrow(IllegalStateException::new);
	}

	@Test
	void localFileRepoIsFetched() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		assertThat(gitRead(fileUri)).isEqualTo(commit2.getShortMessage());

		RevCommit commit3 = testGit.addAndCommit();
		assertThat(gitRead(fileUri)).isEqualTo(commit3.getShortMessage());
	}

	@Test
	void customRevision() throws Exception {
		RevCommit commit1 = testGit.addAndCommit();
		testGit.addAndCommit();
		ConfijSourceBuilder.URIish revUri = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE,
				commit1.abbreviate(7)
						.name());
		assertThat(gitRead(revUri)).isEqualTo(commit1.getShortMessage());

		ConfijSourceBuilder.URIish relUri = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE, "HEAD~1");
		assertThat(gitRead(relUri)).isEqualTo(commit1.getShortMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {"%20", // not a scheme at all
			"aaaaaaaaaaaaaaa", // doesn't exist
			"null", // kinda empty
			"HEAD~42",})
	void unknownRevision(String revision) throws Exception {
		testGit.addAndCommit();
		ConfijSourceBuilder.URIish revUri = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE, revision);
		assertThatThrownBy(() -> gitRead(revUri)).isInstanceOf(ConfijSourceException.class);
	}

	@Test
	void notGitScheme() {
		assertThat(git.canHandle(fileUri)).isTrue();
		assertThat(git.canHandle(ConfijSourceBuilder.URIish.create("someUri"))).isFalse();
		assertThat(git.canHandle(ConfijSourceBuilder.URIish.create("http://example.com/bla.git"))).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"http://example.com/not/git/scheme.git/file.txt", // not git scheme
			"git://example.com/missing.git/host.yaml", // no real host
			"git:http://example.com/dunno/where/file/starts.yaml", //no url/file separation
			"", // kinda empty
			".", // still kinda empty
			"..", // you're kidding me right?
	})
	void notGitUri(String invalidUri) {
		ConfijSourceBuilder.URIish uri = ConfijSourceBuilder.URIish.create(invalidUri);
		assertThatThrownBy(() -> gitRead(uri)).isInstanceOf(ConfijSourceException.class);
	}

	@Disabled("unstable due to lock files preventing file move")
	@Test
	void tmpRepoIsRemovedAtRuntimeRecovers() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		assertThat(gitRead(fileUri)).isEqualTo(commit2.getShortMessage());

		// move away
		File currentTempDir = git.uriToGitSettings(fileUri)
				.getLocalDir();
		File movedDir = new File(currentTempDir.getParentFile(), GitResourceProvider.TEMP_DIR_PREFIX + "moved");
		assertThat(currentTempDir).exists();
		assertThat(currentTempDir.renameTo(movedDir)).isTrue();
		assertThat(currentTempDir).doesNotExist();

		assertThat(gitRead(fileUri)).isEqualTo(commit2.getShortMessage());
		assertThat(currentTempDir).exists();
	}

	@Test
	void fileDoesntExist() throws Exception {
		testGit.addAndCommit();
		ConfijSourceBuilder.URIish nonexistingFile = GitResourceProvider.toUri(testGit.getWorkingDir(), "non3xisting.bla");
		assertThatThrownBy(() -> gitRead(nonexistingFile)).isInstanceOf(ConfijSourceException.class)
				.hasMessageContaining("non3xisting.bla");
	}

	@Test
	void fileOnBranch() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		testGit.createBranch("fuu");
		RevCommit branch1 = testGit.addAndCommit();
		RevCommit branch2 = testGit.addAndCommit();

		assertThat(gitRead(fileUri)).isEqualTo(commit2.getShortMessage());
		ConfijSourceBuilder.URIish branchFile = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE,
				"refs/heads/fuu");
		assertThat(gitRead(branchFile)).isEqualTo(branch2.getShortMessage());

		ConfijSourceBuilder.URIish olderBranchFile = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE,
				"refs/heads/fuu~1");
		assertThat(gitRead(olderBranchFile)).isEqualTo(branch1.getShortMessage());
	}

	@Test
	void fileOnTag() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		testGit.createTag("v1.0.42");
		testGit.addAndCommit();

		ConfijSourceBuilder.URIish tagFile = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE,
				"refs/tags/v1.0.42");
		assertThat(gitRead(tagFile)).isEqualTo(commit2.getShortMessage());
	}

	@Test
	void gitRepoIsEmpty() {
		assertThatThrownBy(() -> gitRead(fileUri)).isInstanceOf(ConfijSourceException.class);
	}

	@Test
	void coverFuu() {
		assertThatThrownBy(() -> GitResourceProvider.toUri("/", null)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> GitResourceProvider.toUri(null, null)).isInstanceOf(NullPointerException.class);
		GitResourceProvider.toUri("http://example.com", "file.txt");
		GitResourceProvider.toUri("http://example.com/repo.git", "file.txt");
		GitResourceProvider.toUri("http://example.com/repo.git/", "file.txt");
		GitResourceProvider.toUri("http://example.com/repo.git", "/file.txt");
		GitResourceProvider.toUri("http://example.com/repo.git/", "/file.txt");
	}

	@Test
	void getFileForSeedIsStable() {
		assertThat(git.getFileForSeed("fuu")).isEqualTo(git.getFileForSeed("fuu"))
				.isNotEqualTo(git.getFileForSeed("bar"));
		//noinspection ConstantConditions
		assertThatThrownBy(() -> git.getFileForSeed(null)).isInstanceOf(NullPointerException.class);
	}
}
