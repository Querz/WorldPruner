package net.querz.worldpruner.ui;

import net.querz.worldpruner.prune.ErrorHandler;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;
import javax.swing.*;

public class DialogErrorHandler implements ErrorHandler {

	private final JFrame parent;

	public DialogErrorHandler(JFrame parent) {
		this.parent = parent;
	}

	@Override
	public boolean handle(Logger logger, String msg, Object... params) {
		FormattedMessage formatted;
		if (params.length > 0 && params[params.length - 1] instanceof Throwable) {
			Object[] copy = new Object[params.length - 1];
			System.arraycopy(params, 0, copy, 0, copy.length);
			logger.error(msg, copy, params[params.length - 1]);
			formatted = new FormattedMessage(msg, copy, (Throwable) params[params.length - 1]);
		} else {
			logger.error(msg, params);
			formatted = new FormattedMessage(msg, params);
		}

		int result = JOptionPane.showConfirmDialog(parent, formatted.getFormattedMessage() + "\n\nWould you like to continue?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
		return result != JOptionPane.YES_OPTION;
	}
}
