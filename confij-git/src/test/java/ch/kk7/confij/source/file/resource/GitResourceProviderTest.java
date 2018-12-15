package ch.kk7.confij.source.file.resource;

import org.assertj.core.api.WithAssertions;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

class GitResourceProviderTest implements WithAssertions {

	private GitResourceProvider git;

	// not private accessable "ssh://git@github.com/github/testrepo.git"
	private static final String GITHUB_HTTP = "https://github.com/github/testrepo.git";

	@BeforeEach
	public void initGit() throws IOException {
		tempDirCleanup();
		git = new GitResourceProvider();
	}

	@AfterEach
	public void tempDirCleanup() throws IOException {
//		if (true) {
//			return;
//		}
		Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
		Arrays.stream(tempDir.toFile()
				.listFiles())
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

	public Git initTestRepo() throws Exception {
		File repoDir = File.createTempFile(GitResourceProvider.TEMP_DIR_PREFIX + "test-", "");
		repoDir.delete();
		return Git.init()
				.setDirectory(repoDir)
				.call();
	}

	public RevCommit addAndCommit(Git git, String filename, String content) throws Exception {
		File file1 = new File(git.getRepository()
				.getWorkTree(), filename);
		Files.write(file1.toPath(), content.getBytes(StandardCharsets.UTF_8));
		git.add()
				.addFilepattern(file1.getName())
				.call();
		return git.commit()
				.setMessage("committed " + filename)
				.call();
	}

	@Test
	public void localRepo() throws Exception {
		Git testRepo = initTestRepo();
		String testFile = "file1.txt";
		addAndCommit(testRepo, testFile, "111");
		addAndCommit(testRepo, testFile, "222");

		URI uri = GitResourceProvider.toUri(testRepo.getRepository()
				.getDirectory()
				.toString(), testFile);
		assertThat(git.read(uri)).isEqualTo("222");

		addAndCommit(testRepo, testFile, "333");
		assertThat(git.read(uri)).isEqualTo("333");
	}

	@Test
	public void cloneGithubTestrepo() {
		URI uri = GitResourceProvider.toUri(GITHUB_HTTP, "test/alloc.c");
		assertThat(git.read(uri)).contains("Linus Torvalds");
		// once more: expecting a fetch instead of clone
		assertThat(git.read(uri)).contains("Linus Torvalds");
	}
}
