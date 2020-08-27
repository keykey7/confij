package ch.kk7.confij.pipeline;

import ch.kk7.confij.binding.BindingResult;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.pipeline.reload.ReloadNotifierImpl;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.tree.ConfijNode;
import ch.kk7.confij.tree.NodeDefinition;
import ch.kk7.confij.validation.ConfijValidator;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.List;

@Value
@NonFinal
public class ConfijPipelineImpl<T> implements ConfijPipeline<T> {
	@NonNull List<ConfijSource> sources;
	@NonNull ConfijSource defaultSource;
	@NonNull ConfijValidator<T> validator;
	@NonNull ConfigBinding<T> configBinding;
	@NonNull NodeDefinition format;
	@NonNull ReloadNotifierImpl<T> reloadNotifier;

	protected ConfijNode newDefaultConfig() {
		ConfijNode defaultsOnly = ConfijNode.newRootFor(format);
		defaultSource.override(defaultsOnly);
		return defaultsOnly;
	}

	protected ConfijNode readConfigToNode() {
		ConfijNode rootNode = newDefaultConfig();
		for (ConfijSource source : sources) {
			source.override(rootNode);
			// always overriding with default source to make sure new
			// (optional) branches are filled with default values before
			// the next source might reference it...
			defaultSource.override(rootNode);
		}
		return rootNode;
	}

	protected BindingResult<T> bind(ConfijNode rootNode) {
		return configBinding.bind(rootNode);
	}

	@Override
	public T build() {
		ConfijNode simpleConfig = readConfigToNode();
		BindingResult<T> bindingResult = bind(simpleConfig);
		validator.validate(bindingResult);
		reloadNotifier.configReloaded(bindingResult);
		return bindingResult.getValue();
	}
}
