package ch.kk7.confij.binding;

import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.format.ConfigFormat;
import ch.kk7.confij.source.simple.SimpleConfig;

public interface ConfigBinding<T> {
	ConfigFormat describe(FormatSettings formatSettings);

	T bind(SimpleConfig config);
}
