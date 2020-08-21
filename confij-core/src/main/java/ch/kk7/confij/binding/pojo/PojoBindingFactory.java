package ch.kk7.confij.binding.pojo;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBindingFactory;
import ch.kk7.confij.binding.ConfijDefinitionException;
import com.fasterxml.classmate.ResolvedType;
import lombok.ToString;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

/**
 * The main entry point for binding the pojo related class.
 *
 * @author せいうはん
 * @version 1.0.0, 2020-05-18 02:40
 * @since 1.0.0, 2020-05-18 02:40
 */
@ToString
public class PojoBindingFactory implements ConfigBindingFactory<PojoBinding> {

	private final Deque<ResolvedType> callStack = new LinkedList<>();

	private String stackAsString() {
		return callStack.stream()
			.map(ResolvedType::getBriefDescription)
			.collect(joining("→"));
	}

	@Override
	public Optional<PojoBinding> maybeCreate(BindingType bindingType, ConfigBinder configBinder) {
		ResolvedType type = bindingType.getResolvedType();
		if (type.isConcrete()) {
			if (callStack.contains(type) || type.getSelfReferencedType() != null) {
				throw new ConfijDefinitionException("circular class definition {}: cannot add another {}", stackAsString(), type);
			}
			callStack.push(type);
			try {
				return Optional.of(new PojoBinding(bindingType, configBinder));
			} finally {
				callStack.pop();
			}
		}
		return Optional.empty();
	}
}
