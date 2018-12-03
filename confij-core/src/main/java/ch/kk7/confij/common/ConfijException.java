package ch.kk7.confij.common;

import static ch.kk7.confij.logging.LogUtil.formatLog;
import static ch.kk7.confij.logging.LogUtil.throwableOrNull;

public class ConfijException extends RuntimeException {
	public ConfijException(String s) {
		super(s);
	}

	public ConfijException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public ConfijException(String s, Object... args) {
		super(formatLog(s, args), throwableOrNull(args));
	}
}
