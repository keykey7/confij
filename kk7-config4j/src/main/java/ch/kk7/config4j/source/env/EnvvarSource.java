package ch.kk7.config4j.source.env;

public class EnvvarSource extends FlatSource {
	public static final String SCHEME = "env";

	public EnvvarSource() {
		super(System.getenv());
		setSeparator("_");
	}
}
