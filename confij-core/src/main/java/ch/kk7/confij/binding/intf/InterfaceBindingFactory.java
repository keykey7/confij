package ch.kk7.confij.binding.intf;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBindingFactory;
import ch.kk7.confij.common.ConfijException;
import com.fasterxml.classmate.ResolvedType;

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
				throw new ConfijException("circular interface definition: {}: cannot add another {}", stackAsString(), type);
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
