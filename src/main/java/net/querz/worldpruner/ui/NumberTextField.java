package net.querz.worldpruner.ui;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class NumberTextField extends JTextField {

	private final int min, max;
	private int length;

	public NumberTextField(int min, int max, String def, int cols) {
		super(def, cols);
		if (min > max) {
			throw new IllegalArgumentException("minimum is larger than maximum");
		}
		this.min = min;
		this.max = max;
	}

	@Override
	protected Document createDefaultModel() {
		return new NumberDocument();
	}

	public int getNumber() {
		return Integer.parseInt(getText());
	}

	class NumberDocument extends PlainDocument {

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			String before = getText(0, getLength());
			super.insertString(offs, str, a);
			String content = getText(0, getLength());
			if (length == 0) {
				length = Integer.toString(max).length();
			}
			if (content.length() > length) {
				setText(before);
				return;
			}
			try {
				int i = Integer.parseInt(content);
				if (i < min || i > max) {
					setText(before);
				}
			} catch (NumberFormatException ex) {
				setText(before);
			}
		}
	}
}
