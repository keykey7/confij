package ch.kk7.config4j.pipeline;

import ch.kk7.config4j.annotation.Default;
import ch.kk7.config4j.annotation.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.collection.IsMapWithSize.anEmptyMap;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class MitAllesUndScharfTest {

	@Nullable
	public interface MitAllesUndScharf {
		Primitives primitives();

		ExtendsGeneric extendsGeneric();

		Collections collections();

		Maps maps();
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

		List<Set<Collection<Integer>>> listSetCollectionInteger();
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

	@Test
	public void canInstantiateEmpty() {
		MitAllesUndScharf allDefaults = Config4jBuilder.of(MitAllesUndScharf.class)
				.build();
		Primitives primitives = allDefaults.primitives();
		assertThat(primitives, notNullValue());
		assertThat(primitives.aBoolean(), is(false));
		assertThat(primitives.aString(), nullValue());
		assertThat(primitives.aChar(), is('\0'));

		Collections collections = allDefaults.collections();
		assertThat(collections.setSetString(), empty());
		assertThat(collections.setPrimitives(), empty());

		Maps maps = allDefaults.maps();
		assertThat(maps.mapStringMapStringString(), anEmptyMap());
	}

	@Test
	public void canInstantiateDefaults() {
		MitAllesUndScharf allDefaults = Config4jBuilder.of(MitAllesUndScharf.class)
				.build();
		Primitives primitives = allDefaults.primitives();
		assertThat(primitives.anInt(), is(42));
		assertThat(primitives.aLong(), is(1337L));
		assertThat(primitives.aByte(), is((byte) 100));

		Maps maps = allDefaults.maps();
		assertThat(maps.mapStringString(), aMapWithSize(1));
		assertThat(maps.mapStringString(), hasEntry("key", "value" + maps.hashCode()));
	}
}
