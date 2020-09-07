package ch.kk7.confij.docs;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.annotation.Default;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.Period;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Readme extends DocTestBase {
	interface HouseConfiguration {
		@Default("true")
		boolean hasRoof();

		Map<String, Room> rooms();

		Set<@NotEmpty String> inhabitants();

		Period chimneyCheckEvery();

		@Default("${chimneyCheckEvery}")
		Period boilerCheckEvery();
	}

	interface Room {
		@Positive int numberOfWindows();

		@Default("Wood")
		FloorType floor();

		enum FloorType {
			Wood,
			Carpet,
			Stone
		}
	}

	@Test
	void houseTest() {
		HouseConfiguration johnsHouse = ConfijBuilder.of(HouseConfiguration.class)
				.loadFrom("classpath:house.properties", "johnshouse.yaml")
				.build();
		assertThat(johnsHouse.chimneyCheckEvery()).isEqualTo(Period.ofYears(2));
		assertThat(johnsHouse.boilerCheckEvery()).isEqualTo(Period.ofYears(2));
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
		return content.replaceAll("[\t\n\r ]+", " ")
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
