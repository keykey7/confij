package ch.kk7.confij.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.binding.values.Base64Mapper.Base64;
import ch.kk7.confij.binding.values.DateTimeMapper.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MitAllesUndScharfTest {
	public interface MitAllesUndScharf {
		Primitives primitives();

		ExtendsGeneric extendsGeneric();

		Collections collections();

		Maps maps();

		Arrays arrays();

		Dates dates();
	}

	public interface Primitives {
		default int anInt() {
			return 42;
		}

		@Default("1337")
		long aLong();

		float aFloat();

		double aDouble();

		boolean aBoolean();

		String aString();

		char aChar();

		@Default("100")
		byte aByte();
	}

	public interface Generic<T> {
		T aT();
	}

	public interface ExtendsGeneric extends Generic<Integer> {
		Generic<Double> anotherT();
	}

	public interface Collections {
		Set<String> setString();

		Set<Set<String>> setSetString();

		Set<Primitives> setPrimitives();

		Set<Generic<String>> setGenericString();

		SortedSet<Integer> sortedSet();

		List<Set<Collection<Integer>>> listSetCollectionInteger();

		HashSet<String> hashSet();
	}

	public interface Maps {
		default Map<String, String> mapStringString() {
			Map<String, String> result = new HashMap<>();
			result.put("key", "value" + this.hashCode());
			return result;
		}

		Map<String, Generic<String>> mapStringGenericString();

		Map<String, Map<String, String>> mapStringMapStringString();
	}

	public interface Arrays {
		int[] anIntArray();

		default byte[] aDefaultByteArray() {
			return new byte[]{2, 2, 2};
		}

		@Base64
		@Default("AQID")
		byte[] aBase64ByteArray();

		String[] aStringArray();

		Set<String>[] aSetArray();

		byte[][] a2DByteArray();

		Primitives[] anInterfaceArray();
	}

	public interface Dates {

		@Default("2001-12-14T21:59:43Z")
		Date date();

		@Default("2001-12-14T21:59:43.01Z")
		Instant instant();

		@Default("10:15:30.01")
		LocalTime localTime();

		@Default("2001-12-14")
		LocalDate localDate();

		@Default("2001-12-14T21:59:43.01")
		LocalDateTime localDateTime();

		@Default("2001-12-14T21:59:43.01-05:00")
		OffsetDateTime offsetDateTime();

		@Default("2001-12-14T21:59:43.01+01:00[Europe/Paris]")
		ZonedDateTime zonedDateTime();

		@DateTime(value = "MMM-dd-uuuu")
		@Default("Dec-14-2001")
		LocalDate localDateWithFormat();
	}

	private MitAllesUndScharf instance;

	@BeforeEach
	void build() {
		instance = ConfijBuilder.of(MitAllesUndScharf.class)
				.build();
	}

	@Test
	void canInstantiateEmpty() {
		Primitives primitives = instance.primitives();
		assertThat(primitives).isNotNull();
		assertThat(primitives.aBoolean()).isFalse();
		assertThat(primitives.aString()).isNull();
		assertThat(primitives.aChar()).isEqualTo('\0');

		Collections collections = instance.collections();
		assertThat(collections.setSetString()).isEmpty();
		assertThat(collections.setPrimitives()).isEmpty();

		Maps maps = instance.maps();
		assertThat(maps.mapStringMapStringString()).isEmpty();

		Arrays arrays = instance.arrays();
		assertThat(arrays.anIntArray()).isEmpty();
	}

	@Test
	void canInstantiateDefaults() {
		Primitives primitives = instance.primitives();
		assertThat(primitives.anInt()).isEqualTo(42);
		assertThat(primitives.aLong()).isEqualTo(1337L);
		assertThat(primitives.aByte()).isEqualTo((byte) 100);

		Collections collections = instance.collections();
		Set<String> strings = collections.setString();
		assertThrows(UnsupportedOperationException.class, strings::clear);
		List<Set<Collection<Integer>>> listOfStuff = collections.listSetCollectionInteger();
		assertThrows(UnsupportedOperationException.class, listOfStuff::clear);
		// but a concrete class IS modifyable...
		collections.hashSet()
				.clear();

		Maps maps = instance.maps();
		assertThat(maps.mapStringString()).hasSize(1);
		assertThat(maps.mapStringString()).hasEntrySatisfying("key", value -> assertThat(value).isEqualTo("value" + maps.hashCode()));

		Arrays arrays = instance.arrays();
		assertThat(arrays.aDefaultByteArray()).hasSize(3);
		assertThat(arrays.aBase64ByteArray()).containsExactly(1,2,3);
	}

	@Test
	@SuppressWarnings("java:S5845")
	void dateTypes() {
		assertThat(instance.dates()).satisfies(dates -> {
			assertThat(dates.date()).isEqualTo("2001-12-14T21:59:43Z");
			assertThat(dates.instant()).isEqualTo("2001-12-14T21:59:43.01Z");
			assertThat(dates.localTime()).isEqualTo("10:15:30.01");
			assertThat(dates.localDate()).isEqualTo("2001-12-14");
			assertThat(dates.localDateTime()).isEqualTo("2001-12-14T21:59:43.01");
			assertThat(dates.offsetDateTime()).isEqualTo("2001-12-14T21:59:43.01-05:00");
			assertThat(dates.zonedDateTime()).isEqualTo("2001-12-14T21:59:43.01+01:00[Europe/Paris]");
			assertThat(dates.localDateWithFormat()).isEqualTo("2001-12-14");
		});
	}
}
