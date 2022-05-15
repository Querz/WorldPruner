package net.querz.worldpruner.selection;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import java.io.*;

public class Selection {

	protected Long2ObjectOpenHashMap<ShortOpenHashSet> selection;
	protected boolean inverted;

	public Selection() {
		this.selection = new Long2ObjectOpenHashMap<>();
		this.inverted = false;
	}

	protected Selection(Long2ObjectOpenHashMap<ShortOpenHashSet> selection, boolean inverted) {
		this.selection = selection;
		this.inverted = inverted;
	}

	public static Selection parseCSV(File csvFile) throws IOException {
		Long2ObjectOpenHashMap<ShortOpenHashSet> sel = new Long2ObjectOpenHashMap<>();
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
		return isChunkSelected(pRegion.asLong(), pChunk.getAsRelativeChunk());
	}

	protected boolean isChunkSelected(long region, short chunk) {
		if (selection.containsKey(region)) {
			ShortOpenHashSet chunks = selection.get(region);
			return (chunks == null || chunks.contains(chunk)) != inverted;
		} else {
			return inverted;
		}
	}

	public void addChunk(Point chunk) {
		addChunk(chunk.chunkToRegion().asLong(), chunk.getAsRelativeChunk());
	}


	public void addChunk(long chunkCoords) {
		Point chunk = new Point(chunkCoords);
		addChunk(chunk.chunkToRegion().asLong(), chunk.getAsRelativeChunk());
	}

	protected void addChunk(long region, short chunk) {
		if (inverted && isChunkSelected(region, chunk)) {
			return;
		}
		if (selection.containsKey(region)) {
			ShortOpenHashSet chunks = selection.get(region);
			if (chunks != null) {
				if (inverted) {
					chunks.remove(chunk);
				} else {
					chunks.add(chunk);
				}
				if (chunks.size() == 1024) {
					selection.put(region, null);
				} else if (chunks.size() == 0) {
					selection.remove(region);
				}
			}
		} else {
			ShortOpenHashSet chunks = new ShortOpenHashSet();
			chunks.add(chunk);
			selection.put(region, chunks);
		}
	}

	public void addRegion(long region) {
		if (inverted) {
			selection.remove(region);
		} else {
			selection.put(region, null);
		}
	}

	public boolean isRegionSelected(long region) {
		if (inverted) {
			return !selection.containsKey(region);
		} else {
			return selection.containsKey(region) && selection.get(region) == null;
		}
	}

	public ShortOpenHashSet getSelectedChunks(Point region) {
		if (inverted) {
			if (selection.containsKey(region.asLong())) {
				return invertChunks(selection.get(region.asLong()));
			} else {
				return null;
			}
		}
		if (selection.containsKey(region.asLong())) {
			return selection.get(region.asLong());
		} else {
			return new ShortOpenHashSet(0);
		}
	}

	private static ShortOpenHashSet invertChunks(ShortOpenHashSet chunks) {
		if (chunks == null) {
			return new ShortOpenHashSet(0);
		}
		ShortOpenHashSet result = new ShortOpenHashSet(1024 - chunks.size());
		for (short i = 0; i < 1024; i++) {
			if (!chunks.contains(i)) {
				result.add(i);
			}
		}
		return result;
	}

	public void merge(Selection other) {
		if (!inverted && !other.inverted) {

			for (Long2ObjectMap.Entry<ShortOpenHashSet> entry : other.selection.long2ObjectEntrySet()) {
				long r = entry.getLongKey();
				if (selection.containsKey(r)) {
					selection.put(r, add(selection.get(r), entry.getValue()));
				} else {
					selection.put(r, cloneValue(entry.getValue()));
				}
			}
		} else if (inverted && !other.inverted) {
			// subtract all other chunks from this selection
			for (Long2ObjectMap.Entry<ShortOpenHashSet> entry : other.selection.long2ObjectEntrySet()) {
				long r = entry.getLongKey();
				if (selection.containsKey(r)) {
					ShortOpenHashSet result = subtract(selection.get(r), entry.getValue());
					if (result.size() == 0) {
						selection.remove(r);
					} else {
						selection.put(r, result);
					}
				}
			}
		} else if (!inverted) { // this selection is not inverted but the other is
			// if something is marked in the other selection, we add it to this selection
			// remember the regions we already touched and ignore them in the next loop
			for (Long2ObjectMap.Entry<ShortOpenHashSet> entry : other.selection.long2ObjectEntrySet()) {
				long r = entry.getLongKey();
				if (selection.containsKey(r)) {
					ShortOpenHashSet result;
					if ((result = subtract(cloneValue(entry.getValue()), selection.get(r))).size() == 0) {
						selection.remove(r);
					} else {
						selection.put(r, result);
					}
				} else {
					selection.put(r, cloneValue(entry.getValue()));
				}
			}

			// if something is marked in this selection, but not the other, we remove it so it's marked when inverted
			for (Long2ObjectMap.Entry<ShortOpenHashSet> entry : selection.long2ObjectEntrySet()) {
				long r = entry.getLongKey();
				if (!other.selection.containsKey(r)) {
					selection.remove(r);
				}
			}
			// invert this selection at the end
			inverted = true;
		} else { // both are inverted

			for (Long2ObjectMap.Entry<ShortOpenHashSet> entry : selection.long2ObjectEntrySet()) {
				long r = entry.getLongKey();
				if (!other.selection.containsKey(r)) {
					// region does not exist in other selection, so it is fully marked.
					// we have to delete it from this selection to mark it too.
					selection.remove(r);
				} else {
					// region exists in other selection so we need to union them
					ShortOpenHashSet union = union(entry.getValue(), other.selection.get(r));
					// and put it in this selection
					if (union != null && union.size() == 0) {
						// the union is completely selected, so we remove this region to fully mark it
						selection.remove(r);
					} else {
						selection.put(r, union);
					}
				}
			}
		}
	}

	private static ShortOpenHashSet cloneValue(ShortOpenHashSet v) {
		return v == null ? null : v.clone();
	}

	private static ShortOpenHashSet union(ShortOpenHashSet a, ShortOpenHashSet b) {
		if (a == null) {
			return cloneValue(b);
		}
		if (b == null) {
			return a;
		}
		for (short l : a) {
			if (!b.contains(l)) {
				a.remove(l);
			}
		}
		for (short l : b) {
			if (!a.contains(l)) {
				a.remove(l);
			}
		}
		return a;
	}

	private static ShortOpenHashSet subtract(ShortOpenHashSet source, ShortOpenHashSet target) {
		if (source == null) {
			return invertChunks(target);
		}
		if (target == null) {
			return new ShortOpenHashSet(0);
		}
		source.removeIf(target::contains);
		return source;
	}

	private static ShortOpenHashSet add(ShortOpenHashSet source, ShortOpenHashSet target) {
		if (source == null || target == null) {
			return null;
		}
		source.addAll(target);
		return source.size() == 1024 ? null : source;
	}

	public void addAll(LongOpenHashSet entries) {
		for (long entry : entries) {
			addChunk(entry);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (inverted) {
			sb.append("inverted\n");
		}
		for (Long2ObjectMap.Entry<ShortOpenHashSet> e : selection.long2ObjectEntrySet()) {
			Point region = new Point(e.getLongKey());
			if (e.getValue() == null) {
				sb.append(region.x()).append(';').append(region.z()).append('\n');
			} else {
				for (short c : e.getValue()) {
					Point chunk = new Point(c).add(region.regionToChunk());
					sb.append(region.x()).append(';').append(region.z()).append(';').append(chunk.x()).append(';').append(chunk.z()).append('\n');
				}
			}
		}
		return sb.toString();
	}
}
