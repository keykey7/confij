package ch.kk7.confij.binding.values;

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
		return ValueMapperFactory.forClass(Paths::get, Path.class);
	}

	public static ValueMapperFactory forFile() {
		return ValueMapperFactory.forClass(File::new, File.class);
	}
}
