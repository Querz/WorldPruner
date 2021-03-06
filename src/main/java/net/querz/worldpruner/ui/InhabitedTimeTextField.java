package net.querz.worldpruner.ui;

import net.querz.worldpruner.prune.PruneData;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class InhabitedTimeTextField extends JTextField {

	private long duration;
	private Runnable updateListener;

	public InhabitedTimeTextField(String def, int cols) {
		super(def, cols);
		validateDuration();
	}

	private void validateDuration() {
		try {
			duration = PruneData.parseDuration(getText()) * 20;
			setBackground(Colors.VALID_BACKGROUND);
			setToolTipText(duration + " ticks");
		} catch (Exception ex) {
			duration = -1;
			setBackground(Colors.INVALID_BACKGROUND);
			setToolTipText("invalid");
		}
		if (updateListener != null) {
			updateListener.run();
		}
	}

	public void setOnUpdate(Runnable action) {
		updateListener = action;
	}

	public boolean isDurationValid() {
		return duration >= 0;
	}

	public long getDuration() {
		return duration;
	}

	@Override
	protected Document createDefaultModel() {
		return new InhabitedTimeDocument();
	}

	class InhabitedTimeDocument extends PlainDocument {

		public InhabitedTimeDocument() {
			addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					validateDuration();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					validateDuration();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {}
			});
		}
	}
}
