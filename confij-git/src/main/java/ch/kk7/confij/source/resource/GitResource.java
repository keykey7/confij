package ch.kk7.confij.source.resource;

import ch.kk7.confij.common.Util;
import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.source.any.ConfijAnyResource;
import ch.kk7.confij.template.ValueResolver.StringResolver;
import com.google.auto.service.AutoService;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;
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
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static ch.kk7.confij.common.Util.not;

@Value
@NonFinal
public class GitResource implements ConfijResource {
	protected static final String TEMP_DIR_PREFIX = "confij-";
	protected static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(GitResource.class);
	private static final String FETCH_REFSPEC = "+refs/*:refs/*";
	GitSettings gitSettings;

	@Override
	public Stream<ResourceContent> read(StringResolver resolver) {
		// TODO: support templating for all settings
		GitSettings settings = gitSettings;
		if (gitSettings.getLocalDir() == null) {
			settings = settings.withLocalDir(getFileForSeed(settings.getRemoteUrl()));
		}
		Git git = gitCloneOrFetch(settings);
		return Stream.of(readFile(git, settings));
	}

	public static File getFileForSeed(@NonNull String seed) {
		// a more stable idea would be to use the SHA hash of the first git commit instead
		// but that's a bit of a chicken egg-issue...
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

	protected Git gitFetch(Git git, GitSettings settings) throws GitAPIException {
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
		StoredConfig config = git.getRepository()
				.getConfig();
		// updating the configuration here is mainly for convenience if used manually
		config.setString(ConfigConstants.CONFIG_REMOTE_SECTION, Constants.DEFAULT_REMOTE_NAME, ConfigConstants.CONFIG_KEY_URL,
				settings.getRemoteUrl());
		config.setString(ConfigConstants.CONFIG_REMOTE_SECTION, Constants.DEFAULT_REMOTE_NAME, ConfigConstants.CONFIG_FETCH_SECTION,
				FETCH_REFSPEC);
		Optional.ofNullable(settings.getGitInitHook())
				.ifPresent(x -> x.accept(git));
		config.save(); // save after init hook
		return git;
	}

	protected RevCommit getRevCommit(Git git, GitSettings settings) {
		final Repository repository = git.getRepository();
		final ObjectId objectId;
		try {
			objectId = repository.resolve(settings.getGitRevision());
		} catch (Exception e) {
			throw new ConfijSourceException("failed to git resove an objectId from rev {} ({})", settings.getGitRevision(), settings, e);
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

	protected ResourceContent readFile(Git git, GitSettings settings) {
		RevCommit revCommit = getRevCommit(git, settings);
		try (TreeWalk walk = TreeWalk.forPath(git.getRepository(), settings.getConfigFile(), revCommit.getTree())) {
			if (walk != null) {
				ObjectId objectId = walk.getObjectId(0);
				byte[] bytes = git.getRepository()
						.open(objectId)
						.getBytes();
				String content = new String(bytes, Defaults.CHARSET_NAME); // TODO: make charset configurable
				String filePathAndSha = settings.getConfigFile() + "#" + objectId.getName();
				return new ResourceContent(content, filePathAndSha);
			} else {
				throw new ConfijSourceException("File {} not found within git repo at commit {}", settings.getConfigFile(), revCommit);
			}
		} catch (IOException e) {
			throw new ConfijSourceException("failed to read file {} within git repo at commit {}", settings.getConfigFile(), revCommit, e);
		}
	}

	@ToString
	@AutoService(ConfijAnyResource.class)
	public static class GitAnyResource implements ConfijAnyResource {
		private static final String SCHEME = "git";
		private static final Pattern URL_FILE_SPLITTER = Pattern.compile("^(?<url>.+(?:\\.git/?|[^:]/))/(?<file>.+)$");

		@Override
		public Optional<GitResource> maybeHandle(String path) {
			if (Util.getScheme(path)
					.filter(SCHEME::equals)
					.isPresent()) {
				GitSettings settings = uriToGitSettings(path);
				return Optional.of(new GitResource(settings));
			}
			return Optional.empty();
		}

		protected GitSettings uriToGitSettings(String pathTemplate) {
			String urlAndFile = Util.getSchemeSpecificPart(pathTemplate);
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
					.gitRevision(Util.getFragment(pathTemplate)
							.filter(not(String::isEmpty))
							.orElse(Constants.HEAD))
					.remoteUrl(remoteUrl)
					.configFile(configFile)
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
	}

	@Value
	@With
	@Builder
	@NonFinal
	public static class GitSettings {
		@NonNull String remoteUrl;
		@NonNull String configFile;
		@Builder.Default
		@NonNull String gitRevision = Constants.HEAD;
		File localDir;
		CredentialsProvider credentialsProvider;
		@Builder.Default
		int timeoutInSeconds = 60;
		TransportConfigCallback transportConfigCallback;
		Consumer<Git> gitInitHook;

		public static class GitSettingsBuilder {
			GitSettingsBuilder usernamePasswordCredential(String username, String password) {
				return credentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
			}
		}
	}
}
