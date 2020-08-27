package ch.kk7.confij.source;

public abstract class ConfijSourceTestBase {
	public interface ConfigX {
		String x();
	}

	public static ConfijSource noop = x -> {
	};
	public static ConfijSource alwaysFail = first -> {
		throw new RuntimeException("fuuu");
	};
	public static ConfijSource setThenFail = first -> {
		setXTo("FAIL").override(first);
		alwaysFail.override(first);
	};

	public static ConfijSource setXTo(String value) {
		return rootNode -> rootNode.getChildren()
				.get("x")
				.setValue(value);
	}
}
