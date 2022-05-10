package net.querz.worldpruner.cli;

import net.querz.worldpruner.prune.Progress;

public class CLIProgress implements Progress {

	private int max, min, value;
	private boolean indeterminate;
	private String msg;

	@Override
	public void setMaximum(int max) {
		this.max = max;
		print();
	}

	@Override
	public void setMinimum(int min) {
		this.min = min;
		print();
	}

	@Override
	public void increment(int inc) {
		this.value += inc;
		print();
	}

	@Override
	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public void setIndeterminate(boolean indeterminate) {
		this.indeterminate = indeterminate;
	}

	@Override
	public void done() {
		value = max;
		indeterminate = false;
		msg = "Done";
	}

	@Override
	public void setMessage(String msg) {
		this.msg = msg;
	}

	private void print() {
		if (indeterminate) {
			System.out.printf("Waiting for %s\n", msg);
		}
		System.out.printf("%s: %.2f%%\n", msg, (float) (value - min) / (float) (max - min) * 100f);
	}
}
