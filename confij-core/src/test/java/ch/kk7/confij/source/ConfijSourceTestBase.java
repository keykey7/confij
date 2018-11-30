package ch.kk7.confij.source;

public abstract class ConfijSourceTestBase {

	public interface ConfigX {
		String x();
	}

	public static ConfigSource noop = x -> {};

	public static ConfigSource alwaysFail = first -> {
		throw new RuntimeException("fuuu");
	};

	public static ConfigSource setThenFail = first -> {
		setXTo("FAIL").override(first);
		alwaysFail.override(first);
	};

	public static ConfigSource setXTo(String value) {
		return rootNode -> rootNode.getChildren()
				.get("x")
				.setValue(value);
	}
}
