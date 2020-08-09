package ch.kk7.confij;

import ch.kk7.confij.binding.BindingContext;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.binding.values.ValueMapperFactory;
import ch.kk7.confij.binding.values.ValueMapperInstance;
import ch.kk7.confij.common.GenericType;
import ch.kk7.confij.common.Util;
import ch.kk7.confij.pipeline.ConfijPipeline;
import ch.kk7.confij.pipeline.ConfijPipelineImpl;
import ch.kk7.confij.pipeline.reload.ConfijReloader;
import ch.kk7.confij.pipeline.reload.ScheduledReloader;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.any.AnySource;
import ch.kk7.confij.source.defaults.DefaultSource;
import ch.kk7.confij.source.logical.MaybeSource;
import ch.kk7.confij.source.logical.OrSource;
import ch.kk7.confij.template.NoopValueResolver;
import ch.kk7.confij.template.ValueResolver;
import ch.kk7.confij.tree.NodeBindingContext;
import ch.kk7.confij.tree.NodeDefinition;
import ch.kk7.confij.validation.ConfijValidator;
import ch.kk7.confij.validation.NonNullValidator;
import ch.kk7.confij.validation.ServiceLoaderValidator;
import com.fasterxml.classmate.ResolvedType;
import lombok.NonNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfijBuilder<T> {
	private final Type forType;
	private final List<ConfijSource> sources = new ArrayList<>();
	private ConfijValidator validator = null;
	private NodeBindingContext nodeBindingContext = null;
	private BindingContext bindingContext = null;
	private ConfijReloader<T> reloader = null;

	protected ConfijBuilder(@NonNull Type forType) {
		this.forType = forType;
	}

	/**
	 * The most common constructor to create a new configuration
	 * builder of given type.
	 *
	 * @param forClass the configuration instance class
	 * @param <X>      the configuration instance type
	 * @return a ConfiJ builder
	 */
	public static <X> ConfijBuilder<X> of(Class<X> forClass) {
		return new ConfijBuilder<>(forClass);
	}

	/**
	 * This constructor is used to pass full generics type information, and
	 * avoid problems with type erasure (that basically removes most
	 * usable type references from runtime Class objects).
	 *
	 * @param forType type holder class
	 * @param <X>     the configuration instance type
	 * @return a ConfiJ builder
	 * @see GenericType
	 */
	public static <X> ConfijBuilder<X> of(GenericType<X> forType) {
		List<ResolvedType> typeParameters = Util.TYPE_RESOLVER.resolve(forType.getClass())
				.findSupertype(GenericType.class)
				.getTypeParameters();
		if (typeParameters.size() != 1) {
			throw new IllegalArgumentException("expected 1 typeParameter, but got " + typeParameters);
		}
		return new ConfijBuilder<>(typeParameters.get(0));
	}

	/**
	 * Read configuration values from these ordered {@code AnySource}s.
	 *
	 * @param sourceStr list of path's to configuration sources
	 * @return self
	 */
	public ConfijBuilder<T> loadFrom(String... sourceStr) {
		Stream.of(sourceStr)
				.map(AnySource::new)
				.forEachOrdered(sources::add);
		return this;
	}

	/**
	 * Read configuration values from these ordered sources.
	 * Intended for more complex scenarios, where {@link #loadFrom(String...)} isn't good enough.
	 *
	 * @param source list of configuration sources
	 * @return self
	 */
	public ConfijBuilder<T> loadFrom(ConfijSource... source) {
		sources.addAll(Arrays.asList(source));
		return this;
	}

	/**
	 * Attempt reading configurationd from all of these ordered {@code AnySource}s.
	 * Read failures are dropped silently.
	 * Usefull for optional configuration, like for tests only.
	 *
	 * @param maybeSourceStr optional sources, reading all of them is attempted
	 * @return self
	 * @see MaybeSource
	 */
	public ConfijBuilder<T> loadOptionalFrom(String... maybeSourceStr) {
		return loadFrom(Stream.of(maybeSourceStr)
				.map(AnySource::new)
				.map(MaybeSource::new)
				.collect(Collectors.toList())
				.toArray(new MaybeSource[]{}));
	}

	/**
	 * Attempt reading configurations from these ordered {@code AnySource}s.
	 * Ignore all configurations before and after the first successful read.
	 * Usefull if a set of configurations should be ignored if another one is present.
	 *
	 * @param firstSource an optional source
	 * @param secondSource an optional source
	 * @param otherSources more optional sources, only read if not an earlier one existed
	 * @return self
	 * @see OrSource
	 */
	public ConfijBuilder<T> loadFromFirstOf(String firstSource, String secondSource, String... otherSources) {
		return loadFrom(new OrSource(new AnySource(firstSource), new AnySource(secondSource), Stream.of(otherSources)
				.map(AnySource::new)
				.collect(Collectors.toList())
				.toArray(new AnySource[]{})));
	}

	public ConfijBuilder<T> validateWith(@NonNull ConfijValidator validator) {
		this.validator = validator;
		return this;
	}

	public ConfijBuilder<T> validateNonNull() {
		return validateWith(new NonNullValidator());
	}

	public ConfijBuilder<T> validationDisabled() {
		return validateWith(ConfijValidator.NOOP);
	}

	@NonNull
	protected NodeBindingContext getNodeBindingContext() {
		if (nodeBindingContext == null) {
			nodeBindingContext = NodeBindingContext.newDefaultSettings();
		}
		return nodeBindingContext;
	}

	public ConfijBuilder<T> nodeBindingContext(NodeBindingContext nodeBindingContext) {
		if (this.nodeBindingContext != null) {
			throw new IllegalStateException("unsafe usage of NodeBindingContext after it has been modified already");
		}
		this.nodeBindingContext = nodeBindingContext;
		return this;
	}

	public ConfijBuilder<T> templatingWith(@NonNull ValueResolver valueResolver) {
		nodeBindingContext = getNodeBindingContext().withValueResolver(valueResolver);
		return this;
	}

	public ConfijBuilder<T> templatingDisabled() {
		return templatingWith(new NoopValueResolver());
	}

	@Deprecated
	public ConfijBuilder<T> globalDefaultValue(String defaultValue) {
		nodeBindingContext = getNodeBindingContext().withDefaultValue(defaultValue);
		return this;
	}

	@NonNull
	protected BindingContext getBindingContext() {
		if (bindingContext == null) {
			bindingContext = BindingContext.newDefaultContext();
		}
		return bindingContext;
	}

	public ConfijBuilder<T> bindingContext(BindingContext bindingContext) {
		if (this.bindingContext != null) {
			throw new IllegalStateException("unsafe usage of BindingContext after it has been modified already");
		}
		this.bindingContext = bindingContext;
		return this;
	}

	public ConfijBuilder<T> bindValuesWith(ValueMapperFactory valueMapperFactory) {
		this.bindingContext = getBindingContext().withMapperFactory(valueMapperFactory);
		return this;
	}

	public <I> ConfijBuilder<T> bindValuesForClassWith(ValueMapperInstance<I> valueMapper, Class<I> forClass) {
		return bindValuesWith(ValueMapperFactory.forClass(valueMapper, forClass));
	}

	public ConfijBuilder<T> withReloader(@NonNull ConfijReloader<T> reloader) {
		this.reloader = reloader;
		return this;
	}

	protected ConfijPipeline<T> buildPipeline() {
		validator = Optional.ofNullable(validator)
				.orElseGet(ServiceLoaderValidator::new);
		ConfigBinder configBinder = new ConfigBinder();
		@SuppressWarnings("unchecked")
		ConfigBinding<T> configBinding = (ConfigBinding<T>) configBinder.toRootConfigBinding(forType, getBindingContext());
		NodeDefinition nodeDefinition = configBinding.describe(getNodeBindingContext());
		return new ConfijPipelineImpl<>(sources, new DefaultSource(), validator, configBinding, nodeDefinition);
	}

	/**
	 * finalize the configuration pipeline and build a single instance of it
	 * @return a fully initialized configuration instance
	 */
	public T build() {
		return buildPipeline().build();
	}

	public ConfijReloader<T> buildReloadable() {
		reloader = Optional.ofNullable(reloader)
				.orElseGet(ScheduledReloader::new);
		reloader.initialize(buildPipeline());
		return reloader;
	}
}
