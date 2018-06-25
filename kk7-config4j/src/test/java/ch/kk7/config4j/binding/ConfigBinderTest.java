package ch.kk7.config4j.binding;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

class ConfigBinderTest {
	private static Integer randomInteger() {
		return ThreadLocalRandom.current()
				.nextInt();
	}

	private static String randomString() {
		return UUID.randomUUID()
				.toString()
				.replaceAll("-", "");
	}

	interface DirectValues {
		Integer Integer();

		String String();

		Long Long();
	}

//	@Test
//	public void directValues() {
//		ConfigBinding<DirectValues> description = new ConfigBinder().toConfigBinding(DirectValues.class);
//		Integer integer = randomInteger();
//		String string = randomString();
//		Long aLong = 7L;
//		DirectValues config = description.bind(
//				map(e("Integer", String.valueOf(integer)), e("String", string), e("Long", String.valueOf(aLong))));
//		assertEquals(integer, config.Integer());
//		assertEquals(string, config.String());
//		assertEquals(aLong, config.Long());
//	}
//
//	interface Lower {
//		String a();
//	}
//
//	interface Upper extends Lower {
//		String b();
//	}
//
//	@Test
//	public void extendingInterfaces() {
//		ConfigBinding<Upper> description = new ConfigBinder().toConfigBinding(Upper.class);
//		Upper config = description.bind(map(e("a", "aaa"), e("b", "bbb")));
//		assertEquals("aaa", config.a());
//		assertEquals("bbb", config.b());
//	}
//
//	interface Void {
//
//	}
//
//	@Test
//	public void delegatingToObj() {
//		ConfigBinding<Void> description = new ConfigBinder().toConfigBinding(Void.class);
//		Void config = description.bind(map());
//		assertEquals(config.hashCode(), config.hashCode());
//
//		ConfigBinding<Void> description2 = new ConfigBinder().toConfigBinding(Void.class);
//		Void config2 = description2.bind(map());
//		assertNotEquals(config.hashCode(), config2.hashCode());
//		assertNotEquals(config, config2);
//	}
//
//	interface SomewhatGeneric<T> {
//		T get();
//	}
//
//	interface Concrete extends SomewhatGeneric<SomewhatGeneric<String>> {
//
//	}
//
//	@Test
//	public void nestedGenericInterfaces() {
//		ConfigBinding<Concrete> description = new ConfigBinder().toConfigBinding(Concrete.class);
//		Concrete config = description.bind(map(e("list", map(e("list", "xxx")))));
//		assertEquals("xxx", config.get().get());
//	}

}
