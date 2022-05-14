package net.querz.worldpruner.ui;

import net.querz.worldpruner.prune.PruneData;
import net.querz.worldpruner.prune.Pruner;
import net.querz.worldpruner.selection.Selection;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public final class Window extends JFrame {

	public static Window INSTANCE;

	public static void create() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			throw new RuntimeException(e);
		}

		INSTANCE = new Window();
		INSTANCE.setTitle("World Pruner");
		INSTANCE.setSize(500, 250);
		INSTANCE.setMinimumSize(new Dimension(500, 250));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JButton prune = new JButton("Prune");
		prune.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel options = new JPanel();
		SpringLayout springLayout = new SpringLayout();
		options.setLayout(springLayout);

		JLabel worldLabel = new JLabel("World: ");
		FileTextField worldField = new FileTextField(null, "Open World");
		worldField.setInvalidTooltip("Not a valid Minecraft world folder");
		worldField.setFileValidator((s, f) -> {
			if (!f.isDirectory()) {
				return false;
			}
			File[] contents = f.listFiles((d, n) -> d.isDirectory() && (n.equals("region") || n.equals("poi") || n.equals("entities")));
			return contents != null && contents.length > 0;
		});
		worldLabel.setLabelFor(worldField);
		options.add(worldLabel);
		options.add(worldField);

		JLabel inhabitedTimeLabel = new JLabel("InhabitedTime: ");
		InhabitedTimeTextField inhabitedTimeField = new InhabitedTimeTextField("5 minutes", 20);
		inhabitedTimeLabel.setLabelFor(inhabitedTimeField);
		inhabitedTimeField.setHorizontalAlignment(JTextField.CENTER);
		options.add(inhabitedTimeLabel);
		options.add(inhabitedTimeField);

		JLabel radiusLabel = new JLabel("Radius: ");
		NumberTextField radiusField = new NumberTextField(PruneData.MIN_RADIUS, PruneData.MAX_RADIUS, "0", 20);
		radiusLabel.setLabelFor(radiusField);
		radiusField.setHorizontalAlignment(JTextField.CENTER);
		options.add(radiusLabel);
		options.add(radiusField);

		JLabel whitelistLabel = new JLabel("Whitelist: ");
		FileTextField whitelistField = new FileTextField("csv", "Open Whitelist");
		whitelistField.setInvalidTooltip("Not a csv file");
		whitelistField.setFileValidator((s, f) -> s == null || s.isEmpty() || f.isFile() && f.getName().endsWith(".csv"));
		whitelistLabel.setLabelFor(whitelistField);
		options.add(whitelistLabel);
		options.add(whitelistField);

		Runnable pruneButtonValidator = () -> {
			prune.setEnabled(worldField.isValueValid() && inhabitedTimeField.isDurationValid() && whitelistField.isValueValid());
		};

		worldField.setOnUpdate(pruneButtonValidator);
		inhabitedTimeField.setOnUpdate(pruneButtonValidator);
		whitelistField.setOnUpdate(pruneButtonValidator);

		SpringUtilities.makeCompactGrid(options, 4, 2, 5, 5, 5, 5);

		panel.add(options, BorderLayout.NORTH);

		JPanel pruneBox = new JPanel();
		pruneBox.setLayout(new BoxLayout(pruneBox, BoxLayout.Y_AXIS));

		ProgressBar progressBar = new ProgressBar();
		pruneBox.add(progressBar);
		pruneBox.add(prune);

		panel.add(pruneBox, BorderLayout.SOUTH);

		prune.addActionListener(e -> {
			// sanity check, in case someone deleted folders and didn't update the world text field
			worldField.update();
			whitelistField.update();
			pruneButtonValidator.run();
			PruneData.WorldDirectory worldDir = PruneData.WorldDirectory.parseWorldDirectory(new File(worldField.getText()));
			if (!prune.isEnabled() || worldDir == null) {
				return;
			}

			String csvString = whitelistField.getText();
			Selection selection;
			if (csvString != null && !csvString.isEmpty()) {
				try {
					selection = Selection.parseCSV(new File(csvString));
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(INSTANCE, ex.getMessage(), "Invalid Whitelist", JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				selection = new Selection();
			}


			new Thread(() -> {
				PruneData pruneData = new PruneData(
					worldDir,
					inhabitedTimeField.getDuration(),
					radiusField.getNumber(),
					selection
				);
				new Pruner(pruneData).prune(progressBar);
			}).start();
		});

		INSTANCE.getContentPane().add(panel);
		INSTANCE.pack();
		INSTANCE.setLocationRelativeTo(null);
		INSTANCE.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		INSTANCE.setVisible(true);
	}
}
