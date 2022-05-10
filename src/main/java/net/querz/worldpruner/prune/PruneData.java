package net.querz.worldpruner.prune;

import net.querz.worldpruner.selection.Selection;
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
	Selection whitelist) {

	public PruneData(WorldDirectory dir, long inhabitedTime, int radius, Selection whitelist) {
		this(dir.region, dir.poi, dir.entities, inhabitedTime, radius, whitelist);
	}

	public static final int MIN_RADIUS = 0;
	public static final int MAX_RADIUS = 16;

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

	public static PruneData parseArgs(Map<String, String> args) throws IOException {
		long inhabitedTime = parseDuration(args.getOrDefault("--time", "0s")) * TICKS_PER_SECOND;
		int radius = parseRadius(args.getOrDefault("--radius", "0"));
		File regionDir = new File(args.get("--region"));
		File poiDir = new File(args.get("--poi"));
		File entitiesDir = new File(args.get("--entities"));
		Selection whitelist = new Selection();
		if (args.containsKey("--whitelist")) {
			whitelist = Selection.parseCSV(new File(args.get("--whitelist")));
		}
		return new PruneData(regionDir, poiDir, entitiesDir, inhabitedTime, radius, whitelist);
	}

	public record WorldDirectory(File region, File poi, File entities) {
		public static WorldDirectory parseWorldDirectory(File dir) {
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
