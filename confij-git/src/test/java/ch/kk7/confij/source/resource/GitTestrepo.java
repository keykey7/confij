package ch.kk7.confij.source.resource;

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
	private final Git git;

	public GitTestrepo(File tmpDir) throws Exception {
		git = Git.init()
				.setDirectory(tmpDir)
				.call();
	}

	public void addFile(String filename, String content) throws Exception {
		Files.write(new File(git.getRepository()
				.getWorkTree(), filename).toPath(), content.getBytes(StandardCharsets.UTF_8));
		git.add()
				.addFilepattern(filename)
				.call();
	}

	public void createTag(String tagName) throws Exception {
		git.tag()
				.setAnnotated(true)
				.setMessage("tagged " + tagName)
				.setName(tagName)
				.call();
	}

	public void createBranch(String branchName) throws Exception {
		git.checkout()
				.setCreateBranch(true)
				.setName(branchName)
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
