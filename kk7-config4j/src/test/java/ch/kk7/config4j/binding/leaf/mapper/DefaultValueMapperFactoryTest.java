package ch.kk7.config4j.binding.leaf.mapper;

import com.fasterxml.classmate.TypeResolver;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultValueMapperFactoryTest {

	private TypeResolver typeResolver = new TypeResolver();

	private DefaultValueMapperFactory mapperFactory = new DefaultValueMapperFactory();

	@SuppressWarnings("unchecked")
	private <T> void assertMapping(Class<T> forClass, T expected, String from) {
		T actual = (T) mapperFactory.maybeForType(typeResolver.resolve(forClass))
				.get()
				.fromString(from);
		assertEquals(expected, actual);
	}

	@Test
	void viaFromStringMethod() {
		UUID expected = UUID.randomUUID();
		assertMapping(UUID.class, expected, expected.toString());
	}

	@Test
	void viaValueOfMethod() {
		TestEnum expected = TestEnum.Two;
		assertMapping(TestEnum.class, expected, expected.name());
	}

	@Test
	void viaConstructor() {
		TestClass expected = new TestClass(UUID.randomUUID()
				.toString());
		assertMapping(TestClass.class, expected, expected.getString());
	}

	enum TestEnum {
		One,
		Two,
		Three
	}

	private static final class TestClass {
		private final String string;

		public TestClass(String fromString) {
			this.string = Objects.requireNonNull(fromString);
		}

		public String getString() {
			return string;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof TestClass)) {
				return false;
			}
			TestClass testClass = (TestClass) o;
			return string.equals(testClass.string);
		}

		@Override
		public int hashCode() {
			return string.hashCode();
		}
	}
}
