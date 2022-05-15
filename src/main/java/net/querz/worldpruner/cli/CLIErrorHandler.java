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
		if (skip) {
			logger.warn(msg, params);
		} else {
			logger.error(msg, params);
		}
		return !skip;
	}

	@Override
	public boolean handle(Logger logger, Throwable t, String msg, Object... params) {
		if (skip) {
			logger.warn(msg, params, t);
		} else {
			logger.error(msg, params, t);
		}
		return !skip;
	}
}
