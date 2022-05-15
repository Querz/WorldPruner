package net.querz.worldpruner.cli;

import java.util.HashMap;
import java.util.Map;

public final class ArgsParser {

	private ArgsParser() {}

	public static Map<String, String> parse(String[] args) {
		Map<String, String> result = new HashMap<>();
		for (int i = 0; i < args.length; i += 2) {
			String key = args[i];
			if (i + 1 == args.length) {
				result.put(key, "");
				break;
			}
			if (args[i + 1].startsWith("--")) {
				result.put(key, null);
				i--;
				continue;
			}
			if (!key.startsWith("--")) {
				throw new IllegalArgumentException("invalid argument " + key);
			}
			String value = args[i + 1];
			result.put(key, value);
		}
		return result;
	}
}
