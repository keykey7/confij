package ch.kk7.config4j.pipeline;

import ch.kk7.config4j.annotation.Default;
import ch.kk7.config4j.binding.leaf.mapper.Base64Mapper.Base64;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MitAllesUndScharfTest {
	public interface MitAllesUndScharf {
		Primitives primitives();

		ExtendsGeneric extendsGeneric();

		Collections collections();

		Maps maps();

		Arrays arrays();
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

	@Test
	public void canInstantiateEmpty() {
		MitAllesUndScharf allDefaults = Config4jBuilder.of(MitAllesUndScharf.class)
				.build();
		Primitives primitives = allDefaults.primitives();
		assertThat(primitives).isNotNull();
		assertThat(primitives.aBoolean()).isFalse();
		assertThat(primitives.aString()).isNull();
		assertThat(primitives.aChar()).isEqualTo('\0');

		Collections collections = allDefaults.collections();
		assertThat(collections.setSetString()).isEmpty();
		assertThat(collections.setPrimitives()).isEmpty();

		Maps maps = allDefaults.maps();
		assertThat(maps.mapStringMapStringString()).isEmpty();

		Arrays arrays = allDefaults.arrays();
		assertThat(arrays.anIntArray()).isEmpty();
	}

	@Test
	public void canInstantiateDefaults() {
		MitAllesUndScharf allDefaults = Config4jBuilder.of(MitAllesUndScharf.class)
				.build();
		Primitives primitives = allDefaults.primitives();
		assertThat(primitives.anInt()).isEqualTo(42);
		assertThat(primitives.aLong()).isEqualTo(1337L);
		assertThat(primitives.aByte()).isEqualTo((byte) 100);

		Collections collections = allDefaults.collections();
		assertThrows(UnsupportedOperationException.class, () -> collections.setString()
				.clear());
		assertThrows(UnsupportedOperationException.class, () -> collections.listSetCollectionInteger()
				.clear());
		// but a concrete class IS modifyable...
		collections.hashSet()
				.clear();

		Maps maps = allDefaults.maps();
		assertThat(maps.mapStringString()).hasSize(1);
		assertThat(maps.mapStringString()).hasEntrySatisfying("key", value -> assertThat(value).isEqualTo("value" + maps.hashCode()));

		Arrays arrays = allDefaults.arrays();
		assertThat(arrays.aDefaultByteArray()).hasSize(3);
		assertThat(arrays.aBase64ByteArray()).containsExactly(1,2,3);
	}
}
