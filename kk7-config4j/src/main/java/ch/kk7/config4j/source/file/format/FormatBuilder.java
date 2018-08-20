package ch.kk7.config4j.source.file.format;

public class FormatBuilder {
	private FormatBuilder() {
	}

	public static ResourceFormat forPath(String path) {
		if (path.matches("(?s).+\\.ya?ml$")) {
			return new YamlFormat();
		}
		if (path.matches("(?s).+\\.prop(ertie)?s?$")) {
			return new PropertiesFormat();
		}
		throw FormatParsingException.unknownFormat(path);
	}
}
