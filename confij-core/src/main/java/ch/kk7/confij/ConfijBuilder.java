package ch.kk7.confij;

import ch.kk7.confij.binding.BindingContext;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.binding.values.ValueMapperFactory;
import ch.kk7.confij.binding.values.ValueMapperInstance;
import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.common.GenericType;
import ch.kk7.confij.common.Util;
import ch.kk7.confij.pipeline.ConfijPipeline;
import ch.kk7.confij.pipeline.ConfijPipelineImpl;
import ch.kk7.confij.pipeline.reload.ConfijReloadNotifier;
import ch.kk7.confij.pipeline.reload.ConfijReloadStrategy;
import ch.kk7.confij.pipeline.reload.NeverReloadStrategy;
import ch.kk7.confij.pipeline.reload.PeriodicReloadStrategy;
import ch.kk7.confij.pipeline.reload.ReloadNotifierImpl;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.any.AnySourceImpl;
import ch.kk7.confij.source.any.FixResourceAnyFormatSource;
import ch.kk7.confij.source.any.FixResourceFixFormatSource;
import ch.kk7.confij.source.defaults.DefaultSource;
import ch.kk7.confij.source.format.ConfijFormat;
import ch.kk7.confij.source.logical.MaybeSource;
import ch.kk7.confij.source.logical.OrSource;
import ch.kk7.confij.source.resource.ConfijResource;
import ch.kk7.confij.template.NoopValueResolver;
import ch.kk7.confij.template.SimpleVariableResolver;
import ch.kk7.confij.template.ValueResolver;
import ch.kk7.confij.tree.NodeBindingContext;
import ch.kk7.confij.tree.NodeDefinition;
import ch.kk7.confij.validation.ConfijValidator;
import ch.kk7.confij.validation.MultiValidator;
import ch.kk7.confij.validation.NonNullValidator;
import ch.kk7.confij.validation.ServiceLoaderValidator;
import com.fasterxml.classmate.ResolvedType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@ToString(onlyExplicitlyIncluded = true)
public class ConfijBuilder<T> {
	@ToString.Include
	private final Type forType;
	private final List<ConfijSource> sources = new ArrayList<>();
	private ConfijValidator<T> validator = null;
	private ConfijValidator<T> nonNullValidator = null;
	private ValueResolver valueResolver = null;
	private final ArrayList<ValueMapperFactory> valueMapperFactories = new ArrayList<>(ValueMapperFactory.defaultFactories());
	private ConfijReloadStrategy reloadStrategy = null;
	private final ReloadNotifierImpl<T> reloadNotifier = new ReloadNotifierImpl<>();

	protected void lazySetDefaults() {
		validator = Optional.ofNullable(validator)
				.orElseGet(() -> MultiValidator.of(new ServiceLoaderValidator<>(), Optional.ofNullable(nonNullValidator)
						.orElseGet(NonNullValidator::initiallyNullable)));
		valueResolver = Optional.ofNullable(valueResolver)
				.orElseGet(SimpleVariableResolver::new);
		reloadStrategy = Optional.ofNullable(reloadStrategy)
				.orElseGet(NeverReloadStrategy::new);
	}

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
	 * Read configuration values from these ordered {@code AnySource}s.
	 *
	 * @param sourceStr list of path's to configuration sources
	 * @return self
	 */
	public ConfijBuilder<T> loadFrom(String... sourceStr) {
		return loadFrom(Stream.of(sourceStr)
				.map(AnySourceImpl::new)
				.toArray(AnySourceImpl[]::new));
	}

	/**
	 * Read configuration values from these ordered resources (like from FS, git,...).
	 * The format of the content is guessed.
	 *
	 * @param resources list of resources (like files,...) to read configurations from
	 * @return self
	 */
	public ConfijBuilder<T> loadFrom(ConfijResource... resources) {
		return loadFrom(Stream.of(resources)
				.map(FixResourceAnyFormatSource::new)
				.toArray(FixResourceAnyFormatSource[]::new));
	}

	/**
	 * Read configuration values from a well defined source and format.
	 * Equivalent to {@link #loadFrom(ConfijSource...)}.
	 *
	 * @param resource resource (like files,...) to read configurations from
	 * @param format the actual format which understants the content of <i>resource</i>
	 * @return self
	 */
	public ConfijBuilder<T> loadFrom(ConfijResource resource, ConfijFormat format) {
		return loadFrom(new FixResourceFixFormatSource(resource, format));
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
				.map(AnySourceImpl::new)
				.map(MaybeSource::new)
				.toArray(MaybeSource[]::new));
	}

	/**
	 * Attempt reading configurations from these ordered {@code AnySource}s.
	 * Ignore all configurations before and after the first successful read.
	 * Usefull if a set of configurations should be ignored if another one is present.
	 *
	 * @param firstSource  an optional source
	 * @param secondSource an optional source
	 * @param otherSources more optional sources, only read if not an earlier one existed
	 * @return self
	 * @see OrSource
	 */
	public ConfijBuilder<T> loadFromFirstOf(String firstSource, String secondSource, String... otherSources) {
		return loadFrom(new OrSource(new AnySourceImpl(firstSource), new AnySourceImpl(secondSource), Stream.of(otherSources)
				.map(AnySourceImpl::new)
				.toArray(AnySourceImpl[]::new)));
	}

	/**
	 * replace the default validation/post-processing steps with this new validator.
	 * The default is to load all {@link java.util.ServiceLoader}s implementing {@link ConfijValidator}.
	 * To combine multiple validators use a {@link MultiValidator}.
	 *
	 * @param validator the new validator
	 * @return self
	 */
	public ConfijBuilder<T> validateOnlyWith(@NonNull ConfijValidator<T> validator) {
		this.validator = validator;
		return this;
	}

	public ConfijBuilder<T> validationAllowsNull() {
		nonNullValidator = ConfijValidator.noopValidator();
		return this;
	}

	/**
	 * Explicitly disable all validation/post-processing steps.
	 *
	 * @see #validateOnlyWith(ConfijValidator)
	 * @return self
	 */
	public ConfijBuilder<T> validationDisabled() {
		return validateOnlyWith(ConfijValidator.noopValidator());
	}

	public ConfijBuilder<T> templatingWith(@NonNull ValueResolver valueResolver) {
		this.valueResolver = valueResolver;
		return this;
	}

	/**
	 * disables templating (variable substitution), such that your values containing {@code ${variables}} just stay as they are.
	 *
	 * @return self
	 */
	public ConfijBuilder<T> templatingDisabled() {
		return templatingWith(new NoopValueResolver());
	}

	/**
	 * Globally register an additional {@link ValueMapperFactory}. This allows for custom classes in your configuration definition.
	 * Same as writing
	 * <pre>{@code
	 *   \@ValueMapper(CustomMapper.class)
	 *   interface MyConfigRoot { ... }
	 * }</pre>
	 *
	 * @param valueMapperFactory the new mapper with highest preference
	 * @return self
	 * @see #bindValuesForClassWith(ValueMapperInstance, Class)
	 */
	public ConfijBuilder<T> bindValuesWith(ValueMapperFactory valueMapperFactory) {
		// put the new value mapper first to give it preference
		valueMapperFactories.add(0, valueMapperFactory);
		return this;
	}

	/**
	 * Convenience method to register a {@link ValueMapperFactory} which will only handle a single class.
	 *
	 * @param valueMapper functional interface to convert a string to {@code I}
	 * @param forClass the class of {@code I}
	 * @param <I> the target type
	 * @return self
	 */
	public <I> ConfijBuilder<T> bindValuesForClassWith(ValueMapperInstance<I> valueMapper, Class<I> forClass) {
		return bindValuesWith(ValueMapperFactory.forClass(valueMapper, forClass));
	}

	/**
	 * define when/how new configurations are loaded. default is a {@link NeverReloadStrategy}.
	 *
	 * @param reloadStrategy the new reload strategy to be used
	 * @return self
	 */
	public ConfijBuilder<T> reloadStrategy(@NonNull ConfijReloadStrategy reloadStrategy) {
		this.reloadStrategy = reloadStrategy;
		return this;
	}

	/**
	 * A {@link PeriodicReloadStrategy} with initial delay equal to interval duration
	 *
	 * @param duration time between reload attempts
	 * @return self
	 */
	public ConfijBuilder<T> reloadPeriodically(@NonNull Duration duration) {
		return reloadStrategy(new PeriodicReloadStrategy(duration, duration));
	}

	@NonNull
	protected BindingContext newBindingContext() {
		return BindingContext.newDefaultContext(valueMapperFactories);
	}

	@NonNull
	protected NodeBindingContext newNodeBindingContext() {
		return NodeBindingContext.newDefaultSettings(valueResolver);
	}

	@NonNull
	protected ConfijPipeline<T> newPipeline() {
		ConfigBinder configBinder = new ConfigBinder();
		@SuppressWarnings("unchecked")
		ConfigBinding<T> configBinding = (ConfigBinding<T>) configBinder.toRootConfigBinding(forType, newBindingContext());
		NodeDefinition nodeDefinition = configBinding.describe(newNodeBindingContext());
		return new ConfijPipelineImpl<>(sources, new DefaultSource(), validator, configBinding, nodeDefinition, reloadNotifier);
	}

	/**
	 * Finalize the configuration pipeline and build a single instance of it.
	 * In cases where you enable periodic configuration reloading, use {@link #buildWrapper()} instead.
	 *
	 * @return a fully initialized configuration instance
	 */
	public T build() {
		if (reloadStrategy != null && !(reloadStrategy instanceof NeverReloadStrategy)) {
			throw new ConfijException("there is an active ReloadStrategy configured ({}). " +
					"however, by calling .build() there is no way to obtain an updated version of the configuration. " +
					"use the .buildWrapper() instead and attach your reload hooks there.", reloadStrategy);
		}
		return buildWrapper().get();
	}

	/**
	 * Finalize the configuration pipeline and build a single instance holding the most recent configuration.
	 * In cases where no periodic configuration reload strategy is defined, {@link #build()} is simpler to use instead.
	 *
	 * @return a wrapper with the most up-to-date instance and ways to register reload notification hooks.
	 */
	public ConfijWrapper<T> buildWrapper() {
		lazySetDefaults();
		ConfijPipeline<T> pipeline = newPipeline();
		T initialConfig = pipeline.build();
		reloadStrategy.register(pipeline);
		return new ConfijWrapper<>(initialConfig, reloadNotifier);
	}

	@Value
	@ToString(onlyExplicitlyIncluded = true)
	public static class ConfijWrapper<T> {
		@Getter(AccessLevel.NONE)
		AtomicReference<T> reference;
		ConfijReloadNotifier<T> reloadNotifier;

		public ConfijWrapper(T initialValue, ReloadNotifierImpl<T> reloadNotifier) {
			reference = new AtomicReference<>(initialValue);
			this.reloadNotifier = reloadNotifier;
			reloadNotifier.registerRootReloadHandler(x -> reference.set(x.getNewValue()));
		}

		public T get() {
			return reference.get();
		}
	}
}
