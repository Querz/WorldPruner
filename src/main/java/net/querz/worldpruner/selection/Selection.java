package net.querz.worldpruner.selection;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.querz.worldpruner.util.Point;
import java.io.*;

public class Selection {

	protected Long2ObjectOpenHashMap<LongOpenHashSet> selection;
	protected boolean inverted;

	public Selection() {
		this.selection = new Long2ObjectOpenHashMap<>();
		this.inverted = false;
	}

	protected Selection(Long2ObjectOpenHashMap<LongOpenHashSet> selection, boolean inverted) {
		this.selection = selection;
		this.inverted = inverted;
	}

	public static Selection parseCSV(File csvFile) throws IOException {
		Long2ObjectOpenHashMap<LongOpenHashSet> sel = new Long2ObjectOpenHashMap<>();
		Selection selection = new Selection(sel, false);
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String line;
			int num = 0;
			while ((line = br.readLine()) != null) {
				num++;
				if (num == 1 && "inverted".equals(line)) {
					selection.inverted = true;
					continue;
				}

				String[] elements = line.split(";");
				if (elements.length != 2 && elements.length != 4) {
					throw ioException("invalid region or chunk coordinate format in line %d", num);
				}

				Integer x = parseInt(elements[0]);
				Integer z = parseInt(elements[1]);
				if (x == null || z == null) {
					throw ioException("failed to read region coordinates in line %d", num);
				}

				Point region = new Point(x, z);
				if (elements.length == 4) {
					Integer cx = parseInt(elements[2]);
					Integer cz = parseInt(elements[3]);
					if (cx == null || cz == null) {
						throw ioException("failed to read chunk coordinates in line %d", num);
					}

					// check if this chunk is actually in this region
					Point chunk = new Point(cx, cz);
					if (!chunk.chunkToRegion().equals(region)) {
						throw ioException("chunk %s is not in region %s in line %d", chunk, region, num);
					}
					selection.addChunk(chunk);
				} else {
					sel.put(region.asLong(), null);
				}
			}
		}
		return selection;
	}

	private static IOException ioException(String msg, Object... format) {
		return new IOException(String.format(msg, format));
	}

	private static Integer parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	public boolean isChunkSelected(int x, int z) {
		Point pChunk = new Point(x, z);
		Point pRegion = pChunk.chunkToRegion();
		long region = pRegion.asLong();
		if (selection.containsKey(region)) {
			LongOpenHashSet chunks = selection.get(region);
			return (chunks == null || chunks.contains(pChunk.asLong())) != inverted;
		} else {
			return inverted;
		}
	}

	public void addChunk(Point chunk) {
		long r = chunk.chunkToRegion().asLong();
		long c = chunk.asLong();
		addChunk(r, c);
	}

	public void addChunk(long region, long chunk) {
		if (selection.containsKey(region)) {
			LongOpenHashSet chunks = selection.get(region);
			if (chunks != null) {
				chunks.add(chunk);
				if (chunks.size() == 1024) {
					selection.put(region, null);
				}
			}
		} else {
			LongOpenHashSet chunks = new LongOpenHashSet();
			chunks.add(chunk);
			selection.put(region, chunks);
		}
	}

	public void merge(Selection other) {
		for (Long2ObjectMap.Entry<LongOpenHashSet> entry : other.selection.long2ObjectEntrySet()) {
			if (entry.getValue() == null) {
				selection.put(entry.getLongKey(), null);
			} else {
				for (long chunk : entry.getValue()) {
					addChunk(entry.getLongKey(), chunk);
				}
			}
		}
	}
}
