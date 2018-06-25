package ch.kk7.config4j.source;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class SimpleConfigTest {
	@Test
	public void asd() {
		URI uri = URI.create("config:/")
				.resolve("ä«♠;hm/");
		System.out.println(uri);
		System.out.println(uri.getScheme());
		System.out.println(uri.getSchemeSpecificPart());
		System.out.println(uri.getRawSchemeSpecificPart());
	}
}
