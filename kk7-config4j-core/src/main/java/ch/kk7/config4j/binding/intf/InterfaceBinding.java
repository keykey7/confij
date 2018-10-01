package ch.kk7.config4j.binding.intf;

import ch.kk7.config4j.annotation.Key;
import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.binding.ConfigBinder;
import ch.kk7.config4j.binding.ConfigBinding;
import ch.kk7.config4j.binding.ConfigBinding.BindResult.BindResultBuilder;
import ch.kk7.config4j.common.AnnotationUtil;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatMap;
import ch.kk7.config4j.format.FormatSettings;
import ch.kk7.config4j.source.simple.SimpleConfig;
import ch.kk7.config4j.source.simple.SimpleConfigMap;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.types.ResolvedInterfaceType;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class InterfaceBinding<T> implements ConfigBinding<T> {
	private final Map<String, AttributeInformation> siblingsByName;
	private final InterfaceInvocationHandler<T> interfaceHandler;
	private final T publicInstance;

	public InterfaceBinding(BindingType bindingType, ConfigBinder configBinder) {
		ResolvedType baseType = bindingType.getResolvedType();
		if (!(baseType instanceof ResolvedInterfaceType)) {
			throw new IllegalArgumentException("expected type " + baseType + " to be a " + ResolvedInterfaceType.class);
		}
		siblingsByName = new HashMap<>();
		interfaceHandler = new InterfaceInvocationHandler<>((ResolvedInterfaceType) baseType);
		for (ResolvedMethod method : interfaceHandler.getSupportedMethods()) {
			BindingType methodBindingType = bindingType.bindingFor(method.getReturnType(), bindingType.getBindingSettings()
					.settingsFor(method.getRawMember()));
			ConfigBinding<?> methodBinding = configBinder.toConfigBinding(methodBindingType);
			String configKey = AnnotationUtil.findAnnotation(method.getRawMember(), Key.class).map(Key::value)
					.orElse(method.getName());
			siblingsByName.put(configKey, new AttributeInformation(methodBinding, method));
		}
		publicInstance = interfaceHandler.instance();
	}

	@Override
	public ConfigFormatMap describe(FormatSettings formatSettings) {
		FormatSettings settingsForThisClass = formatSettings.settingsFor(interfaceHandler.getType()
				.getErasedType());
		// TODO: support a special method like: @UnknownValues Map<String,Object> unknowns;
		return ConfigFormatMap.fixedKeysMap(settingsForThisClass, siblingsByName.entrySet()
				.stream()
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue()
						.getDescription()
						.describe(settingsForThisClass.settingsFor(e.getValue()
								.getMethod()
								.getRawMember())))));
	}

	@Override
	public BindResult<T> bind(SimpleConfig config) {
		if (!(config instanceof SimpleConfigMap)) {
			throw new IllegalStateException("expected a config map, but got: " + config);
		}
		Map<String, SimpleConfig> configMap = ((SimpleConfigMap) config).map();

		// FIXME: interface handler should be immutable
		BindResultBuilder<T> resultBuilder = BindResult.builder();
		interfaceHandler.clear();
		siblingsByName.forEach((key, siblingDescription) -> {
			BindResult<?> siblingBindResult = siblingDescription.getDescription()
					.bind(configMap.get(key));
			interfaceHandler.setMethod(siblingDescription.getMethod(), siblingBindResult.getValue());
			resultBuilder.sibling(key, siblingBindResult);
		});
		return resultBuilder.value(publicInstance)
				.build();
	}

	public static class AttributeInformation {
		private final ConfigBinding<?> description;
		private final ResolvedMethod method;

		public AttributeInformation(ConfigBinding<?> description, ResolvedMethod method) {
			this.description = description;
			this.method = method;
		}

		public ConfigBinding<?> getDescription() {
			return description;
		}

		public ResolvedMethod getMethod() {
			return method;
		}
	}
}
