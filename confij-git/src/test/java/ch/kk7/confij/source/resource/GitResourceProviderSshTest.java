package ch.kk7.confij.source.resource;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import org.assertj.core.api.WithAssertions;
import org.eclipse.jgit.junit.ssh.SshTestGitServer;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GitResourceProviderSshTest implements WithAssertions {
	private GitResourceProvider git;
	private GitTestrepo testGit;
	protected static final String TEST_USER = "testuser";
	private SshTestGitServer server;
	private URI sshUri;

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

	private static File createKeyPair(File privateKeyFile) throws Exception {
		JSch jsch = new JSch();
		KeyPair pair = KeyPair.genKeyPair(jsch, KeyPair.RSA, 2048);
		try (OutputStream out = new FileOutputStream(privateKeyFile)) {
			pair.writePrivateKey(out);
		}
		File publicKeyFile = new File(privateKeyFile.getParentFile(), privateKeyFile.getName() + ".pub");
		try (OutputStream out = new FileOutputStream(publicKeyFile)) {
			pair.writePublicKey(out, TEST_USER);
		}
		return publicKeyFile;
	}

	@BeforeEach
	public void initServer(@TempDir File tempDir) throws Exception {
		testGit = new GitTestrepo(tempDir);
		File sshDir = File.createTempFile(GitResourceProvider.TEMP_DIR_PREFIX, "_ssh");
		assertTrue(sshDir.delete());
		assertTrue(sshDir.mkdir());
		File privateKeyFile = new File(sshDir, "id_testkey");

		ByteArrayOutputStream publicHostKey = new ByteArrayOutputStream();
		// Start a server with our test user and the first key.
		server = new SshTestGitServer(TEST_USER, createKeyPair(privateKeyFile).toPath(), testGit.getRepository(),
				createHostKey(publicHostKey));
		int testPort = server.start();
		Assert.assertTrue(testPort > 0);
		File knownHostsFile = new File(sshDir, "known_hosts");
		Files.write(knownHostsFile.toPath(),
				Collections.singleton("[localhost]:" + testPort + ' ' + publicHostKey.toString(StandardCharsets.US_ASCII.name())));
		server.start();
		sshUri = GitResourceProvider.toUri("ssh://" + TEST_USER + "@localhost:" + testPort, GitTestrepo.DEFAULT_FILE);
		git = new ExplicitSshKeyGitResourceProvider(privateKeyFile, knownHostsFile);
	}

	@AfterEach
	public void tearDown() throws Exception {
		server.stop();
	}

	@Test
	public void viaSshUserAndPK() throws Exception {
		testGit.addAndCommit();
		RevCommit commit2 = testGit.addAndCommit();
		assertThat(git.read(sshUri)).isEqualTo(commit2.getShortMessage());
	}
}
