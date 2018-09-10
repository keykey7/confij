package ch.kk7.config4j.binding.leaf.mapper;

import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.binding.leaf.IValueMapper;
import ch.kk7.config4j.binding.leaf.IValueMapper.NullableValueMapper;
import ch.kk7.config4j.binding.leaf.IValueMapperFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ExplicitMapperFactory implements IValueMapperFactory {
	private final Map<Class<?>, IValueMapper<?>> mappings;

	public ExplicitMapperFactory() {
		this.mappings = new HashMap<>();
		withMapping(String.class, s -> s);
		withMapping(Path.class, s -> Paths.get(s));
		withMapping(File.class, File::new);
		withMapping(Duration.class, new DurationMapper());
		withMapping(Period.class, new PeriodMapper());
		// TODO: support Date formats
		// TODO: support MemoryUnits
		// TODO: support Optional<> (might also be a full binding instead of a leaf binding)
		// TODO: pass in annotation information to mapper to allow for customization
	}

	@Override
	public Optional<IValueMapper<?>> maybeForType(BindingType type) {
		return Optional.ofNullable(mappings.get(type.getResolvedType()
				.getErasedType()));
	}

	protected <T> void withMapping(Class<T> forClass, NullableValueMapper<T> mapping) {
		mappings.put(forClass, mapping);
	}
}
