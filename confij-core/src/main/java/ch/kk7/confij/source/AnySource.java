package ch.kk7.confij.source;

import ch.kk7.confij.common.ServiceLoaderUtil;
import ch.kk7.confij.template.VariableResolver;
import ch.kk7.confij.tree.ConfijNode;
import lombok.ToString;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ToString
public class AnySource implements ConfigSource {
	private final List<ConfigSourceBuilder> sourceBuilders;
	private final String pathTemplate;
	private VariableResolver resolverOverride;

	public AnySource(String pathTemplate) {
		this.pathTemplate = Objects.requireNonNull(pathTemplate);
		sourceBuilders = ServiceLoaderUtil.instancesOf(ConfigSourceBuilder.class);
	}

	private VariableResolver getResolver(ConfijNode rootNode) {
		if (resolverOverride != null) {
			return resolverOverride;
		}
		return rootNode.getConfig()
				.getNodeBindingContext()
				.getVariableResolver();
	}

	public AnySource setResolver(VariableResolver resolver) {
		resolverOverride = resolver;
		return this;
	}

	@Override
	public void override(ConfijNode rootNode) {
		String actualPath = getResolver(rootNode).resolveValue(rootNode, pathTemplate);
		URI path = URI.create(actualPath);
		sourceBuilders.stream()
				.map(sb -> sb.fromURI(path))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> {
					String addon = pathTemplate.equals(actualPath) ? "" : " (resolved from '" + pathTemplate + "')";
					return new ConfijSourceException("failed to loadFrom source data from '{}'{}", path, addon);
				})
				.override(rootNode);
	}
}
