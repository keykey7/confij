package ch.kk7.confij.binding.leaf.mapper;

import ch.kk7.confij.binding.leaf.ValueMapperFactory;
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
		return ValueMapperFactory.forClass(s -> Paths.get(s), Path.class);
	}

	public static ValueMapperFactory forFile() {
		return ValueMapperFactory.forClass(File::new, File.class);
	}

	// TODO: support Date formats
	// TODO: support MemoryUnits
	// TODO: support Optional<> (might also be a full binding instead of a leaf binding)
}
