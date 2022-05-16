package net.querz.worldpruner.prune;

import net.querz.worldpruner.selection.Selection;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record PruneData(
		File regionDir,
		File poiDir,
		File entitiesDir,
		long inhabitedTime,
		int radius,
		Selection whitelist,
		boolean continueOnError) {

	private static final Logger LOGGER = LogManager.getLogger(PruneData.class);

	public PruneData(WorldDirectory dir, long inhabitedTime, int radius, Selection whitelist, boolean continueOnError) {
		this(dir.region, dir.poi, dir.entities, inhabitedTime, radius, whitelist, continueOnError);
	}

	public PruneData(WorldDirectory dir, long inhabitedTime, int radius, Selection whitelist) {
		this(dir, inhabitedTime, radius, whitelist, false);
	}

	public static final int MIN_RADIUS = 0;
	public static final int MAX_RADIUS = 128;

	private static final long TICKS_PER_SECOND = 20L;
	private static final Map<Pattern, Long> DURATION_REGEXP = new HashMap<>();

	static {
		DURATION_REGEXP.put(Pattern.compile("(?<data>\\d+)\\W*(?:years?|y)"), 31536000L);
		DURATION_REGEXP.put(Pattern.compile("(?<data>\\d+)\\W*months?"), 2592000L);
		DURATION_REGEXP.put(Pattern.compile("(?<data>\\d+)\\W*(?:days?|d)"), 86400L);
		DURATION_REGEXP.put(Pattern.compile("(?<data>\\d+)\\W*(?:hours?|h)"), 3600L);
		DURATION_REGEXP.put(Pattern.compile("(?<data>\\d+)\\W*(?:minutes?|mins?)"), 60L);
		DURATION_REGEXP.put(Pattern.compile("(?<data>\\d+)\\W*(?:seconds?|secs?|s)"), 1L);
	}

	public static long parseDuration(String d) {
		boolean result = false;
		int duration = 0;
		List<String> elements = new ArrayList<>();
		for (Map.Entry<Pattern, Long> entry : DURATION_REGEXP.entrySet()) {
			Matcher m = entry.getKey().matcher(d);
			if (m.find()) {
				duration += Long.parseLong(m.group("data")) * entry.getValue();
				result = true;

				elements.add(d.substring(m.start(), m.end()));
			}
		}
		if (!result) {
			throw new IllegalArgumentException("could not parse anything from duration string");
		}

		String remains = d;
		for (String element : elements) {
			remains = remains.replaceFirst(element, "");
		}
		remains = remains.trim();
		if (remains.length() > 0) {
			throw new IllegalArgumentException("invalid element in duration string: " + remains);
		}

		return duration;
	}

	public static int parseRadius(String r) {
		int radius = Integer.parseInt(r);
		if (radius < MIN_RADIUS || radius > MAX_RADIUS) {
			throw new IllegalArgumentException("radius out of bounds: " + radius);
		}
		return radius;
	}

	@Nullable
	public static PruneData parseArgs(String... args) {
		Options options = new Options();
		options.addOption(Option.builder("h")
				.longOpt("help")
				.desc("Prints all available commandline options")
				.build());
		options.addOption(Option.builder("t")
				.longOpt("time")
				.hasArg()
				.desc("The minimum time a chunk should have to be kept")
				.build());
		options.addOption(Option.builder("r")
				.longOpt("radius")
				.hasArg()
				.desc("The radius of additional chunks preserved around matching chunks")
				.build());
		options.addOption(Option.builder("w")
				.longOpt("world")
				.hasArg()
				.desc("The path to the world folder")
				.build());
		options.addOption(Option.builder("W")
				.longOpt("white-list")
				.hasArg()
				.desc("The path to whitelist CSV file")
				.build());

		options.addOption(Option.builder("c")
				.longOpt("continue-on-error")
				.desc("If execution should continue if an error occurs")
				.build());

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("help")) {
				printHelp(options);
				return null;
			}

			long inhabitedTime = parseDuration(line.getOptionValue("time", "0s")) * TICKS_PER_SECOND;
			int radius = parseRadius(line.getOptionValue("radius", "0"));
			Selection whitelist = new Selection();
			if (line.hasOption("white-list")) {
				whitelist = Selection.parseCSV(new File(line.getOptionValue("white-list")));
			}
			if (!line.hasOption("world")) {
				LOGGER.error("Missing required argument \"world\"");
				printHelp(options);
				return null;
			}
			File worldDir = new File(line.getOptionValue("world"));
			WorldDirectory world = WorldDirectory.parseWorldDirectory(worldDir);
			if (world == null) {
				LOGGER.error("Could not find world at \"{}\"", worldDir.getAbsolutePath());
				return null;
			}
			return new PruneData(world, inhabitedTime, radius, whitelist, line.hasOption("continue-on-error"));
		} catch (ParseException e) {
			LOGGER.error(e.getMessage());
			printHelp(options);
			return null;
		} catch (IOException e) {
			// This should only happen if the CSV fails parsing
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar WorldPruner.jar <args>", options);
	}

	public record WorldDirectory(File region, File poi, File entities) {
		public static WorldDirectory parseWorldDirectory(File dir) {
			if (!dir.isDirectory()) {
				return null;
			}
			File region = new File(dir, "region");
			File poi = new File(dir, "poi");
			File entities = new File(dir, "entities");
			if (!region.exists()) {
				return null;
			}
			return new WorldDirectory(region, poi.exists() ? poi : null, entities.exists() ? entities : null);
		}
	}
}
