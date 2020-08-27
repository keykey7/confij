package ch.kk7.confij.source.resource;

import ch.kk7.confij.source.ConfijSourceBuilder;
import ch.kk7.confij.source.ConfijSourceException;
import org.assertj.core.api.WithAssertions;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.junit.http.AppServer;
import org.eclipse.jgit.junit.http.SimpleHttpServer;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

class GitResourceProviderHttpTest implements WithAssertions {
	private GitResourceProvider git;
	private GitTestrepo testGit;
	private SimpleHttpServer server;
	private ConfijSourceBuilder.URIish httpUri;
	private ConfijSourceBuilder.URIish httpsUri;

	@BeforeEach
	public void initServer(@TempDir File tempDir) throws Exception {
		git = new GitResourceProvider();
		testGit = new GitTestrepo(tempDir);
		server = new SimpleHttpServer(testGit.getRepository(), true);
		server.start();
		httpUri = GitResourceProvider.toUri(server.getUri()
				.setUser(AppServer.username)
				.setPass(AppServer.password)
				.toPrivateString(), GitTestrepo.DEFAULT_FILE);
		httpsUri = GitResourceProvider.toUri(server.getSecureUri()
				.setUser(AppServer.username)
				.setPass(AppServer.password)
				.toPrivateString(), GitTestrepo.DEFAULT_FILE);
	}

	@AfterEach
	public void tearDown() throws Exception {
		server.stop();
	}

	private String gitRead(ConfijSourceBuilder.URIish uri) {
		return git.read(uri)
				.findAny()
				.orElseThrow(IllegalStateException::new);
	}

	@Test
	void basicAuthOverHttp() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		assertThat(gitRead(httpUri)).isEqualTo(commit2.getShortMessage());

		RevCommit commit3 = testGit.addAndCommit();
		assertThat(gitRead(httpUri)).isEqualTo(commit3.getShortMessage());
	}

	@Test
	void invalidPassword() {
		ConfijSourceBuilder.URIish invalidPasswordUri = GitResourceProvider.toUri(server.getUri()
				.setUser(AppServer.username)
				.setPass("totallyWrongPassword")
				.toPrivateString(), GitTestrepo.DEFAULT_FILE);
		assertThatThrownBy(() -> git.read(invalidPasswordUri)).isInstanceOf(ConfijSourceException.class)
				.hasCauseInstanceOf(TransportException.class);
	}

	@Test
	void httpsFailsDueToCerts() throws Exception {
		testGit.addAndCommit();
		assertThatThrownBy(() -> gitRead(httpsUri)).isInstanceOf(ConfijSourceException.class)
				.hasStackTraceContaining("cert");
	}

	@Test
	void basicAuthOverHttps() throws Exception {
		git = new NoSslVerifyGitResourceProvider();
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		assertThat(gitRead(httpUri)).isEqualTo(commit2.getShortMessage());

		RevCommit commit3 = testGit.addAndCommit();
		assertThat(gitRead(httpUri)).isEqualTo(commit3.getShortMessage());
	}

	@Disabled("since it is a remote repo")
	@Test
	void cloneGithubTestrepo() {
		// not private accessable "ssh://git@github.com/github/testrepo.git"
		ConfijSourceBuilder.URIish uri = GitResourceProvider.toUri("https://github.com/github/testrepo.git", "test/alloc.c");
		assertThat(gitRead(uri)).contains("Linus Torvalds");
		// once more: expecting a fetch instead of clone
		assertThat(gitRead(uri)).contains("Linus Torvalds");
	}
}
