package ch.kk7.confij.logging;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class LogUtil {
	private static final Pattern CFG_PATTERN = Pattern.compile("\\{}");

	public static String formatLog(String template, Object... args) {
		if (template == null) {
			return null;
		}
		Matcher m = CFG_PATTERN.matcher(template);
		StringBuffer sb = new StringBuffer(template.length());
		final int argsLength = args.length;
		int matchId = 0;
		while (m.find()) {
			if (matchId < argsLength) {
				String arg = Matcher.quoteReplacement(String.valueOf(args[matchId]));
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
			sb.append(" ");
			sb.append(args[i]);
		}
		return sb.toString();
	}

	public static Supplier<String> formatLogSupplier(String template, Object... args) {
		return () -> formatLog(template, args);
	}

	public static Throwable throwableOrNull(Object... args) {
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
