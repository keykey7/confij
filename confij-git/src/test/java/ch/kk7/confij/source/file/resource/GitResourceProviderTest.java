package ch.kk7.confij.source.file.resource;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

class GitResourceProviderTest implements WithAssertions {
	@Test
	public void uri() {
		URI uri = URI.create("git:https://somewhere/bla.git:somefile.properties?asd#asdxxxasdss");
		System.out.println(uri.getScheme());
		System.out.println(uri.getSchemeSpecificPart());
		System.out.println(uri.getFragment());
	}

	@Test
	public void asd() {
		GitResourceProvider git = new GitResourceProvider();
		git.setUri("https://github.com/github/testrepo.git");
		git.setConfigFile("test/alias.c");
		System.out.println(git.read(null));
	}
}
