package net.querz.worldpruner;

import net.querz.worldpruner.cli.AdvancedCLIProgress;
import net.querz.worldpruner.cli.CLIErrorHandler;
import net.querz.worldpruner.cli.Timer;
import net.querz.worldpruner.prune.PruneData;
import net.querz.worldpruner.prune.Pruner;
import net.querz.worldpruner.ui.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

public class Main {

	private static final Logger LOGGER = LogManager.getLogger(Main.class);

	public static void main(String[] args) {

		if (args.length == 0) {
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
}
