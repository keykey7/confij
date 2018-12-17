package ch.kk7.confij.source.file.resource;

import org.assertj.core.api.WithAssertions;
import org.eclipse.jgit.junit.http.AppServer;
import org.eclipse.jgit.junit.http.SimpleHttpServer;
import org.eclipse.jgit.transport.URIish;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;

@ExtendWith(TestCleanupExtension.class)
public class GitResourceProviderIntTest implements WithAssertions {
	private GitResourceProvider git;
	private GitTestrepo testGit;
	private SimpleHttpServer server;

	@BeforeEach
	public void initGit() throws Exception {
		git = new GitResourceProvider();
		testGit = new GitTestrepo();
		server = new SimpleHttpServer(testGit.getRepository());
		server.start();
	}

	@AfterEach
	public void tearDown() throws Exception {
		server.stop();
	}

	@Test
	void overHttp() throws Exception {
		URIish urIish = server.getUri()
				.setUser(AppServer.username)
				.setPass(AppServer.password);
		String testFile = "file.txt";
		URI uri = GitResourceProvider.toUri(urIish.toPrivateString(), testFile);
		testGit.addAndCommit(testFile, "111");
		testGit.addAndCommit(testFile, "222");
		assertThat(git.read(uri)).isEqualTo("222");
	}

	@Disabled("since it is a remote repo")
	@Test
	public void cloneGithubTestrepo() {
		// not private accessable "ssh://git@github.com/github/testrepo.git"
		URI uri = GitResourceProvider.toUri("https://github.com/github/testrepo.git", "test/alloc.c");
		assertThat(git.read(uri)).contains("Linus Torvalds");
		// once more: expecting a fetch instead of clone
		assertThat(git.read(uri)).contains("Linus Torvalds");
	}
}
