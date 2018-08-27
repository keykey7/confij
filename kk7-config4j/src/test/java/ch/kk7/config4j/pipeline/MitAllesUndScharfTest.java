package ch.kk7.config4j.pipeline;

import ch.kk7.config4j.annotation.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		int anInt();

		long aLong();

		float aFloat();

		double aDouble();

		boolean aBoolean();

		String aString();

		char aChar();

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
		Map<String, String> mapStringString();

		Map<String, Generic<String>> mapStringGenericString();

		Map<String, Map<String, String>> mapStringMapStringString();
	}

	@Test
	public void canInstantiate() {
		MitAllesUndScharf allDefaults = Config4jBuilder.of(MitAllesUndScharf.class)
				.build();
		Primitives primitives = allDefaults.primitives();
		assertThat(primitives, notNullValue());
		assertThat(primitives.aBoolean(), is(false));
		assertThat(primitives.aString(), nullValue());
		assertThat(primitives.aChar(), is('\0'));
	}
}
