# ConfiJ - Configuration for Java

[![Build Status](https://github.com/keykey7/confij/workflows/release/badge.svg)](https://github.com/keykey7/confij/actions)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellowgreen.svg)](LICENSE)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ch.kk7%3Aconfij&metric=coverage)](https://sonarcloud.io/dashboard?id=ch.kk7%3Aconfij)
[![release](https://api.bintray.com/packages/kk7/mvn-release/confij-core/images/download.svg) ](https://bintray.com/kk7/mvn-release/confij-core/_latestVersion)

See the full documentation at <https://keykey7.github.io/confij/>

ConfiJ is a Java configuration framework to facilitate loading and validating
configurations from various sources in a type-safe manner. 
This includes features such as

- configuration definition as interfaces
- even as lists/maps/arrays/... of nested configurations
- support for various source formats: properties, YAML, HOCON, JSON
- load from various sources at once with different merge strategies
- load from: Git, File, Classpath, HTTP, system properties, environment variables
- binding support to various immutable property types like URL, DateTime, Duration, enums, Period,...
- templating support (variable substitutions), even within paths
- plugin support for more formats and sources
- JSR303 bean validation
- no dependencies

## Example

Sample configuration of a house with nested properties, defaults and validation-annotations
```java
interface HouseConfiguration {
	@Default("true")
	boolean hasRoof();

	Map<String,Room> rooms();

	Set<@NotEmpty String> inhabitants();

	Period chimneyCheckEvery();

	@Default("${chimneyCheckEvery}")
	Period boilerCheckEvery();
}

interface Room {
	@Positive
	int numberOfWindows();

	@Default("Wood")
	FloorType floor();

	enum FloorType {
		Wood, Carpet, Stone
	}
}
```
Load an immutable configuration instance with base settings from
a property file on the classpath and override it with a YAML configuration from
the local filesystem.
```java
HouseConfiguration johnsHouse = ConfijBuilder.of(HouseConfiguration.class)
	.loadFrom("classpath:house.properties", "johnshouse.yaml")
	.build();
```
```yaml
# johnshouse.yaml
rooms:
  livingroom:
    numberOfWindows: 4
  bedroom:
    numberOfWindows: 1
    floor: Wood
inhabitants:
  - John
  - Alice
chimneyCheckEvery: 2years
```
