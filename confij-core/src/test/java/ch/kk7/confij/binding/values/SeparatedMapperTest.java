package ch.kk7.confij.binding.values;

import ch.kk7.confij.binding.values.SeparatedMapper.Separated;
import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.source.env.PropertiesSource;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

class SeparatedMapperTest implements WithAssertions {

	interface Stringings {
		@Separated
		List<String> listOfStrings();

		@Separated
		String[] stringArray();

		@Separated
		Set<String> setOfStrings();

		String notAnnotated();
	}

	private void assertThatStringingsAll(String source, String[] expected) {
		Stringings stringings = ConfijBuilder.of(Stringings.class)
				.loadFrom(new PropertiesSource().with("listOfStrings", source)
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
		@Separated
		int[] intArray();

		@Separated
		HashSet<Long> longHashSet();
	}

	@Test
	public void testOtherValidTypes() {
		OtherValidTypes otherValidTypes = ConfijBuilder.of(OtherValidTypes.class)
				.loadFrom(new PropertiesSource().with("intArray", "0,1,2,3,4,5")
						.with("longHashSet", "0"))
				.build();
		assertThat(otherValidTypes.intArray()).containsExactly(0, 1, 2, 3, 4, 5);
		assertThat(otherValidTypes.longHashSet()).containsOnly(0L);
	}

	@Test
	public void unmappableValue() {
		assertThatThrownBy(() -> ConfijBuilder.of(OtherValidTypes.class)
				.loadFrom(new PropertiesSource().with("intArray", "0 ,1,2,3,4,5")
						.with("longHashSet", "0"))
				.build());
	}

	interface InvalidMap {
		@Separated
		Map<String, String> aMap();
	}

	@Test
	public void invalidMap() {
		assertThatThrownBy(() -> ConfijBuilder.of(InvalidMap.class)
				.build());
	}

	interface InvalidNonList {
		@Separated
		Stream notACollectionOrArray();
	}

	@Test
	public void notACollectionOrArray() {
		assertThatThrownBy(() -> ConfijBuilder.of(InvalidNonList.class)
				.build());
	}

	interface CustomAnnotation {
		@Separated(separator = ";")
		String[] semicolon();

		@Separated(separator = ";", trim = true)
		String[] semicolonTrimmed();

		@Separated(separator = "[A-Z]+", trim = true)
		int[] intTrimmed();
	}

	@Test
	public void customAnnotation() {
		CustomAnnotation customAnnotation = ConfijBuilder.of(CustomAnnotation.class)
				.loadFrom(new PropertiesSource().with("semicolon", "a, b ;c;; ")
						.with("semicolonTrimmed", "a, b;c;; ")
						.with("intTrimmed", "1 SPLIT 2 SPLUNK 3"))
				.build();
		assertThat(customAnnotation.semicolon()).containsExactly("a, b ", "c", "", " ");
		assertThat(customAnnotation.semicolonTrimmed()).containsExactly("a, b", "c", "", "");
		assertThat(customAnnotation.intTrimmed()).containsExactly(1, 2, 3);
	}
}
