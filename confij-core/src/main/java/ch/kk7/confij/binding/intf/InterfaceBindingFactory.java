package ch.kk7.confij.binding.intf;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBindingFactory;
import ch.kk7.confij.binding.ConfijDefinitionException;
import com.fasterxml.classmate.ResolvedType;
import lombok.ToString;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

@ToString
public class InterfaceBindingFactory implements ConfigBindingFactory<InterfaceBinding> {
	private final Deque<ResolvedType> callStack = new LinkedList<>();

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
				throw new ConfijDefinitionException("circular interface definition: {}: cannot add another {}", stackAsString(), type);
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
