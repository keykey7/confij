package ch.kk7.confij.source.resource;

import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.source.resource.ConfijResource.ResourceContent;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

class ClasspathResourceTest implements WithAssertions {

	@Test
	public void rootName() {
		assertThat(ClasspathResource.ofName("file1.txt")
				.read()).hasSize(1)
				.element(0)
				.extracting(ResourceContent::getContent)
				.isEqualTo("111\n");
		assertThat(ClasspathResource.ofName("file1.txt")
				.relativeTo(ClassLoader.getSystemClassLoader())
				.read()).hasSize(1)
				.element(0)
				.extracting(ResourceContent::getContent)
				.isEqualTo("111\n");
		assertThat(ClasspathResource.ofName("ch/kk7/confij/source/resource/file2.txt")
				.read()).hasSize(1)
				.element(0)
				.extracting(ResourceContent::getContent)
				.isEqualTo("222\n");
	}

	@Test
	public void relativeToClass() {
		assertThat(ClasspathResource.ofName("/file1.txt")
				.relativeTo(ClasspathResource.class)
				.read()).hasSize(1)
				.element(0)
				.extracting(ResourceContent::getContent)
				.isEqualTo("111\n");
		assertThat(ClasspathResource.ofName("file2.txt")
				.relativeTo(ClasspathResource.class)
				.read()).hasSize(1)
				.element(0)
				.extracting(ResourceContent::getContent)
				.isEqualTo("222\n");
	}

	@Test
	public void nonexistent() {
		assertThatThrownBy(() -> ClasspathResource.ofName("non3xistent.fuu")
				.read()).isInstanceOf(ConfijSourceException.class)
				.hasMessageContaining("non3xistent.fuu");
	}
}
