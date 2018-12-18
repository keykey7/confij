package ch.kk7.confij.source.file.resource;

import ch.kk7.confij.source.ConfijSourceException;
import org.assertj.core.api.WithAssertions;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.net.URI;

@ExtendWith(TempDirCleanupExtension.class)
class GitResourceProviderFileTest implements WithAssertions {
	private GitResourceProvider git;
	private GitTestrepo testGit;
	private URI fileUri;

	@BeforeEach
	public void initGit() throws Exception {
		git = new GitResourceProvider();
		testGit = new GitTestrepo();
		fileUri = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE);
	}

	@Test
	public void localFileRepoIsFetched() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		assertThat(git.read(fileUri)).isEqualTo(commit2.getShortMessage());

		RevCommit commit3 = testGit.addAndCommit();
		assertThat(git.read(fileUri)).isEqualTo(commit3.getShortMessage());
	}

	@Test
	public void customRevision() throws Exception {
		RevCommit commit1 = testGit.addAndCommit();
		testGit.addAndCommit();
		URI revUri = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE, commit1.abbreviate(7)
				.name());
		assertThat(git.read(revUri)).isEqualTo(commit1.getShortMessage());

		URI relUri = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE, "HEAD~1");
		assertThat(git.read(relUri)).isEqualTo(commit1.getShortMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {"%20", // not a scheme at all
			"aaaaaaaaaaaaaaa", // doesn't exist
			"null", // kinda empty
			"HEAD~42",})
	public void unknwonRevision(String revision) throws Exception {
		testGit.addAndCommit();
		URI revUri = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE, revision);
		assertThatThrownBy(() -> git.read(revUri)).isInstanceOf(ConfijSourceException.class);
	}

	@Test
	public void notGitScheme() {
		assertThat(git.canHandle(fileUri)).isTrue();
		assertThat(git.canHandle(URI.create("someUri"))).isFalse();
		assertThat(git.canHandle(URI.create("http://example.com/bla.git"))).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"http://example.com/not/git/scheme.git/file.txt", // not git scheme
			"git://example.com/missing.git/host.yaml", // no real host
			"git:http://example.com/dunno/where/file/starts.yaml", //no url/file separation
			"", // kinda empty
			".", // still kinda empty
			"..", // you're kidding me right?
	})
	public void notGitUri(String invalidUri) {
		URI uri = URI.create(invalidUri);
		assertThatThrownBy(() -> git.read(uri)).isInstanceOf(ConfijSourceException.class);
	}

	@Test
	public void tmpRepoIsRemovedAtRuntimeRecovers() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		assertThat(git.read(fileUri)).isEqualTo(commit2.getShortMessage());

		// move away
		File currentTempDir = git.uriToGitSettings(fileUri)
				.getLocalDir();
		currentTempDir.renameTo(new File(currentTempDir.getParentFile(), GitResourceProvider.TEMP_DIR_PREFIX + "moved"));
		assertThat(currentTempDir).doesNotExist();

		assertThat(git.read(fileUri)).isEqualTo(commit2.getShortMessage());
		assertThat(currentTempDir).exists();
	}

	@Test
	public void fileDoesntExist() throws Exception {
		testGit.addAndCommit();
		URI nonexistingFile = GitResourceProvider.toUri(testGit.getWorkingDir(), "non3xisting.bla");
		assertThatThrownBy(() -> git.read(nonexistingFile)).isInstanceOf(ConfijSourceException.class)
				.hasMessageContaining("non3xisting.bla");
	}

	@Test
	public void fileOnBranch() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		testGit.createBranch("fuu");
		RevCommit branch1 = testGit.addAndCommit();
		RevCommit branch2 = testGit.addAndCommit();

		assertThat(git.read(fileUri)).isEqualTo(commit2.getShortMessage());
		URI branchFile = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE, "refs/heads/fuu");
		assertThat(git.read(branchFile)).isEqualTo(branch2.getShortMessage());

		URI olderBranchFile = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE, "refs/heads/fuu~1");
		assertThat(git.read(olderBranchFile)).isEqualTo(branch1.getShortMessage());
	}

	@Test
	public void fileOnTag() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		testGit.createTag("v1.0.42");
		testGit.addAndCommit();

		URI tagFile = GitResourceProvider.toUri(testGit.getWorkingDir(), GitTestrepo.DEFAULT_FILE, "refs/tags/v1.0.42");
		assertThat(git.read(tagFile)).isEqualTo(commit2.getShortMessage());
	}


	@Test
	public void gitRepoIsEmpty() {
		assertThatThrownBy(() -> git.read(fileUri)).isInstanceOf(ConfijSourceException.class);
	}

	@Test
	public void coverFuu() {
		assertThatThrownBy(() -> GitResourceProvider.toUri("/", null)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> GitResourceProvider.toUri(null, null)).isInstanceOf(NullPointerException.class);
		GitResourceProvider.toUri("http://example.com", "file.txt");
		GitResourceProvider.toUri("http://example.com/repo.git", "file.txt");
		GitResourceProvider.toUri("http://example.com/repo.git/", "file.txt");
		GitResourceProvider.toUri("http://example.com/repo.git", "/file.txt");
		GitResourceProvider.toUri("http://example.com/repo.git/", "/file.txt");
	}

	@Test
	public void getFileForSeedIsStable() {
		assertThat(git.getFileForSeed("fuu")).isEqualTo(git.getFileForSeed("fuu"))
				.isNotEqualTo(git.getFileForSeed("bar"));
		assertThatThrownBy(() -> git.getFileForSeed(null)).isInstanceOf(NullPointerException.class);
	}
}
