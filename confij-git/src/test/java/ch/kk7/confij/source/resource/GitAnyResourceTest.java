package ch.kk7.confij.source.resource;

import org.assertj.core.api.WithAssertions;

class GitAnyResourceTest implements WithAssertions {

//	@Test
//	void notGitScheme() {
//		assertThat(git.canHandle(fileUri)).isTrue();
//		assertThat(git.canHandle(ConfijAnySource.URIish.create("someUri"))).isFalse();
//		assertThat(git.canHandle(ConfijAnySource.URIish.create("http://example.com/bla.git"))).isFalse();
//	}

//	@ParameterizedTest
//	@ValueSource(strings = {"http://example.com/not/git/scheme.git/file.txt", // not git scheme
//			"git://example.com/missing.git/host.yaml", // no real host
//			"git:http://example.com/dunno/where/file/starts.yaml", //no url/file separation
//			"", // kinda empty
//			".", // still kinda empty
//			"..", // you're kidding me right?
//	})
//	void notGitUri(String invalidUri) {
//		ConfijAnySource.URIish uri = ConfijAnySource.URIish.create(invalidUri);
//		assertThatThrownBy(() -> gitRead(uri)).isInstanceOf(ConfijSourceException.class);
//	}
}
