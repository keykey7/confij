package ch.kk7.confij.source.file.resource;

import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.source.ConfijSourceException;
import com.google.auto.service.AutoService;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
	protected static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(GitResourceProvider.class);
	private static final String SCHEME = "git";
	private static final Pattern URL_FILE_SPLITTER = Pattern.compile("^(?<url>.+(?:\\.git/?|[^:]/))/(?<file>.+)$");
	private static final String FETCH_REFSPEC = "+refs/*:refs/*";

	@Value
	@Wither
	@Builder
	@NonFinal
	public static class GitSettings {
		@NonNull String gitRevision;
		@NonNull String remoteUrl;
		@NonNull File localDir;
		@NonNull String configFile;
		CredentialsProvider credentialsProvider;
		int timeoutInSeconds;
		TransportConfigCallback transportConfigCallback;
	}

	public static URI toUri(String remoteUri, String configFile) {
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
					"'git:https://example.com/repo.git/path/to/file.yaml' (repo ending in '.git') or " +
					"'git:/var/opt/dir//path/to/file.yaml' (repo separated from file by double slash). " +
					"However the provided '{}' does not match: {}", urlAndFile, URL_FILE_SPLITTER.pattern());
		}
		// we might also support: git://example.com/repo.git/... and ssh+git://... as a shortform
		// for http this might be confusing however

		String remoteUrl = m.group("url");
		String configFile = m.group("file");
		return GitSettings.builder()
				.gitRevision(Optional.ofNullable(uri.getFragment())
						.filter(not(String::isEmpty))
						.orElse(Constants.HEAD))
				.remoteUrl(remoteUrl)
				.configFile(configFile)
				.localDir(getFileForSeed(remoteUrl))
				.credentialsProvider(getCredentialsProvider(remoteUrl))
				.build();
	}

	protected CredentialsProvider getCredentialsProvider(String remoteUrl) {
		final URIish urIish;
		try {
			urIish = new URIish(remoteUrl);
		} catch (URISyntaxException e) {
			throw new ConfijSourceException("not URIish: " + remoteUrl, e);
		}
		if (urIish.getUser() != null) {
			// that's not 100% correct, you can very well send creds in the URL and ignore Authorization headers...
			return new UsernamePasswordCredentialsProvider(urIish.getUser(), Optional.ofNullable(urIish.getPass())
					.orElse(""));
		}
		return null;
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
		} catch (JGitInternalException | GitAPIException | IOException e) {
			throw new ConfijSourceException("failed to open and fetch git repository with {}", settings, e);
		}
	}

	protected Git gitFetch(Git git, GitSettings settings) throws GitAPIException, IOException {
		LOGGER.debug("git fetch: {}", settings);
		FetchResult fetchResult = git.fetch()
				.setRemote(settings.getRemoteUrl())
				.setRefSpecs(FETCH_REFSPEC) // similar to --mirror
				.setRemoveDeletedRefs(true)
				.setCheckFetchedObjects(true)
				.setCredentialsProvider(settings.getCredentialsProvider())
				.setTimeout(settings.getTimeoutInSeconds())
				.setTransportConfigCallback(settings.getTransportConfigCallback())
				.call();
		LOGGER.info("git fetch result: {}", fetchResult.getTrackingRefUpdates());
		return git;
	}

	protected Git gitClone(GitSettings settings) throws GitAPIException, IOException {
		// we do not directly git clone in order to customize git config before remote operations
		Git git = gitInit(settings);
		return gitFetch(git, settings);
	}

	protected Git gitInit(GitSettings settings) throws GitAPIException, IOException {
		LOGGER.debug("initializing a new git repository: {}", settings);
		Git git = Git.init()
				.setDirectory(settings.getLocalDir())
				.setBare(true)
				.call();
		// updating the configuration here is only for convenience if used manually
		StoredConfig config = git.getRepository()
				.getConfig();
		config.setString(ConfigConstants.CONFIG_REMOTE_SECTION, Constants.DEFAULT_REMOTE_NAME, ConfigConstants.CONFIG_KEY_URL,
				settings.getRemoteUrl());
		config.setString(ConfigConstants.CONFIG_REMOTE_SECTION, Constants.DEFAULT_REMOTE_NAME, ConfigConstants.CONFIG_FETCH_SECTION,
				FETCH_REFSPEC);
		config.save();
		return git;
	}

	protected RevCommit getRevCommit(Git git, GitSettings settings) {
		final Repository repository = git.getRepository();
		final ObjectId objectId;
		try {
			objectId = repository.resolve(settings.getGitRevision());
		} catch (Exception e) {
			throw new ConfijSourceException("failed to git resove revision {} ({})", settings.getGitRevision(), settings, e);
		}
		if (objectId == null) {
			throw new ConfijSourceException("unable to git resove revision {} ({})", settings.getGitRevision(), settings);
		}
		try {
			return repository.parseCommit(objectId);
		} catch (IOException e) {
			throw new ConfijSourceException("git failed to find commit for objectId {} ({})", objectId, settings, e);
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
				throw new ConfijSourceException("File {} not found within git repo at commit {}", settings.getConfigFile(), revCommit);
			}
		} catch (IOException e) {
			throw new ConfijSourceException("failed to read file {} within git repo at commit {}", settings.getConfigFile(), revCommit, e);
		}
	}
}
