package net.querz.worldpruner;

import net.querz.worldpruner.cli.AdvancedCLIProgress;
import net.querz.worldpruner.cli.CLIErrorHandler;
import net.querz.worldpruner.cli.Timer;
import net.querz.worldpruner.prune.PruneData;
import net.querz.worldpruner.prune.Pruner;
import net.querz.worldpruner.ui.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

public class Main {

	private static final Logger LOGGER = LogManager.getLogger(Main.class);

	public static void main(String[] args) throws InterruptedException {

		if (args.length == 0 || args.length == 1 && args[0].equals("--debug") || args[0].equals("-d")) {
			if (args.length == 1) {
				logLevel = "DEBUG";
			}
			ThreadContext.put("dynamicLogLevel", getLogLevel());
			Window.create();
		} else {
			PruneData data = PruneData.parseArgs(args);
			if (data == null) {
				// Exiting because either the args are wrong or "--help" was entered
				return;
			}

			Pruner pruner = new Pruner(data, new CLIErrorHandler(data.continueOnError()));

			Timer t = new Timer();

			pruner.prune(new AdvancedCLIProgress());

			LOGGER.info("Pruning took " + t);
		}
	}

	private static String logLevel = "INFO";

	public static String getLogLevel() {
		return logLevel;
	}

	private static String version;

	public static String getVersion() {
		if (version == null) {
			try {
				version = readVersion();
			} catch (IOException e) {
				LOGGER.error("Could not read version from manifest: {}", e.getMessage());
				version = "N/A";
			}
		}
		return version;
	}

	private static String readVersion() throws IOException {
		Enumeration<URL> resources = Main.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
		if (resources.hasMoreElements()) {
			Manifest manifest = new Manifest(resources.nextElement().openStream());
			String version = manifest.getMainAttributes().getValue("Implementation-Version");
			if (version != null) {
				return version;
			} else {
				// Leave version empty when in dev environment
				return "";
			}
		}
		throw new IOException("Could not find MANIFEST.MF");
	}

}
