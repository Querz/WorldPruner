package net.querz.worldpruner.ui;

import net.querz.worldpruner.prune.ErrorHandler;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;

import javax.swing.*;

public class DialogErrorHandler implements ErrorHandler {

	private final JFrame parent;
	private boolean error;

	public DialogErrorHandler(JFrame parent) {
		this.parent = parent;
	}

	@Override
	public boolean handle(Logger logger, String msg, Object... params) {
		logger.error(msg, params);
		FormattedMessage formatted = new FormattedMessage(msg, params);
		int result = JOptionPane.showConfirmDialog(
				parent,
				formatted.getFormattedMessage() + "\n\nDo you want to continue?",
				"Error",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.ERROR_MESSAGE);
		onError(result);
		return result != JOptionPane.YES_OPTION;
	}

	@Override
	public boolean handle(Logger logger, Throwable t, String msg, Object... params) {
		logger.error(msg, params, t);
		FormattedMessage formatted = new FormattedMessage(msg, params, t);
		int result = JOptionPane.showConfirmDialog(
				parent,
				formatted.getFormattedMessage() + "\n\nDo you want to continue?",
				"Error",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.ERROR_MESSAGE);
		onError(result);
		return result != JOptionPane.YES_OPTION;
	}

	@Override
	public boolean wasSuccessful() {
		return !error;
	}

	private void onError(int result) {
		if (result != JOptionPane.YES_OPTION) {
			error = true;
		}
	}

}
