package net.querz.worldpruner.ui;

import net.querz.worldpruner.prune.Progress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.swing.*;

public class ProgressBar extends JProgressBar implements Progress {

	private final static Logger LOGGER = LogManager.getLogger(ProgressBar.class);

	@Override
	public void setMaximum(int max) {
		if (SwingUtilities.isEventDispatchThread()) {
			super.setMaximum(max);
		} else {
			logException(() -> SwingUtilities.invokeAndWait(() -> super.setMaximum(max)));
		}
	}

	@Override
	public void setMinimum(int min) {
		if (SwingUtilities.isEventDispatchThread()) {
			super.setMinimum(min);
		} else {
			logException(() -> SwingUtilities.invokeAndWait(() -> super.setMinimum(min)));
		}
	}

	@Override
	public void increment(int inc) {
		if (SwingUtilities.isEventDispatchThread()) {
			setValue(getValue() + inc);
		} else {
			logException(() -> SwingUtilities.invokeAndWait(() -> setValue(getValue() + inc)));
		}
	}

	@Override
	public void setValue(int value) {
		if (SwingUtilities.isEventDispatchThread()) {
			super.setValue(value);
		} else {
			logException(() -> SwingUtilities.invokeAndWait(() -> super.setValue(value)));
		}
	}

	@Override
	public void done() {
		Runnable action = () -> {
			setValue(getMaximum());
			setStringPainted(false);
			setValue(0);
			setMinimum(0);
			setMaximum(0);
		};
		if (SwingUtilities.isEventDispatchThread()) {
			action.run();
		} else {
			logException(() -> SwingUtilities.invokeAndWait(action));
		}
	}

	@Override
	public void setMessage(String msg) {
		Runnable action = () -> {
			if (msg == null) {
				setStringPainted(false);
			} else {
				setStringPainted(true);
				setString(msg);
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			action.run();
		} else {
			logException(() -> SwingUtilities.invokeAndWait(action));
		}
	}

	private void logException(ExceptionRunner r) {
		try {
			r.run();
		} catch (Exception ex) {
			LOGGER.error("ProgressBar error", ex);
		}
	}

	@FunctionalInterface
	private interface ExceptionRunner {
		void run() throws Exception;
	}
}
