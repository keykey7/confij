package ch.kk7.confij.binding.values;

import ch.kk7.confij.binding.values.CommaSeparatedMapper.CommaSeparated;
import ch.kk7.confij.pipeline.ConfijBuilder;
import ch.kk7.confij.source.env.PropertiesSource;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

class CommaSeparatedMapperTest implements WithAssertions {

	interface Stringings {
		@CommaSeparated
		List<String> listOfStrings();

		@CommaSeparated
		String[] stringArray();

		@CommaSeparated
		Set<String> setOfStrings();

		String notAnnotated();
	}

	private void assertThatStringingsAll(String source, String[] expected) {
		Stringings stringings = ConfijBuilder.of(Stringings.class)
				.withSource(new PropertiesSource().with("listOfStrings", source)
						.with("stringArray", source)
						.with("setOfStrings", source)
						.with("notAnnotated", source))
				.build();
		assertThat(stringings.listOfStrings()).containsExactly(expected);
		assertThat(stringings.stringArray()).containsExactly(expected);
		assertThat(stringings.setOfStrings()).containsOnly(expected);
		assertThat(stringings.notAnnotated()).isEqualTo(source);
	}

	@Test
	public void testValid() {
		String in = "hello,☠world ,\tyay\n\n,, ";
		assertThatStringingsAll(in, in.split(","));
	}

	@Test
	public void testEmpty() {
		assertThatStringingsAll("", new String[]{""});
	}

	@Test
	public void testNull() {
		assertThatStringingsAll(null, new String[]{});
	}

	interface OtherValidTypes {
		@CommaSeparated
		int[] intArray();

		@CommaSeparated
		HashSet<Long> longHashSet();
	}

	@Test
	public void testOtherValidTypes() {
		OtherValidTypes otherValidTypes = ConfijBuilder.of(OtherValidTypes.class)
				.withSource(new PropertiesSource().with("intArray", "0,1,2,3,4,5")
						.with("longHashSet", "0"))
				.build();
		assertThat(otherValidTypes.intArray()).containsExactly(0, 1, 2, 3, 4, 5);
		assertThat(otherValidTypes.longHashSet()).containsOnly(0L);
	}

	@Test
	public void unmappableValue() {
		assertThatThrownBy(() -> ConfijBuilder.of(OtherValidTypes.class)
				.withSource(new PropertiesSource().with("intArray", "0 ,1,2,3,4,5")
						.with("longHashSet", "0"))
				.build());
	}

	interface InvalidMap {
		@CommaSeparated
		Map<String, String> aMap();
	}

	@Test
	public void invalidMap() {
		assertThatThrownBy(() -> ConfijBuilder.of(InvalidMap.class)
				.build());
	}

	interface InvalidNonList {
		@CommaSeparated
		Stream notACollectionOrArray();
	}

	@Test
	public void notACollectionOrArray() {
		assertThatThrownBy(() -> ConfijBuilder.of(InvalidNonList.class)
				.build());
	}

	interface CustomAnnotation {
		@CommaSeparated(separator = ";")
		String[] semicolon();

		@CommaSeparated(separator = ";", trim = true)
		String[] semicolonTrimmed();

		@CommaSeparated(separator = "[A-Z]+", trim = true)
		int[] intTrimmed();
	}

	public void customAnnotation() {
		CustomAnnotation customAnnotation = ConfijBuilder.of(CustomAnnotation.class)
				.withSource(new PropertiesSource().with("semicolon", "a, b ;c;; ")
						.with("semicolonTrimmed", "a, b;c;; ")
						.with("intTrimmed", "1 SPLIT 2 SPLUNK 3"))
				.build();
		assertThat(customAnnotation.semicolon()).containsExactly("a, b", "c", "", " ");
		assertThat(customAnnotation.semicolonTrimmed()).containsExactly("a, b ", "c", "", "");
		assertThat(customAnnotation.intTrimmed()).containsExactly(1, 2, 3);
	}
}
