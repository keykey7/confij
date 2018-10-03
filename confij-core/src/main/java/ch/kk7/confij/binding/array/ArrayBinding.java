package ch.kk7.confij.binding.array;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatList;
import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.source.simple.SimpleConfig;
import ch.kk7.confij.source.simple.SimpleConfigList;
import com.fasterxml.classmate.ResolvedType;

import java.lang.reflect.Array;
import java.util.List;

public class ArrayBinding<T> implements ConfigBinding<Object> {
	private final ResolvedType componentType;
	private final ConfigBinding<T> componentDescription;

	public ArrayBinding(BindingType bindingType, ConfigBinder configBinder) {
		componentType = bindingType.getResolvedType();
		//noinspection unchecked
		componentDescription = (ConfigBinding<T>) configBinder.toConfigBinding(bindingType);
	}

	@Override
	public ConfigFormatList describe(FormatSettings formatSettings) {
		return new ConfigFormatList(formatSettings, componentDescription.describe(formatSettings));
	}

	/**
	 * binds to Object instead of T[] since it also handles primitive arrays
	 */
	@Override
	public Object bind(SimpleConfig config) {
		if (!(config instanceof SimpleConfigList)) {
			throw new IllegalStateException("expected a config list, but got: " + config);
		}
		List<SimpleConfig> configList = ((SimpleConfigList) config).list();
		Object result = Array.newInstance(componentType.getErasedType(), configList.size());
		int i = 0;
		for (SimpleConfig configItem : configList) {
			Array.set(result, i++, componentDescription.bind(configItem));
		}
		return result;
	}
}
