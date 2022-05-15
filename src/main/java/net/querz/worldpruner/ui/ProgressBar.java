package net.querz.worldpruner.ui;

import net.querz.worldpruner.prune.Progress;
import javax.swing.*;

public class ProgressBar extends JProgressBar implements Progress {

	@Override
	public void setMaximum(int max) {
		if (SwingUtilities.isEventDispatchThread()) {
			super.setMaximum(max);
		} else {
			toRuntimeException(() -> SwingUtilities.invokeAndWait(() -> super.setMaximum(max)));
		}
	}

	@Override
	public void setMinimum(int min) {
		if (SwingUtilities.isEventDispatchThread()) {
			super.setMinimum(min);
		} else {
			toRuntimeException(() -> SwingUtilities.invokeAndWait(() -> super.setMinimum(min)));
		}
	}

	@Override
	public void increment(int inc) {
		if (SwingUtilities.isEventDispatchThread()) {
			setValue(getValue() + inc);
		} else {
			toRuntimeException(() -> SwingUtilities.invokeAndWait(() -> setValue(getValue() + inc)));
		}
	}

	@Override
	public void setValue(int value) {
		if (SwingUtilities.isEventDispatchThread()) {
			super.setValue(value);
		} else {
			toRuntimeException(() -> SwingUtilities.invokeAndWait(() -> super.setValue(value)));
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
			toRuntimeException(() -> SwingUtilities.invokeAndWait(action));
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
			toRuntimeException(() -> SwingUtilities.invokeAndWait(action));
		}
	}

	private void toRuntimeException(ExceptionRunner r) {
		try {
			r.run();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@FunctionalInterface
	private interface ExceptionRunner {
		void run() throws Exception;
	}
}
