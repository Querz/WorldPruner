package net.querz.worldpruner.ui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class FileTextField extends JPanel {

	private final JTextField field = new JTextField();
	private final JButton choose = new JButton("Open");

	public FileTextField(boolean dirsOnly, String fileType) {
		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		choose.addActionListener(e -> {

			JFileChooser chooser = new JFileChooser();
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setDialogTitle("Open World");
			if (field.getText() != null && !field.getText().isEmpty()) {
				File fieldFile = new File(field.getText());
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
			}
		});

		add(field);
		add(choose);

		choose.setMargin(new Insets(1, 5, 1, 5));

		choose.setVerticalAlignment(JButton.CENTER);

		SpringUtilities.makeCompactGrid(this, 1, 2, 0, 0, 0, 0);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, choose, 0, SpringLayout.VERTICAL_CENTER, field);
	}

	private void setAppleFileDialogForDirectories(boolean dirsOnly, boolean value) {
		if (dirsOnly) {
			System.setProperty("apple.awt.fileDialogForDirectories", value + "");
		}
	}

	public String getText() {
		return field.getText();
	}
}
