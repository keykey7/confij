package ch.kk7.config4j.format.validation;

import ch.kk7.config4j.source.simple.SimpleConfig;
import ch.kk7.config4j.source.simple.SimpleConfigLeaf;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class NotNullValidator implements IValidator {
	public void validate(SimpleConfig simpleConfig) {
		List<String> offendingLeafUris = simpleConfig.leaves()
				.filter(leaf -> leaf.get() == null)
				.filter(leaf -> !leaf.getConfig()
						.getFormatSettings()
						.isNullAllowed())
				.map(SimpleConfigLeaf::getUri)
				.map(URI::toString)
				.collect(Collectors.toList());
		if (offendingLeafUris.isEmpty()) {
			return;
		}
		String msg = "Found non-nullable fields with null values: " +
				offendingLeafUris.stream()
						.collect(Collectors.joining("\n  "));
		throw new ValidationException(msg);
	}
}
