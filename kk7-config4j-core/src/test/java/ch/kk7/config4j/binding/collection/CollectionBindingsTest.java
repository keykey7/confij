package ch.kk7.config4j.binding.collection;

import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.binding.ConfigBinder;
import ch.kk7.config4j.common.Config4jException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CollectionBindingsTest {
	private CollectionBindingFactory collectionFactory = new CollectionBindingFactory();
	private ConfigBinder configBinder = new ConfigBinder();

	private interface CustomSet<T extends String> extends Set<T> {
	}

	@SuppressWarnings("unused")
	private interface ValidCollections {
		Set<Integer> integerSet();

		Set<Set<Long>> longSetSet();

		Collection<Long> longCollection();

		List<Long> longList();

		Set<? extends Integer> wildcardExtendedSet();

		<T extends String> Set<T> genericExtendedSet();

		CopyOnWriteArraySet<String> notAnInterface();
	}

	@SuppressWarnings("unused")
	private interface InvalidCollections {
		Set<?> wildcardSet();

		Set rawSet();

		<T> Set<T> genericSet();

		CustomSet<String> noBuilderForAnUnmodifiableSet();
	}

	private static Stream<BindingType> toMethodStream(Class<?> clazz) {
		return Arrays.stream(clazz.getMethods())
				.map(Method::getGenericReturnType)
				.map(BindingType::newBindingType);
	}

	private static Stream<BindingType> validCollectionTypes() {
		return toMethodStream(ValidCollections.class);
	}

	private static Stream<BindingType> invalidCollectionTypes() {
		return toMethodStream(InvalidCollections.class);
	}

	@ParameterizedTest
	@MethodSource("validCollectionTypes")
	public void validSetsProduceABinding(BindingType type) {
		assertThat(collectionFactory.maybeCreate(type, configBinder)).isPresent();
	}

	@ParameterizedTest
	@MethodSource("invalidCollectionTypes")
	public void invalidSets(BindingType type) {
		assertThrows(Config4jException.class, () -> collectionFactory.maybeCreate(type, configBinder));
	}
}
