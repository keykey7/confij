package ch.kk7.config4j.source.file.format;

import ch.kk7.config4j.source.simple.SimpleConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ch.kk7.config4j.source.file.format.FormatParsingException.invalidFormat;

public class YamlFormat implements ResourceFormat {
	//	public static void main(String[] args) {
	//		YamlFormat source = new YamlFormat();
	//		Object res = source.parse("canonical: 6.8523015e+5\n" +
	//				"exponentioal: 685.230_15e+03\n" +
	//				"fixed: 685_230.15\n" +
	//				"sexagesimal: 190:20:30.15\n" +
	//				"negative infinity: -.inf\n" +
	//				"not a number: .NaN");
	//		System.out.println(res);
	//	}
	private final Yaml yaml = new Yaml(new SafeConstructor());

	@Override
	public void override(SimpleConfig simpleConfig, String content) {
		final Object root;
		// TODO: support multiple yaml in one file
		try {
			root = yaml.load(content);
		} catch (Exception e) {
			throw invalidFormat("YAML", "content cannot be parsed:\n{}", content, e);
		}
		Object simpleRoot = simplify(root);
		SimpleConfig newConfig = SimpleConfig.fromObject(simpleRoot, simpleConfig.getConfig());
		simpleConfig.overrideWith(newConfig);
	}

	public String write(Object content) {
		return yaml.dump(content);
	}

	@SuppressWarnings("unchecked")
	private Object simplify(Object yaml) {
		if (yaml instanceof Map) {
			((Map<String, Object>) yaml).replaceAll((key, value) -> simplify(value));
		} else if (yaml instanceof List) {
			((List<Object>) yaml).replaceAll(this::simplify);
		} else if (yaml != null) {
			yaml = Objects.toString(yaml);
		}
		return yaml;
	}
}
