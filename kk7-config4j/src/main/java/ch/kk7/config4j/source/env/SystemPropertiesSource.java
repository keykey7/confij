package ch.kk7.config4j.source.env;

public class SystemPropertiesSource extends FlatSource {
	public static final String SCHEME = "sys";

	public SystemPropertiesSource() {
		super(System.getProperties());
		setSeparator(".");
	}
}
