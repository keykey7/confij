package ch.kk7.confij.binding.array;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.binding.collection.CollectionUtil;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatList;
import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.source.simple.ConfijNode;
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
	public Object bind(ConfijNode config) {
		// TODO: add config to allow null values in array
		List<ConfijNode> childNodes = CollectionUtil.childrenAsContinuousList(config);
		Object result = Array.newInstance(componentType.getErasedType(), childNodes.size());
		int i = 0;
		for (ConfijNode childNode : childNodes) {
			Array.set(result, i++, componentDescription.bind(childNode));
		}
		return result;
	}
}
