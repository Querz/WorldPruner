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

//			try {
//				// for MacOS, we need to set this system property so the file dialog allows selection of folders
//				setAppleFileDialogForDirectories(dirsOnly, true);
//
//				// either open the directory from the text field if it's valid or open the default directory
//				FileDialog chooser = new FileDialog(Window.INSTANCE, "Open World", FileDialog.LOAD);
//				if (field.getText() != null && !field.getText().isEmpty()) {
//					File fieldFile = new File(field.getText());
//					if (fieldFile.exists() && fieldFile.isDirectory()) {
//						chooser.setDirectory(fieldFile + "");
//					}
//				}
//				chooser.setMultipleMode(false);
//				if (fileType != null) {
//					chooser.setFilenameFilter((f, n) -> !new File(f, n).isDirectory() && n.endsWith("." + fileType));
//				} else {
//					chooser.setFilenameFilter((f, n) -> new File(f, n).isDirectory());
//				}
//				chooser.setVisible(true);
//				if (chooser.getFile() != null) {
//					File file = new File(chooser.getDirectory(), chooser.getFile());
//					field.setText(file + "");
//				}
//			} finally {
//				setAppleFileDialogForDirectories(dirsOnly, false);
//			}
		});

		add(field);
		add(choose);

		choose.setVerticalAlignment(JButton.CENTER);

		SpringUtilities.makeCompactGrid(this, 1, 2, 0, 0, 0, 0);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, choose, 1, SpringLayout.VERTICAL_CENTER, field);
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
