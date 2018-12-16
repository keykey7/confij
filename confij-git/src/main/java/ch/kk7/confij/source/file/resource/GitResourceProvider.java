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

import static ch.kk7.confij.common.Util.not;

@ToString
@AutoService(ConfijResourceProvider.class)
public class GitResourceProvider extends AbstractResourceProvider {
	protected static final String TEMP_DIR_PREFIX = "confij-";
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(GitResourceProvider.class);
	private static final String SCHEME = "git";
	private static final Pattern URL_FILE_SPLITTER = Pattern.compile("^(?<url>.+(?:\\.git/?|[^:]/))/(?<file>.+)$");
	private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

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
		configFile = configFile.startsWith("/") ? configFile.substring(1) : configFile;
		String urlFileSep = remoteUri.matches(".*\\.git/?$") ? "/" : "//";
		return URI.create(SCHEME + ":" + remoteUri + urlFileSep + configFile + (gitRevision == null ? "" : "#" + gitRevision));
	}

	@Override
	public String read(URI path) {
		GitSettings settings = uriToGitSettings(path);
		Git git = gitCloneOrFetch(settings);
		return readFile(git, settings);
	}

	@Override
	public boolean canHandle(URI path) {
		return SCHEME.equals(path.getScheme());
	}

	protected GitSettings uriToGitSettings(URI uri) {
		String urlAndFile = uri.getSchemeSpecificPart();
		Matcher m = URL_FILE_SPLITTER.matcher(urlAndFile);
		if (!m.matches()) {
			throw new ConfijSourceException("expected the git source to have a format like " +
					"'git:https://example.com/repo.git/path/to/file.yaml' (repo ending in '.git/') or " +
					"'git:/var/opt/dir//path/to/file.yaml' (repo separated from file by double slash). " +
					"However the provided '{}' does not match: {}", urlAndFile, URL_FILE_SPLITTER.pattern());
		}
		// TODO: also support: git://example.com/repo.git/... and ssh+git://... as a shortform
		//       for http this might be confusing however

		String remoteUrl = m.group("url");
		String configFile = m.group("file");
		return GitSettings.builder()
				.gitRevision(Optional.ofNullable(uri.getFragment())
						.filter(not(String::isEmpty))
						.orElse(Constants.HEAD))
				.remoteUrl(remoteUrl)
				.configFile(configFile)
				.localDir(getFileForSeed(remoteUrl))
				.build();
	}

	protected File getFileForSeed(@NonNull String seed) {
		// another idea would be to use the SHA1 hash of the first git commit instead
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
		} catch (JGitInternalException | GitAPIException e) {
			throw new ConfijSourceException("failed to open and fetch git repository with {}", settings, e);
		}
	}

	protected Git gitFetch(Git git, GitSettings settings) throws GitAPIException {
		LOGGER.debug("git fetch: {}", settings);
		FetchResult fetchResult = git.fetch()
				.setRemote(settings.getRemoteUrl())
				.setRefSpecs("+refs/*:refs/*") // similar to --mirror
				.setRemoveDeletedRefs(true)
				.setCheckFetchedObjects(true)
				// .setTimeout()
				.call();
		LOGGER.info("git fetch result: {}", fetchResult.getTrackingRefUpdates());
		return git;
	}

	protected Git gitClone(GitSettings settings) throws GitAPIException {
		LOGGER.debug("git clone: {}", settings);
		return Git.cloneRepository()
				.setDirectory(settings.getLocalDir())
				.setURI(settings.getRemoteUrl())
				.setBare(true)
				// TODO: support .setCredentialsProvider()
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
