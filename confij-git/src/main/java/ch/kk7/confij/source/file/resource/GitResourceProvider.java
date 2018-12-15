package ch.kk7.confij.source.file.resource;

import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.source.ConfijSourceException;
import com.google.auto.service.AutoService;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ToString
@AutoService(ConfijResourceProvider.class)
public class GitResourceProvider extends AbstractResourceProvider {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(GitResourceProvider.class);
	private static final Pattern SCHEME_PATTERN = Pattern.compile("git(?:@(?<rev>.+))?");
	private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));
	protected static final String TEMP_DIR_PREFIX = "confij-";

	@Value
	@Builder
	@NonFinal
	public static class GitSettings {
		@NonNull String gitRevision;
		@NonNull String remoteUrl;
		@NonNull File localDir;
		@NonNull String configFile;
	}

	public static URI toUri(@NonNull String remoteUri, @NonNull String configFile) {
		return toUri(remoteUri, configFile, null);
	}

	public static URI toUri(@NonNull String remoteUri, @NonNull String configFile, String gitRevision) {
		return URI.create("git" + (gitRevision == null ? "" : "@" + gitRevision) + ":" + remoteUri + "#" + configFile);
	}

	@Override
	public String read(URI path) {
		if (!canHandle(path) || path.getFragment() == null) {
			String expected = "git[@<gitRevision>]:https://example.com/repo.git#path/to/someFile.properties";
			throw new ConfijSourceException("expected the git source to be in the format {}, but got {}", expected, path);
		}
		// TODO: might be an idea to also do guessing on: https://example.com/repo.git/path/to/someFile.properties

		GitSettings settings = uriToGitSettings(path);
		Git git = gitCloneOrFetch(settings);
		return readFile(git, settings);
	}

	@Override
	public boolean canHandle(URI path) {
		return path.getScheme() != null &&
				SCHEME_PATTERN.matcher(path.getScheme())
						.matches();
	}

	protected GitSettings uriToGitSettings(URI uri) {
		Matcher m = SCHEME_PATTERN.matcher(uri.getScheme());
		if (!m.matches()) {
			throw new IllegalStateException();
		}
		String remoteUrl = uri.getSchemeSpecificPart();
		return GitSettings.builder()
				.gitRevision(Optional.ofNullable(m.group("rev"))
						.orElse(Constants.HEAD))
				.remoteUrl(remoteUrl)
				.configFile(uri.getFragment())
				.localDir(getFileForSeed(remoteUrl))
				.build();
	}

	protected File getFileForSeed(@NonNull String seed) {
		String name = UUID.nameUUIDFromBytes(seed.getBytes())
				.toString()
				.replace("-", "");
		return TEMP_DIR.resolve(TEMP_DIR_PREFIX + name + ".git")
				.toFile();
	}

	protected Git gitCloneOrFetch(GitSettings settings) {
		Git git = null;
		try {
			git = Git.open(settings.getLocalDir());
		} catch (RepositoryNotFoundException e) {
			// expected behaviour, let's clone it then...
		} catch (IOException e) {
			throw new ConfijSourceException("failed to check git repository with {}", settings, e);
		}
		try {
			if (git == null) {
				return gitClone(settings);
			}
			return gitFetch(git, settings);
		}
		catch (JGitInternalException | GitAPIException | IOException e) {
			throw new ConfijSourceException("failed to open and fetch git repository with {}", settings, e);
		}
	}

	protected Git gitFetch(Git git, GitSettings settings) throws GitAPIException, IOException {
		LOGGER.debug("git fetch: {}", settings);
		// verify there are no dirty files
//		if (!git.status()
//				.call()
//				.isClean()) {
//			throw new ConfijSourceException("git repository is not clean: {}", settings);
//		}
		FetchResult fetchResult = git.fetch()
				.setRemote(settings.getRemoteUrl())
				.setRefSpecs("+refs/*:refs/*") // similar to --mirror
				.setRemoveDeletedRefs(true)
				.setCheckFetchedObjects(true)
				// .setForceUpdate()
				// .setTimeout()
				.call();
		LOGGER.info("git fetch result: {}", fetchResult.getTrackingRefUpdates());
		git.log()
				.all()
				.call()
				.forEach(x -> LOGGER.info("gitlog: {}", x));
		return git;
	}

	protected Git gitClone(GitSettings settings) throws GitAPIException {
		LOGGER.debug("git clone: {}", settings);
		return Git.cloneRepository()
				.setDirectory(settings.getLocalDir())
				.setURI(settings.getRemoteUrl())
				.setBare(true)
				// .setCredentialsProvider()
				// .setTimeout()
				.call();
	}

	protected RevCommit getRevCommit(Git git, GitSettings settings) {
		Repository repository = git.getRepository();
		try {
			ObjectId head = repository.resolve(settings.getGitRevision());
			return repository.parseCommit(head);
		} catch (IOException e) {
			throw new ConfijException("unable to git resove {}", settings.getGitRevision(), e);
		}
	}

	protected String readFile(Git git, GitSettings settings) {
		RevCommit revCommit = getRevCommit(git, settings);
		try (TreeWalk walk = TreeWalk.forPath(git.getRepository(), settings.getConfigFile(), revCommit.getTree())) {
			if (walk != null) {
				byte[] bytes = git.getRepository()
						.open(walk.getObjectId(0))
						.getBytes();
				return new String(bytes, getCharset());
			} else {
				throw new ConfijException("File {} not found within git repo at commit {}", settings.getConfigFile(), revCommit);
			}
		} catch (IOException e) {
			throw new ConfijException("failed to read file {} within git repo at commit {}", settings.getConfigFile(), revCommit, e);
		}
	}
}
