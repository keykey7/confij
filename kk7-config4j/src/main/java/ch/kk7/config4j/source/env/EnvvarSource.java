package ch.kk7.config4j.source.env;

public class EnvvarSource extends FlatSource {
	public EnvvarSource() {
		super(System.getenv());
		setSeparator("_");
	}
}
