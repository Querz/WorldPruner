package net.querz.worldpruner;

import net.querz.worldpruner.cli.ArgsParser;
import net.querz.worldpruner.cli.Timer;
import net.querz.worldpruner.prune.PruneData;
import net.querz.worldpruner.prune.Pruner;
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
	public static void main(String[] args) throws IOException {
		Map<String, String> parsedArgs = ArgsParser.parse(args);

		PruneData data = PruneData.parseArgs(parsedArgs);
		System.out.println(data);
		Pruner pruner = new Pruner(data);

		Timer t = new Timer();

		pruner.prune();

		System.out.println("pruning took " + t);
	}
}
