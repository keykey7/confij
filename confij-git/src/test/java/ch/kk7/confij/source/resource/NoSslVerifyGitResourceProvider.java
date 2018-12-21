package ch.kk7.confij.source.resource;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.HttpConfig;

import java.io.IOException;

public class NoSslVerifyGitResourceProvider extends GitResourceProvider {
	@Override
	protected Git gitInit(GitSettings settings) throws GitAPIException, IOException {
		Git git = super.gitInit(settings);
		StoredConfig config = git.getRepository()
				.getConfig();
		config.setBoolean(HttpConfig.HTTP, null, HttpConfig.SSL_VERIFY_KEY, false);
		config.save();
		return git;
	}
}
