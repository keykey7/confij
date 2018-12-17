package ch.kk7.confij.source.file.resource;

import org.assertj.core.api.WithAssertions;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;

@ExtendWith(TestCleanupExtension.class)
class GitResourceProviderTest implements WithAssertions {

	private GitResourceProvider git;
	private GitTestrepo testGit;

	@BeforeEach
	public void initGit() throws Exception {
		git = new GitResourceProvider();
		testGit = new GitTestrepo();
	}

	@Test
	public void localFileRepoIsFetched() throws Exception {
		String testFile = "file.txt";
		URI uri = GitResourceProvider.toUri(testGit.getWorkingDir(), testFile);
		testGit.addAndCommit(testFile, "111");
		testGit.addAndCommit(testFile, "222");
		assertThat(git.read(uri)).isEqualTo("222");

		testGit.addAndCommit(testFile, "333");
		assertThat(git.read(uri)).isEqualTo("333");
	}

	@Test
	public void customRevision() throws Exception {
		String testFile = "file.txt";
		RevCommit revCommit = testGit.addAndCommit(testFile, "111");
		testGit.addAndCommit(testFile, "222");

		URI revUri = GitResourceProvider.toUri(testGit.getWorkingDir(), testFile, revCommit.abbreviate(7).name());
		assertThat(git.read(revUri)).isEqualTo("111");

		URI relUri = GitResourceProvider.toUri(testGit.getWorkingDir(), testFile, "HEAD~1");
		assertThat(git.read(relUri)).isEqualTo("111");
	}
}
