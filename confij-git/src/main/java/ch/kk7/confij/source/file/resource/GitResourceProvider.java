package ch.kk7.confij.source.file.resource;

import ch.kk7.confij.common.ConfijException;
import com.google.auto.service.AutoService;
import lombok.Setter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import sun.security.action.GetPropertyAction;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.util.UUID;

@Setter
@AutoService(ConfijResourceProvider.class)
public class GitResourceProvider extends AbstractResourceProvider {
	private static final String SCHEME = "git";

	private static final Path tmpdir = Paths.get(AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")));

	private String revString = Constants.HEAD;
	private String uri;
	private File directory;
	private String configFile;
	private Git git;

	protected File getFileForSeed(String seed) {
		String name = UUID.nameUUIDFromBytes(seed.getBytes())
				.toString()
				.replace("-", "");
		return tmpdir.resolve("confij-" + name)
				.toFile();
	}

	protected File getTempFile() {
		try {
			return Files.createTempDirectory("confij")
					.toFile();
		} catch (IOException e) {
			throw new ConfijException("failed to create temp path", e);
		}
	}

	@Override
	public String read(URI path) {
		if (directory == null) {
			directory = getFileForSeed(uri);
		}

		try {
			git = Git.cloneRepository()
					.setDirectory(directory)
					.setURI(uri)
					// .setCredentialsProvider()
					// .setTimeout()
					.call();
		} catch (GitAPIException e) {
			throw new ConfijException("git clone {} failed", uri, e);
		}
		return readFile(getRevCommit(), configFile);
	}

	protected RevCommit getRevCommit() {
		Repository repository = git.getRepository();
		try {
			ObjectId head = repository.resolve(revString);
			return repository.parseCommit(head);
		} catch (IOException e) {
			throw new ConfijException("unable to git resove {}", revString, e);
		}
	}

	protected String readFile(RevCommit commit, String filepath) {
		try (TreeWalk walk = TreeWalk.forPath(git.getRepository(), filepath, commit.getTree())) {
			if (walk != null) {
				byte[] bytes = git.getRepository()
						.open(walk.getObjectId(0))
						.getBytes();
				return new String(bytes, getCharset());
			} else {
				throw new ConfijException("File {} not found within git repo at commit {}", filepath, commit);
			}
		} catch (IOException e) {
			throw new ConfijException("failed to read file {} within git repo at commit {}", filepath, commit, e);
		}
	}

	@Override
	public boolean canHandle(URI path) {
		return SCHEME.equals(path.getScheme());
	}
}
