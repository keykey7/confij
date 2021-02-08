# ConfiJ - Configuration for Java

[![Build Status](https://github.com/keykey7/confij/workflows/release/badge.svg)](https://github.com/keykey7/confij/actions)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellowgreen.svg)](LICENSE)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ch.kk7%3Aconfij&metric=coverage)](https://sonarcloud.io/dashboard?id=ch.kk7%3Aconfij)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ch.kk7/confij-core/badge.svg)](https://search.maven.org/artifact/ch.kk7/confij-core)

See the full documentation at <https://keykey7.github.io/confij/>

<img align="right" height="300" width="206" src="confij-documentation/src/docs/resources/confij-logo.png" alt="ConfiJ Logo">

ConfiJ is a Java configuration framework to facilitate loading and validating
configurations from various sources in a type-safe manner. 
This includes features such as:

- configuration definition as interfaces
- even as lists/maps/arrays/... of nested configurations
- support for various source formats: properties, YAML, HOCON, JSON, TOML
- load from various sources at once with different merge strategies
- load from: git, file, classpath, system properties, environment variables
- binding support to various immutable property types like URL, DateTime, Duration, enums, Period,...
- templating support (variable substitutions), even within paths
- plugin support for more formats and sources
- from simple non-null validation to powerful JSR303 bean validation
- live reloading hooks for change detection
- no external dependencies required by the core package

## Example

Define your configuration in code:
```java
interface HouseConfiguration {     // configuration definition as interfaces (or any)
    boolean hasRoof();             // bind to primitives
    LocalDate constructedAt();     // ...or to complex types, like temporal ones
    Room livingRoom();             // nested configurations
    Map<String, Room> rooms();     // ...even in Maps (usually immutable)
    Set<String> inhabitants();     // ...or Collections, Arrays
}
@NotNull                           // enforce well defined values (recursive)
interface Room {                   // nested definition
    @Default("1")                  // defaults and other customizations
    @Positive                      // optional full JSR303 bean validation
    int numberOfDoors();           // will become 1 if not defined otherwise
                                   //
    @Default("${numberOfDoors}")   // templating support: referencing other keys
    Optional<Integer> lockCount(); // explicit optionals
}
```
Load it as flexible as you need it (but always fail fast on unknown values):
```java
HouseConfiguration johnsHouse = ConfijBuilder.of(HouseConfiguration.class)
    .loadFrom("classpath:house.properties")   // first read properties from classpath 
    .loadFrom("johnshouse.yaml")              // override with a YAML file on disk
    .loadOptionalFrom("*-test.${sys:ending}") // wildcards, variables, optional,...
    .loadFrom(EnvvarSource.withPrefix("APP")) // then read EnvVars like APP_hasRoof=true
    .build();                                 // build an immutable instance
```
```yaml
# sample johnshouse.yaml
hasRoof: yes
constructedAt: 2000-12-24
livingRoom:
  numberOfDoors: 4
  lockCount: 1
rooms:
  bathRoom: {}
  kitchen:
    numberOfDoors: 2
inhabitants:
  - John
  - Alice
```

See the full documentation at <https://keykey7.github.io/confij/>

-- ☕⚙️
