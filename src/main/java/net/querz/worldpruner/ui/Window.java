package net.querz.worldpruner.ui;

import net.querz.worldpruner.Main;
import net.querz.worldpruner.prune.PruneData;
import net.querz.worldpruner.prune.Pruner;
import net.querz.worldpruner.selection.Selection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

public final class Window extends JFrame {

	private static final Logger LOGGER = LogManager.getLogger(Window.class);

	public static Window INSTANCE;

	private JButton prune;
	private JButton pruneForWhitelist;

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

		JMenuBar menuBar = new JMenuBar();
		JMenu info = new JMenu("Info");
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(actionEvent -> {
			AboutDialog.show(INSTANCE);
		});
		info.add(about);
		menuBar.add(info);
		INSTANCE.setJMenuBar(menuBar);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		INSTANCE.prune = new JButton("Prune");
		INSTANCE.prune.setAlignmentX(Component.CENTER_ALIGNMENT);
		INSTANCE.pruneForWhitelist = new JButton("Prune for whitelist");
		INSTANCE.pruneForWhitelist.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel options = new JPanel();
		SpringLayout springLayout = new SpringLayout();
		options.setLayout(springLayout);

		JLabel worldLabel = new JLabel("World: \u24D8");
		worldLabel.setOpaque(true);
		addInfoDialog(worldLabel, """
				<body style="font-family: Sans-Serif; font-size: 12;">
					<span>A World folder is the folder containing all your world files.</span>
					<br/>
					<br/>
					<span>It always contains a sub-folder <b>region</b>, as well as <b>poi</b> and since Minecraft 1.17, <b>entities</b>.</span>
					<br/>
					<br/>
					<span>For The Nether and The End, select the <b>DIM-1</b> and <b>DIM1</b> folders respectively.</span>
				</body>
				""");

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

		JLabel inhabitedTimeLabel = new JLabel("InhabitedTime: \u24D8");
		inhabitedTimeLabel.setOpaque(true);
		addInfoDialog(inhabitedTimeLabel, """
				<body style="font-family: Sans-Serif; font-size: 12;">
					<span>The minimum InhabitedTime of a chunk for WorldPruner to <i>not</i> delete it.</span>
					<br/>
					<br/>
					<span><b>Example:</b></span>
					<br/>
					<span>1 day 3 hours 5 minutes 30 seconds</span>
					<br/>
					<br/>
					<span>In recent versions of Minecraft, InhabitedTime is the <i>accumulated</i> time</span>
					<br/>
					<span>of all players who have spent any time in that chunk.</span>
				</body>
				""");

		InhabitedTimeTextField inhabitedTimeField = new InhabitedTimeTextField("5 minutes", 20);
		inhabitedTimeLabel.setLabelFor(inhabitedTimeField);
		inhabitedTimeField.setHorizontalAlignment(JTextField.CENTER);
		options.add(inhabitedTimeLabel);
		options.add(inhabitedTimeField);

		JLabel radiusLabel = new JLabel("Radius: \u24D8");
		radiusLabel.setOpaque(true);
		addInfoDialog(radiusLabel, """
				<body style="font-family: Sans-Serif; font-size: 12;">
					<span>The radius of additional chunk preserved around matching chunks.</span>
					<br/>
					<br/>
					<span>The maximum allowed radius is <b>128</b>.</span>
					<br/>
					<span>The radius does not affect chunks containing structure data</span>
					<span>but did not match the InhabitedTime condition</span>
				</body>
				""");
		NumberTextField radiusField = new NumberTextField(PruneData.MIN_RADIUS, PruneData.MAX_RADIUS, "0", 20);
		radiusLabel.setLabelFor(radiusField);
		radiusField.setHorizontalAlignment(JTextField.CENTER);
		options.add(radiusLabel);
		options.add(radiusField);

		JLabel whitelistLabel = new JLabel("Whitelist: \u24D8");
		whitelistLabel.setOpaque(true);
		addInfoDialog(whitelistLabel, """
				<body style="font-family: Sans-Serif; font-size: 12;">
					<span>The whitelist contains chunks and regions that need to be kept in <i>any case</i>.</span>
					<br/>
					<br/>
					<span>Whitelists can be created in MCA Selector by using <b>Selection --> Export selection as .csv</b>.</span>
					<br/>
					<span>Custom whitelists can be created by following the format specification <a href="https://github.com/Querz/mcaselector/wiki/Selections#selection-file-format">here</a>.</span>
				</body>
				""");
		FileTextField whitelistField = new FileTextField("csv", "Open Whitelist");
		whitelistField.setInvalidTooltip("Not a csv file");
		whitelistField.setFileValidator((s, f) -> s == null || s.isEmpty() || f.isFile() && f.getName().endsWith(".csv"));
		whitelistLabel.setLabelFor(whitelistField);
		options.add(whitelistLabel);
		options.add(whitelistField);

		Runnable pruneButtonValidator = () -> {
			prune.setEnabled(worldField.isValueValid() && inhabitedTimeField.isDurationValid() && whitelistField.isValueValid());
			pruneForWhitelist.setEnabled(worldField.isValueValid() && whitelistField.isValueValid() && !whitelistField.getText().isEmpty());
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
		pruneBox.add(pruneForWhitelist);

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
				selection = getSelection(csvString);
				if (selection == null) {
					return;
				}
			} else {
				selection = new Selection();
			}

			prune(prune, worldField, inhabitedTimeField.getDuration(), radiusField, whitelistField, progressBar, worldDir, selection);
		});

		pruneForWhitelist.addActionListener(e -> {
			// sanity check, in case someone deleted folders and didn't update the world text field
			worldField.update();
			whitelistField.update();
			pruneButtonValidator.run();
			PruneData.WorldDirectory worldDir = PruneData.WorldDirectory.parseWorldDirectory(new File(worldField.getText()));
			if (!pruneForWhitelist.isEnabled() || worldDir == null) {
				return;
			}

			String csvString = whitelistField.getText();
			Selection selection;
			if (csvString != null && !csvString.isEmpty()) {
				selection = getSelection(csvString);
				if (selection == null) return;
			} else {
				return;
			}

			prune(prune, worldField, inhabitedTimeField.getDuration(), radiusField, whitelistField, progressBar, worldDir, selection);
		});

		INSTANCE.getContentPane().add(panel);
		INSTANCE.pack();
		INSTANCE.setLocationRelativeTo(null);
		INSTANCE.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		// make sure that the AWT Event Queue thread has this ThreadContext map
		SwingUtilities.invokeLater(() -> ThreadContext.put("dynamicLogLevel", Main.getLogLevel()));
		INSTANCE.setVisible(true);
	}

	private static void prune(
			JButton prune,
			FileTextField worldField,
			long inhabitedTime,
			NumberTextField radiusField,
			FileTextField whitelistField,
			ProgressBar progressBar,
			PruneData.WorldDirectory worldDir,
			Selection selection) {

		INSTANCE.setFieldsEnabled(false,
				worldField,
				inhabitedTimeField,
				radiusField,
				whitelistField,
				prune);

		new Thread(() -> {
			try {
				PruneData pruneData = new PruneData(
						worldDir,
						inhabitedTimeField.getDuration(),
						radiusField.getNumber(),
						selection
				);
				DialogErrorHandler errorHandler = new DialogErrorHandler(INSTANCE);
				new Pruner(pruneData, errorHandler).prune(progressBar);
				if (errorHandler.wasSuccessful()) {
					JOptionPane.showMessageDialog(
							INSTANCE,
							"Successfully pruned world",
							"Success",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} finally {
				try {
					SwingUtilities.invokeAndWait(() -> {
						INSTANCE.setFieldsEnabled(true,
								worldField,
								inhabitedTimeField,
								radiusField,
								whitelistField,
								prune);
					});
				} catch (InterruptedException | InvocationTargetException ex) {
					LOGGER.error("Failed to re-enable ui fields", ex);
				}
			}
		}).start();
	}

	private static Selection getSelection(String csvString) {
		File csv = new File(csvString);
		try {
			return Selection.parseCSV(csv);
		} catch (IOException ex) {
			LOGGER.error("Failed to parse whitelist from {}", csv, ex);
			JOptionPane.showMessageDialog(INSTANCE, ex.getMessage(), "Invalid Whitelist", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	private void setFieldsEnabled(boolean enable, JComponent... components) {
		for (JComponent component : components) {
			component.setEnabled(enable);
		}
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

	private static void addInfoDialog(JComponent c, String message) {
		c.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				JEditorPane pane = new JEditorPane();
				pane.setContentType("text/html");
				pane.setText(message);
				pane.addHyperlinkListener(h -> {
					if (HyperlinkEvent.EventType.ACTIVATED.equals(h.getEventType())) {
						Desktop desktop = Desktop.getDesktop();
						try {
							desktop.browse(h.getURL().toURI());
						} catch (Exception ignored) {
						}
					}
				});
				pane.setBackground(c.getBackground());
				pane.setEditable(false);
				JOptionPane.showMessageDialog(INSTANCE, pane, "Info", JOptionPane.PLAIN_MESSAGE);
			}
		});
	}
}
