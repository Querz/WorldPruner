package net.querz.worldpruner.ui;

import net.querz.worldpruner.prune.PruneData;
import net.querz.worldpruner.prune.Pruner;
import net.querz.worldpruner.selection.Selection;
import javax.swing.*;
import java.awt.*;
import java.io.File;

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
		FileTextField worldField = new FileTextField(true, null);
		worldLabel.setLabelFor(worldField);
		options.add(worldLabel);
		options.add(worldField);

		JLabel inhabitedTimeLabel = new JLabel("InhabitedTime: ");
		InhabitedTimeTextField inhabitedTimeField = new InhabitedTimeTextField("5 minutes", 20);
		inhabitedTimeField.setOnUpdate(() -> prune.setEnabled(inhabitedTimeField.isDurationValid()));
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
		FileTextField whitelistField = new FileTextField(false, "csv");
		whitelistLabel.setLabelFor(whitelistField);
		options.add(whitelistLabel);
		options.add(whitelistField);

		SpringUtilities.makeCompactGrid(options, 4, 2, 5, 5, 5, 5);

		panel.add(options, BorderLayout.NORTH);

		JPanel pruneBox = new JPanel();
		pruneBox.setLayout(new BoxLayout(pruneBox, BoxLayout.Y_AXIS));

		ProgressBar progressBar = new ProgressBar();
		pruneBox.add(progressBar);
		pruneBox.add(prune);

		panel.add(pruneBox, BorderLayout.SOUTH);

		prune.addActionListener(e -> new Thread(() -> {
			// TODO: handle world directories being null properly
			PruneData pruneData = new PruneData(PruneData.WorldDirectory.parseWorldDirectory(new File(worldField.getText())), inhabitedTimeField.getDuration(), radiusField.getNumber(), new Selection());
			System.out.println(pruneData);
			new Pruner(pruneData).prune(progressBar);
		}).start());

		INSTANCE.getContentPane().add(panel);
		INSTANCE.pack();
		INSTANCE.setLocationRelativeTo(null);
		INSTANCE.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		INSTANCE.setVisible(true);
	}
}
