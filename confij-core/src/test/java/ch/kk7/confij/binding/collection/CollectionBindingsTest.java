package ch.kk7.confij.binding.collection;

import ch.kk7.confij.binding.BindingContext;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.values.ValueMapperFactory;
import ch.kk7.confij.common.ConfijException;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

class CollectionBindingsTest implements WithAssertions {
	private CollectionBindingFactory collectionFactory = new CollectionBindingFactory();
	private ConfigBinder configBinder = new ConfigBinder();

	private interface CustomSet<T extends String> extends Set<T> {
	}

	public static class NoEmptyConstructor extends ArrayList<String> {
		public NoEmptyConstructor(String whatever) {
			// noop
		}
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

		NoEmptyConstructor noEmptyConstructor();
	}

	private static Stream<BindingType> toMethodStream(Class<?> clazz) {
		return Arrays.stream(clazz.getMethods())
				.sorted(Comparator.comparing(Method::getName))
				.map(Method::getGenericReturnType)
				.map(x -> BindingType.newBindingType(x,
						BindingContext.newDefaultContext(ValueMapperFactory.defaultFactories())));
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
		assertThatThrownBy(() -> collectionFactory.maybeCreate(type, configBinder)).isInstanceOf(ConfijException.class);
	}
}
