package net.querz.worldpruner.cli;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import net.querz.worldpruner.prune.Progress;

public class AdvancedCLIProgress implements Progress {

	private final ProgressBar progressBar;

	public AdvancedCLIProgress() {
		ProgressBarBuilder pbb = new ProgressBarBuilder()
				.setStyle(ProgressBarStyle.ASCII)
				.setUpdateIntervalMillis(100);
		progressBar = pbb.build();
	}

	@Override
	public void setMaximum(int max) {
		progressBar.maxHint(max);
		progressBar.reset();
	}

	@Override
	public void setMinimum(int min) {
		// ProgressBar does not support setting minimum value
	}

	@Override
	public void increment(int inc) {
		progressBar.stepBy(inc);
	}

	@Override
	public void setValue(int value) {
		progressBar.stepTo(value);
		if (value <= 0) {
			progressBar.reset();
		}
	}

	@Override
	public void setIndeterminate(boolean indeterminate) {
		progressBar.reset();
	}

	@Override
	public void done() {
		progressBar.close();
	}

	@Override
	public void setMessage(String msg) {
		progressBar.setExtraMessage(msg);
	}
}
