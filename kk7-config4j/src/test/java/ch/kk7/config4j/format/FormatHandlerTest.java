package ch.kk7.config4j.format;

import ch.kk7.config4j.annotation.Default;
import ch.kk7.config4j.annotation.Nullable;

class FormatHandlerTest {
	interface XConfig {
		//		List<XComplex> complexList();
		//
		//		List<String> stringList();

		String mandatory();

		@Nullable
		String optionalNull();

		@Default("asd")
		String defaultAsd();
	}

	interface XItem {
		String item();
	}

//	@Test
//	public void awedjnas() {
//		Object rawConfig = map(e("mandatory", "m"));
//		SimpleConfig simpleConfig = SimpleConfig.fromObject(rawConfig);
//		ConfigBinding<XConfig> description = new ConfigBinder().toConfigBinding(XConfig.class);
//		ConfigFormat configFormat = description.describe(FormatSettings.newDefaultSettings());
//		new FormatHandler().complete(simpleConfig, configFormat);
//		assertEquals("m", simpleConfig.asMap()
//				.map()
//				.get("mandatory")
//				.asLeaf()
//				.get());
//		assertNull(simpleConfig.asMap()
//				.map()
//				.get("optionalNull")
//				.asLeaf()
//				.get());
//		assertEquals("asd", simpleConfig.asMap()
//				.map()
//				.get("defaultAsd")
//				.asLeaf()
//				.get());
//	}
//
//	interface XList {
//		List<XItem> complexList();
//
//		List<String> stringList();
//
//		List<List<String>> stringListList();
//	}
//
//	@Test
//	public void lists() {
//		Object rawConfig = map(e("complexList", list(map(e("item", "item1"), e("item", "item2")))),
//				e("stringList", list("str1", "str2", "str3")),
//				e("stringListList", list(list("sstr1"), list("sstr2", "sstr2"), list())));
//		SimpleConfig simpleConfig = SimpleConfig.fromObject(rawConfig);
//		ConfigBinding<XList> description = new ConfigBinder().toConfigBinding(XList.class);
//		ConfigFormat configFormat = description.describe(FormatSettings.newDefaultSettings());
//		new FormatHandler().complete(simpleConfig, configFormat);
//		System.out.println(new YamlFormat().write(simpleConfig.toObject()));
//	}
}
