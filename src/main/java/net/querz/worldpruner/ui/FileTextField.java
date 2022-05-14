package net.querz.worldpruner.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.function.BiFunction;

public class FileTextField extends JPanel {

	private final JTextField field = new JTextField();

	private boolean valid;

	private Runnable updateListener;
	private BiFunction<String, File, Boolean> fileValidator;
	private String invalidTooltip;

	public FileTextField(String fileType, String description) {
		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		JButton choose = new JButton("Open");
		choose.addActionListener(e -> {

			JFileChooser chooser = new JFileChooser();
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setDialogTitle(description);
			if (getText() != null && !getText().isEmpty()) {
				File fieldFile = new File(getText());
				if (fieldFile.exists() && fieldFile.isDirectory()) {
					chooser.setCurrentDirectory(fieldFile);
				}
			}
			if (fileType != null) {
				chooser.setFileFilter(new FileNameExtensionFilter("*." + fileType, fileType));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			} else {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}

			if (chooser.showOpenDialog(Window.INSTANCE) == JFileChooser.APPROVE_OPTION) {
				field.setText(chooser.getSelectedFile() + "");
				update();
			}
		});

		field.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			public void removeUpdate(DocumentEvent e) {
				update();
			}
			public void insertUpdate(DocumentEvent e) {
				update();
			}
		});

		add(field);
		add(choose);

		choose.setMargin(new Insets(1, 5, 1, 5));

		choose.setVerticalAlignment(JButton.CENTER);

		SpringUtilities.makeCompactGrid(this, 1, 2, 0, 0, 0, 0);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, choose, 0, SpringLayout.VERTICAL_CENTER, field);
	}

	public void setOnUpdate(Runnable action) {
		updateListener = action;
		action.run();
	}

	public void setFileValidator(BiFunction<String, File, Boolean> validator) {
		fileValidator = validator;
		setValid(validator.apply(getText(), new File(getText())));
	}

	public void setInvalidTooltip(String tooltip) {
		invalidTooltip = tooltip;
	}

	public String getText() {
		return field.getText();
	}

	public boolean isValueValid() {
		return valid;
	}

	public void update() {
		if (fileValidator != null) {
			setValid(fileValidator.apply(getText(), new File(getText())));
		}
		if (updateListener != null) {
			updateListener.run();
		}
	}

	private void setValid(boolean valid) {
		this.valid = valid;
		if (valid) {
			field.setBackground(Color.WHITE);
			field.setToolTipText(null);
		} else {
			field.setBackground(Const.INVALID_BACKGROUND);
			field.setToolTipText(invalidTooltip);
		}
	}
}
