# ConfiJ - Configuration for Java

[![Build Status](https://travis-ci.com/keykey7/confij.svg?branch=master)](https://travis-ci.com/keykey7/confij)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellowgreen.svg)](LICENSE)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ch.kk7%3Aconfij&metric=coverage)](https://sonarcloud.io/dashboard?id=ch.kk7%3Aconfij)
[![release](https://api.bintray.com/packages/kk7/mvn-release/confij-core/images/download.svg) ](https://bintray.com/kk7/mvn-release/confij-core/_latestVersion)

ConfiJ is a Java configuration framework to facilitate loading and validating
configurations from various sources in a type-safe manner. 
This includes features such as

- configuration definition as interfaces
- support for various source formats: properties, YAML, HOCON, JSON
- load from: Git, File, Classpath, HTTP, system properties, environment variables
- load from various sources (pipelines) with different merge strategies
- powerfull support for nested configurations
- even as lists/maps/arrays/... of nested configurations
- binding support to various immutable property types like URL, DateTime, Duration, enums, Period,...
- templating support (variable substitutions), even within paths
- plugin support for more formats and sources
- JSR303 bean validation
- minimal dependencies

## Example

See the full documentation at <https://keykey7.github.io/confij/>

Sample configuration of a house with nested properties, defaults and validation-annotations
```java
interface House {
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
House johnsHouse = ConfijBuilder.of(House.class)
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
