package net.querz.worldpruner.prune;

public interface Progress {

	void setMaximum(int max);

	void setMinimum(int min);

	void increment(int inc);

	void setValue(int value);

	void setIndeterminate(boolean indeterminate);

	void done();

	void setMessage(String msg);
}
