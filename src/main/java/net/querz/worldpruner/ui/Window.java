package net.querz.worldpruner.ui;

import net.querz.worldpruner.Main;
import net.querz.worldpruner.prune.PruneData;
import net.querz.worldpruner.prune.Pruner;
import net.querz.worldpruner.selection.Selection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public final class Window extends JFrame {

	private static final Logger LOGGER = LogManager.getLogger(Window.class);

	public static Window INSTANCE;

	private Window() {
	}

	public static void create() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			throw new RuntimeException(e);
		}

		INSTANCE = new Window();
		INSTANCE.setTitle("World Pruner " + Main.getVersion());
		INSTANCE.setSize(500, 250);
		INSTANCE.setMinimumSize(new Dimension(500, 250));

		// icons
		INSTANCE.setIconImages(INSTANCE.loadIcons());

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
			String[] contents = f.list((d, n) -> d.isDirectory() && (n.equals("region") || n.equals("poi") || n.equals("entities")));
			if (contents == null || contents.length == 0 || !Arrays.asList(contents).contains("region")) {
				return false;
			}
			worldField.setValidTooltip(formatValidWorldTooltip(contents));
			return true;
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
				File csv = new File(csvString);
				try {
					selection = Selection.parseCSV(csv);
				} catch (IOException ex) {
					LOGGER.error("Failed to parse whitelist from {}", csv, ex);
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
				new Pruner(pruneData, new DialogErrorHandler(INSTANCE)).prune(progressBar);
			}).start();
		});

		INSTANCE.getContentPane().add(panel);
		INSTANCE.pack();
		INSTANCE.setLocationRelativeTo(null);
		INSTANCE.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		INSTANCE.setVisible(true);
	}

	private List<Image> loadIcons() {
		List<Image> images = new ArrayList<>();
		for (int res = 16; res <= 128; res *= 2) {
			try {
				images.add(ImageIO.read(Objects.requireNonNull(getClass().getResource(String.format("/img/icon/%dx%d.png", res, res)))));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return images;
	}

	private static String formatValidWorldTooltip(String[] subDirs) {
		return switch (subDirs.length) {
			case 1 -> "Found world sub-directory " + subDirs[0];
			case 2 -> "Found world sub-directories " + subDirs[0] + " and " + subDirs[1];
			case 3 -> "Found world sub-directories " + subDirs[0] + ", " + subDirs[1] + " and " + subDirs[2];
			default -> "Found world sub-directories " + String.join(", ", subDirs);
		};
	}
}
