package ch.kk7.confij.source.resource;

import ch.kk7.confij.source.resource.GitResource.GitSettings;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import lombok.SneakyThrows;
import org.eclipse.jgit.junit.ssh.SshTestGitServer;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

class GitResourceSshTest extends GitTestBase {
	protected static final String TEST_USER = "testuser";
	private static Path privateKeyFile;
	private static Path publicKeyFile;
	private static Path knownHostsFile;
	private SshTestGitServer server;

	private static byte[] createHostKey(OutputStream publicKey) throws Exception {
		JSch jsch = new JSch();
		KeyPair pair = KeyPair.genKeyPair(jsch, KeyPair.RSA, 2048);
		pair.writePublicKey(publicKey, "");
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			pair.writePrivateKey(out);
			out.flush();
			return out.toByteArray();
		}
	}

	private static Path createKeyPair(Path privateKeyFile) throws Exception {
		JSch jsch = new JSch();
		KeyPair pair = KeyPair.genKeyPair(jsch, KeyPair.RSA, 2048);
		try (OutputStream out = new FileOutputStream(privateKeyFile.toFile())) {
			pair.writePrivateKey(out);
		}
		Path publicKeyFile = privateKeyFile.getParent()
				.resolve(privateKeyFile.getFileName() + ".pub");
		try (OutputStream out = new FileOutputStream(publicKeyFile.toFile())) {
			pair.writePublicKey(out, TEST_USER);
		}
		return publicKeyFile;
	}

	@BeforeAll
	public static void initSshDir(@TempDir Path sshDir) throws Exception {
		privateKeyFile = sshDir.resolve("id_testkey");
		publicKeyFile = createKeyPair(privateKeyFile);
		knownHostsFile = sshDir.resolve("known_hosts");
	}

	@SneakyThrows
	@Override
	public GitSettings defaultSettings() {
		ByteArrayOutputStream publicHostKey = new ByteArrayOutputStream();
		// Start a server with our test user and the first key.
		server = new SshTestGitServer(TEST_USER, publicKeyFile, testGit.getRepository(), createHostKey(publicHostKey));
		int testPort = server.start();
		assertThat(testPort).isGreaterThan(0);
		Files.write(knownHostsFile,
				Collections.singleton("[localhost]:" + testPort + ' ' + publicHostKey.toString(StandardCharsets.US_ASCII.name())));
		server.start();
		return GitSettings.builder()
				.remoteUrl("ssh://" + TEST_USER + "@localhost:" + testPort + "/repo.git")
				.configFile(GitTestrepo.DEFAULT_FILE)
				.transportConfigCallback(transport -> {
					SshTransport sshTransport = (SshTransport) transport;
					sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {
						@Override
						protected void configure(Host host, Session session) {
							// nop
						}

						@Override
						protected JSch createDefaultJSch(FS fs) throws JSchException {
							JSch defaultJSch = super.createDefaultJSch(fs);
							defaultJSch.removeAllIdentity();
							defaultJSch.addIdentity(privateKeyFile.toString());
							defaultJSch.setKnownHosts(knownHostsFile.toString());
							return defaultJSch;
						}
					});
				})
				.build();
	}

	@AfterEach
	public void tearDown() throws Exception {
		server.stop();
	}

	@Test
	void viaSshUserAndPK() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		assertThatGitRead().isEqualTo(commit2.getShortMessage());
	}
}
