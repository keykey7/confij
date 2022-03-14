package ch.kk7.confij.source.resource;

import ch.kk7.confij.source.resource.GitResource.GitSettings;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.WithAssertions;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

abstract class GitTestBase implements WithAssertions {
	protected GitSettings gitSettings;
	protected GitTestrepo testGit;

	@BeforeEach
	public void initTestGit(@TempDir File tempDir) throws Exception {
		testGit = new GitTestrepo(tempDir);
		StoredConfig config = testGit.getRepository()
				.getConfig();
		config.setInt("gc", null, "auto", 0);
		config.setInt("gc", null, "autoPackLimit", 0);
		config.setBoolean("receive", null, "autogc", false);
		gitSettings = defaultSettings();
	}

	public abstract GitSettings defaultSettings();

	public ObjectAssert<String> assertThatGitRead() {
		return assertThatGitRead(gitSettings);
	}

	public ObjectAssert<String> assertThatGitRead(GitSettings gitSettings) {
		GitResource gitResource = new GitResource(gitSettings);
		return (ObjectAssert<String>) assertThat(gitResource.read(x -> x)).hasSize(1)
				.element(0)
				.extracting(x -> x.getContent());
	}

	public AbstractThrowableAssert<?, ? extends Throwable> assertThatGitReadThrows(GitSettings gitSettings) {
		GitResource gitResource = new GitResource(gitSettings);
		return assertThatThrownBy(() -> gitResource.read(x -> x));
	}
}
