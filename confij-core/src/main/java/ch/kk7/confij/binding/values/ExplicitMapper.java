package ch.kk7.confij.binding.values;

import ch.kk7.confij.binding.values.ValueMapperInstance.NullableValueMapperInstance;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class ExplicitMapper {
	public static ValueMapperFactory forString() {
		return ValueMapperFactory.forClass(s -> s, String.class);
	}

	public static ValueMapperFactory forPath() {
		return ValueMapperFactory.forClass((NullableValueMapperInstance<Path>) Paths::get, Path.class);
	}

	public static ValueMapperFactory forFile() {
		return ValueMapperFactory.forClass((NullableValueMapperInstance<File>) File::new, File.class);
	}
}
