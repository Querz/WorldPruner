package net.querz.worldpruner.ui;

import net.querz.worldpruner.prune.Progress;
import javax.swing.*;

public class ProgressBar extends JProgressBar implements Progress {

	@Override
	public void increment(int inc) {
		setValue(getValue() + inc);
	}

	@Override
	public void done() {
		setValue(getMaximum());
		setStringPainted(false);
		setValue(0);
		setMinimum(0);
		setMaximum(0);
	}

	@Override
	public void setMessage(String msg) {
		if (msg == null) {
			setStringPainted(false);
		} else {
			setStringPainted(true);
			setString(msg);
		}
	}
}
