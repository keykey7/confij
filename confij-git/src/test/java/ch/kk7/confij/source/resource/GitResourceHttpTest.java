package ch.kk7.confij.source.resource;

import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.source.resource.GitResource.GitSettings;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.junit.http.AppServer;
import org.eclipse.jgit.junit.http.SimpleHttpServer;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.HttpConfig;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

class GitResourceHttpTest extends GitTestBase {
	private SimpleHttpServer server;

	@BeforeEach
	public void initServer(@TempDir File tempDir) throws Exception {
		testGit = new GitTestrepo(tempDir);
	}

	@SneakyThrows
	@Override
	public GitSettings defaultSettings() {
		server = new SimpleHttpServer(testGit.getRepository(), true);
		server.start();
		return GitSettings.builder()
				.remoteUrl(server.getUri()
						.toString())
				.configFile(GitTestrepo.DEFAULT_FILE)
				.usernamePasswordCredential(AppServer.username, AppServer.password)
				.build();
		// TODO: server.getSecureUri()
	}

	@AfterEach
	public void tearDown() throws Exception {
		server.stop();
	}

	@Test
	void basicAuthOverHttp() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		assertThatGitRead().isEqualTo(commit2.getShortMessage());

		RevCommit commit3 = testGit.addAndCommit();
		assertThatGitRead().isEqualTo(commit3.getShortMessage());
	}

	@Test
	void invalidPassword() {
		assertThatGitReadThrows(gitSettings.withCredentialsProvider(
				new UsernamePasswordCredentialsProvider(AppServer.username, "totallyWrongPassword"))).isInstanceOf(
				ConfijSourceException.class)
				.hasCauseInstanceOf(TransportException.class);
	}

	@Test
	void httpsFailsDueToCerts() throws Exception {
		testGit.addAndCommit();
		assertThatGitReadThrows(gitSettings.withRemoteUrl(server.getSecureUri()
				.toString())).isInstanceOf(ConfijSourceException.class)
				.hasStackTraceContaining("cert");
	}

	@Test
	void basicAuthOverHttps() throws Exception {
		GitSettings ignoreSsl = gitSettings.withGitInitHook(git -> git.getRepository()
				.getConfig()
				.setBoolean(HttpConfig.HTTP, null, HttpConfig.SSL_VERIFY_KEY, false));
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		assertThatGitRead(ignoreSsl).isEqualTo(commit2.getShortMessage());
		RevCommit commit3 = testGit.addAndCommit();
		assertThatGitRead(ignoreSsl).isEqualTo(commit3.getShortMessage());
	}

	@Test
	@DisabledIfEnvironmentVariable(named = "CI", matches = ".*")
	void cloneGithubTestrepo() {
		// not private accessable "ssh://git@github.com/github/testrepo.git"
		assertThatGitRead(gitSettings.withRemoteUrl("https://github.com/github/testrepo.git")
				.withConfigFile("test/alloc.c"));
		assertThatGitRead(gitSettings.withRemoteUrl("https://github.com/github/testrepo.git")
				.withConfigFile("test/alloc.c"));
	}
}
