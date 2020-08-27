package ch.kk7.confij.binding.intf;

import ch.kk7.confij.annotation.Key;
import ch.kk7.confij.binding.BindingContext;
import ch.kk7.confij.binding.BindingResult;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.common.AnnotationUtil;
import ch.kk7.confij.tree.ConfijNode;
import ch.kk7.confij.tree.NodeBindingContext;
import ch.kk7.confij.tree.NodeDefinition.NodeDefinitionMap;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.types.ResolvedInterfaceType;
import lombok.ToString;
import lombok.Value;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@ToString
public class InterfaceBinding<T> implements ConfigBinding<T> {
	private final Map<String, AttributeInformation> siblingsByName;
	private final InterfaceProxyBuilder<T> interfaceBuilder;

	public InterfaceBinding(BindingType bindingType, ConfigBinder configBinder) {
		ResolvedType baseType = bindingType.getResolvedType();
		if (!(baseType instanceof ResolvedInterfaceType)) {
			throw new IllegalArgumentException("expected type " + baseType + " to be a " + ResolvedInterfaceType.class);
		}
		// resolve context first for class (annotations that hold for the whole class)
		BindingContext classBindingContext = bindingType.getBindingContext()
				.settingsFor(baseType.getErasedType(), false);
		siblingsByName = new LinkedHashMap<>();
		interfaceBuilder = new InterfaceProxyBuilder<>((ResolvedInterfaceType) baseType);
		for (ResolvedMethod method : interfaceBuilder.getAllowedMethods()) {
			BindingType methodBindingType = bindingType.bindingFor(method.getReturnType(),
					classBindingContext.settingsFor(method.getRawMember(), true));
			ConfigBinding<?> methodDescription = configBinder.toConfigBinding(methodBindingType);
			String configKey = AnnotationUtil.findAnnotation(method.getRawMember(), Key.class)
					.map(Key::value)
					.orElse(method.getName());
			siblingsByName.put(configKey, new AttributeInformation(methodDescription, method));
		}
	}

	@Override
	public NodeDefinitionMap describe(NodeBindingContext nodeBindingContext) {
		NodeBindingContext settingsForThisClass = nodeBindingContext.settingsFor(interfaceBuilder.getType()
				.getErasedType());
		// TODO: support a special method like: @UnknownValues Map<String,Object> unknowns;
		return NodeDefinitionMap.fixedKeysMap(settingsForThisClass, siblingsByName.entrySet()
				.stream()
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue()
						.getDescription()
						.describe(settingsForThisClass.settingsFor(e.getValue()
								.getMethod()
								.getRawMember())))));
	}

	@Override
	public BindingResult<T> bind(ConfijNode config) {
		Map<String, ConfijNode> childConfigs = config.getChildren();
		List<BindingResult<?>> bindingResultChildren = new ArrayList<>();
		InterfaceProxyBuilder<T>.ValidatingProxyBuilder proxyBuilder = interfaceBuilder.builder();
		siblingsByName.forEach((key, siblingDescription) -> {
			BindingResult<?> siblingBindingResult = siblingDescription.getDescription()
					.bind(childConfigs.get(key));
			Object siblingValue = siblingBindingResult.getValue();
			// TODO: what does it mean when a siblingValue is null/empty for a default method? should it be called or simply return null?
			//       -> maybe make the behavior configurable. For now the default method is called for null end empty collection-likes.
			ResolvedMethod method = siblingDescription.getMethod();
			if (method.getRawMember()
					.isDefault() && isEmpty(siblingValue)) {
				return;
			}
			proxyBuilder.methodToValue(method, siblingValue);
			bindingResultChildren.add(siblingBindingResult);
		});
		T result = proxyBuilder.build();
		return BindingResult.of(result, config, bindingResultChildren);
	}

	protected boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof Collection) {
			return ((Collection) value).isEmpty();
		}
		if (value instanceof Map) {
			return ((Map) value).isEmpty();
		}
		if (value.getClass()
				.isArray()) {
			return Array.getLength(value) == 0;
		}
		return false;
	}

	@Value
	public static class AttributeInformation {
		private final ConfigBinding<?> description;
		private final ResolvedMethod method;
	}
}
