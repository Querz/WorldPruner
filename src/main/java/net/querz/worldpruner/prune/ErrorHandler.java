package net.querz.worldpruner.prune;

import org.apache.logging.log4j.Logger;

public interface ErrorHandler {

	boolean handle(Logger logger, String msg, Object... params);

	boolean handle(Logger logger, Throwable t, String msg, Object... params);
}
