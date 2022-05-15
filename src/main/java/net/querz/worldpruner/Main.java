package net.querz.worldpruner;

import net.querz.worldpruner.cli.ArgsParser;
import net.querz.worldpruner.cli.CLIErrorHandler;
import net.querz.worldpruner.cli.CLIProgress;
import net.querz.worldpruner.cli.Timer;
import net.querz.worldpruner.prune.PruneData;
import net.querz.worldpruner.prune.Pruner;
import net.querz.worldpruner.ui.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.Map;

public class Main {

// (To spare chunks from pruning)
// InhabitedTime Required        3 Minutes
// Additional radius             2 Chunks
// Whitelisted Chunks/Regions	 A Table Of Entries
// Run Pruning
//
// Prune all except whitelist	Run Pruning


// cmd --time <duration> --radius <int> --whitelist [csv]

	private static final Logger LOGGER = LogManager.getLogger(Main.class);

	public static void main(String[] args) throws IOException {

		if (args.length == 0) {
			Window.create();
			return;
		} else {
			Map<String, String> parsedArgs = ArgsParser.parse(args);

			PruneData data = PruneData.parseArgs(parsedArgs);

			Pruner pruner = new Pruner(data);

			Timer t = new Timer();

			pruner.prune(new CLIProgress(), new CLIErrorHandler(parsedArgs.containsKey("--continue-on-error")));

			LOGGER.info("pruning took " + t);
		}
	}
}
