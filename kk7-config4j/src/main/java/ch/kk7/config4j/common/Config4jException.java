package ch.kk7.config4j.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config4jException extends RuntimeException {
	private static final Pattern CFG_PATTERN = Pattern.compile("\\{}");

	public Config4jException(String s) {
		super(s);
	}

	public Config4jException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public Config4jException(String s, Object... args) {
		super(format(s, args), maybeThrowable(s, args));
	}

	private static String format(String s, Object... args) {
		if (s == null) {
			return null;
		}
		Matcher m = CFG_PATTERN.matcher(s);
		StringBuffer sb = new StringBuffer(s.length());
		final int argsLength = args.length;
		int matchId = 0;
		while (m.find()) {
			if (matchId < argsLength) {
				String arg = String.valueOf(args[matchId]);
				m.appendReplacement(sb, arg);
				matchId++;
			} else {
				// invalid format: we have nothing to put into this placeholder. let's ignore it...
				m.appendReplacement(sb, "{?}");
			}
		}
		m.appendTail(sb);
		for (int i = matchId; i < argsLength; i++) {
			if (i == argsLength - 1 && args[i] instanceof Throwable) {
				continue;
			}
			// invalid format: just append whatever args do not have a placeholder
			String arg = String.valueOf(args[i]);
			sb.append(" ")
					.append(String.valueOf(arg));
		}
		return sb.toString();
	}

	private static Throwable maybeThrowable(String s, Object... args) {
		if (args.length == 0) {
			return null;
		}
		Object lastArg = args[args.length - 1];
		if (lastArg instanceof Throwable) {
			return (Throwable) lastArg;
		}
		return null;
	}
}
