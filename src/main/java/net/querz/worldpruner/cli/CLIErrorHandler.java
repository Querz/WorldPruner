package net.querz.worldpruner.cli;

import net.querz.worldpruner.prune.ErrorHandler;
import org.apache.logging.log4j.Logger;

public class CLIErrorHandler implements ErrorHandler {

	private final boolean skip;

	public CLIErrorHandler(boolean skip) {
		this.skip = skip;
	}

	@Override
	public boolean handle(Logger logger, String msg, Object... params) {
		if (params.length > 0 && params[params.length - 1] instanceof Throwable) {
			Object[] copy = new Object[params.length - 1];
			System.arraycopy(params, 0, copy, 0, copy.length);
			if (skip) {
				logger.warn(msg, copy, params[params.length - 1]);
			} else {
				logger.error(msg, copy, params[params.length - 1]);
			}
		} else {
			if (skip) {
				logger.warn(msg, params);
			} else {
				logger.error(msg, params);
			}
		}
		return !skip;
	}
}
