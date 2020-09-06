package ch.kk7.confij.source.resource;

import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.source.resource.GitResource.GitSettings;
import lombok.SneakyThrows;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

class GitResourceFileTest extends GitTestBase {
//	private GitResource git;
//	private GitTestrepo testGit;
//
//	@BeforeEach
//	public void initGit(@TempDir File tempDir) throws Exception {
//		testGit = new GitTestrepo(tempDir);
//		StoredConfig config = testGit.getRepository()
//				.getConfig();
//		config.setInt("gc", null, "auto", 0);
//		config.setInt("gc", null, "autoPackLimit", 0);
//		config.setBoolean("receive", null, "autogc", false);
//
//		git = new GitResource(GitSettings.builder()
//				.remoteUrl(testGit.getWorkingDir())
//				.configFile(GitTestrepo.DEFAULT_FILE)
//				.build());
//	}

	@Override
	public GitSettings defaultSettings() {
		return GitSettings.builder()
				.remoteUrl(testGit.getWorkingDir())
				.configFile(GitTestrepo.DEFAULT_FILE)
				.build();
	}

	@Test
	void localFileRepoIsFetched() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		assertThatGitRead().isEqualTo(commit2.getShortMessage());

		RevCommit commit3 = testGit.addAndCommit();
		assertThatGitRead().isEqualTo(commit3.getShortMessage());
	}

	@Test
	void customRevision() throws Exception {
		RevCommit commit1 = testGit.addAndCommit();
		testGit.addAndCommit();
		assertThatGitRead(gitSettings.withGitRevision(commit1.abbreviate(7)
				.name())).isEqualTo(commit1.getShortMessage());
		assertThatGitRead(gitSettings.withGitRevision("HEAD~1")).as("relative to HEAD")
				.isEqualTo(commit1.getShortMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {"%20", // not a scheme at all
			"aaaaaaaaaaaaaaa", // doesn't exist
			"null", // kinda empty
			"HEAD~42",})
	void unknownRevision(String revision) throws Exception {
		testGit.addAndCommit();
		assertThatGitReadThrows(gitSettings.withGitRevision(revision)).isInstanceOf(ConfijSourceException.class);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@SneakyThrows
	private static void deleteRecursively(Path folder) {
		Files.walk(folder)
				.map(Path::toFile)
				.sorted(Comparator.reverseOrder())
				.forEach(File::delete);
	}

	// @Disabled("unstable due to lock files preventing file move")
	@Test
	void tmpRepoIsRemovedAtRuntimeRecovers() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		assertThatGitRead().isEqualTo(commit2.getShortMessage());

		// move away
		Path currentTempDir = GitResource.getFileForSeed(gitSettings.getRemoteUrl())
				.toPath();
		deleteRecursively(currentTempDir);
		assertThat(currentTempDir).doesNotExist();

		assertThatGitRead().isEqualTo(commit2.getShortMessage());
		assertThat(currentTempDir).exists();
	}

	@Test
	void fileDoesntExist() throws Exception {
		testGit.addAndCommit();
		assertThatGitReadThrows(gitSettings.withConfigFile("non3xisting.bla")).isInstanceOf(ConfijSourceException.class)
				.hasMessageContaining("non3xisting.bla");
	}

	@Test
	void fileOnBranch() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		testGit.createBranch("fuu");
		RevCommit branch1 = testGit.addAndCommit();
		RevCommit branch2 = testGit.addAndCommit();

		assertThatGitRead().isEqualTo(commit2.getShortMessage());
		assertThatGitRead(gitSettings.withGitRevision("refs/heads/fuu")).isEqualTo(branch2.getShortMessage());
		assertThatGitRead(gitSettings.withGitRevision("refs/heads/fuu~1")).isEqualTo(branch1.getShortMessage());
	}

	@Test
	void fileOnTag() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		testGit.createTag("v1.0.42");
		testGit.addAndCommit();
		assertThatGitRead(gitSettings.withGitRevision("refs/tags/v1.0.42")).isEqualTo(commit2.getShortMessage());
	}

	@Test
	void gitRepoIsEmpty() {
		assertThatGitReadThrows(gitSettings).isInstanceOf(ConfijSourceException.class);
	}

//	@Test
//	void coverFuu() {
//		assertThatThrownBy(() -> GitResource.toUri("/", null)).isInstanceOf(NullPointerException.class);
//		assertThatThrownBy(() -> GitResource.toUri(null, null)).isInstanceOf(NullPointerException.class);
//		GitResource.toUri("http://example.com", "file.txt");
//		GitResource.toUri("http://example.com/repo.git", "file.txt");
//		GitResource.toUri("http://example.com/repo.git/", "file.txt");
//		GitResource.toUri("http://example.com/repo.git", "/file.txt");
//		GitResource.toUri("http://example.com/repo.git/", "/file.txt");
//	}

	@Test
	void getFileForSeedIsStable() {
		assertThat(GitResource.getFileForSeed("fuu")).isEqualTo(GitResource.getFileForSeed("fuu"))
				.isNotEqualTo(GitResource.getFileForSeed("bar"));
		//noinspection ConstantConditions
		assertThatThrownBy(() -> GitResource.getFileForSeed(null)).isInstanceOf(NullPointerException.class);
	}


}
