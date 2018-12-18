package ch.kk7.confij.source.file.resource;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

public class GitTestrepo {
	public static final String DEFAULT_FILE = "file.txt";
	private int counter = 0;
	private Git git;

	public GitTestrepo() throws Exception {
		init();
	}

	public GitTestrepo init() throws Exception {
		File repoDir = File.createTempFile(GitResourceProvider.TEMP_DIR_PREFIX + "test-", "");
		repoDir.delete();
		git = Git.init()
				.setDirectory(repoDir)
				.call();
		return this;
	}

	public void addFile(String filename, String content) throws Exception {
		Files.write(new File(git.getRepository()
				.getWorkTree(), filename).toPath(), content.getBytes(StandardCharsets.UTF_8));
		git.add()
				.addFilepattern(filename)
				.call();
	}

	public RevCommit addAndCommit() throws Exception {
		return addAndCommit(DEFAULT_FILE, "nr" + (counter++) + " " + UUID.randomUUID());
	}

	public RevCommit addAndCommit(String filename, String content) throws Exception {
		addFile(filename, content);
		return commit(content);
	}

	public RevCommit commit(String msg) throws Exception {
		return git.commit()
				.setMessage(msg)
				.call();
	}

	public String getWorkingDir() {
		return git.getRepository()
				.getWorkTree()
				.getAbsolutePath();
	}

	public Repository getRepository() {
		return git.getRepository();
	}
}
