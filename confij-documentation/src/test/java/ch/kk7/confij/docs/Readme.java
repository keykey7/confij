package ch.kk7.confij.docs;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.source.env.EnvvarSource;
import ch.kk7.confij.validation.NonNullValidator.NotNull;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.Positive;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Readme extends DocTestBase {
	// tag:readme-unused[]
	interface HouseConfiguration {     // configuration definition as interfaces (or any)
		boolean hasRoof();             // bind to primitives
		LocalDate constructedAt();     // ...or to complex types, like temporal ones
		Room livingRoom();             // nested configurations
		Map<String, Room> rooms();     // ...even in Maps (usually immutable)
		Set<String> inhabitants();     // ...or Collections, Arrays
	}
	@NotNull
	interface Room {                   // nested definition
		@Default("1")                  // defaults and other customizations
		@Positive                      // optional full JSR303 bean validation
		int numberOfDoors();           // will become 1 if not defined otherwise
		@Default("${numberOfDoors}")   // templating support: referencing other keys
		Optional<Integer> lockCount(); // explicit optionals
	}
	// end:readme-unused[]

	@Test
	void houseTest() {
		HouseConfiguration johnsHouse = ConfijBuilder.of(HouseConfiguration.class)
				.loadFrom("classpath:house.properties")   // first read properties from classpath
				.loadFrom("johnshouse.yaml")              // override with a YAML file on disk
				.loadOptionalFrom("*-test.${sys:ending}") // wildcards, variables, optional,...
				.loadFrom(EnvvarSource.withPrefix("APP")) // then read EnvVars like APP_hasRoof=true
				.build();
		assertThat(johnsHouse.hasRoof()).isTrue();
		assertThat(johnsHouse.livingRoom()
				.lockCount()).hasValue(1);
		assertThat(johnsHouse.rooms()
				.get("bathRoom")
				.lockCount()).hasValue(42);
	}

	@Test
	void readmeMatches() throws Exception {
		assertThatContains(projectRootReadme(), "(?msi)^```java(.+?)^```", simplify(thisJavaFile()));
		String yaml = new Scanner(new File("johnshouse.yaml")).useDelimiter("\\A")
				.next();
		assertThatContains(projectRootReadme(), "(?msi)^```yaml(.+?)^```", simplify(yaml));
	}

	private void assertThatContains(String readme, String regex, String mustContainIt) {
		Matcher matcher = Pattern.compile(regex)
				.matcher(readme);
		boolean hasJava = false;
		while (matcher.find()) {
			String content = simplify(matcher.group(1));
			assertThat(mustContainIt).contains(content);
			hasJava = true;
		}
		assertThat(hasJava).isTrue();
	}

	private static String simplify(String content) {
		return content.replaceAll("//.*", "")
				.replaceAll("[\t\n\r ]+", " ")
				.trim();
	}

	private static File projectRoot() {
		File path = new File(".").getAbsoluteFile()
				.getParentFile();
		while (path != null) {
			if (new File(path, "gradlew").exists()) {
				return path;
			}
			path = path.getParentFile();
		}
		throw new IllegalStateException("cannot find project root");
	}

	private static String projectRootReadme() throws FileNotFoundException {
		return new Scanner(new File(projectRoot(), "README.md")).useDelimiter("\\A")
				.next();
	}

	private static String thisJavaFile() throws FileNotFoundException {
		return new Scanner(new File(projectRoot(), "confij-documentation/src/test/java/" +
				Readme.class.getName()
						.replaceAll("\\.", "/") +
				".java")).useDelimiter("\\A")
				.next();
	}
}
