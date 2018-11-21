# ConfiJ - Configuration for Java

ConfiJ is a Java configuration framework to facilitate loading and validating
configurations from various sources in a type-safe manner. 
This includes features such as

- configuration definition as interfaces
- most powerfull support for nested configurations
- even as lists/maps/arrays/... of nested configurations
- binding support for various immutable property types
- configuration pipelines and loading from various sources
- support for source formats: Java properties, YAML, system properties, environment variables
- templating support (variable substitutions), even within paths
- plugin support for more formats and sources
- JSR303 bean validation

## Example

Sample configuration of a house with nested properties, defaults and validation
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
	.withSource("classpath:house.properties", "johnshouse.yaml")
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
