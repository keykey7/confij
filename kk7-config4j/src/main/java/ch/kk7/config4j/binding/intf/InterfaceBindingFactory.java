package ch.kk7.config4j.binding.intf;

import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.common.Config4jException;
import ch.kk7.config4j.binding.ConfigBindingFactory;
import ch.kk7.config4j.binding.ConfigBinder;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.types.ResolvedInterfaceType;

import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

public class InterfaceBindingFactory implements ConfigBindingFactory<InterfaceBinding> {
	private Stack<ResolvedType> callStack = new Stack<>();

	private String stackAsString() {
		return callStack.stream()
				.map(ResolvedType::getBriefDescription)
				.collect(Collectors.joining("→"));
	}

	@Override
	public Optional<InterfaceBinding> maybeCreate(BindingType bindingType, ConfigBinder configBinder) {
		ResolvedType type = bindingType.getResolvedType();
		if (type.isInterface()) {
			if (callStack.contains(type)) {
				throw new Config4jException("circular interface definition: " + stackAsString() + " cannot add another " + type);
			}
			callStack.push(type);
			try {
				return Optional.of(new InterfaceBinding(bindingType, configBinder));
			} finally {
				callStack.pop();
			}
		}
		return Optional.empty();
	}
}
