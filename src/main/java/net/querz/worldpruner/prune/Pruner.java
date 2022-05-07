package net.querz.worldpruner.prune;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.querz.mca.Chunk;
import net.querz.mca.MCAFile;
import net.querz.mca.MCAFileHandle;
import net.querz.mca.MCCFileHandler;
import net.querz.mca.seekable.SeekableFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pruner {

	public static final Pattern MCA_FILE_PATTERN = Pattern.compile("^r\\.(?<x>-?\\d+)\\.(?<z>-?\\d+)\\.mca$");

	// TODO: implement a "defragment" function for MCAFile so we don't have to load the entire file
//	private static final TagSelector inhabitedTimeSelector = new TagSelector("InhabitedTime", LongTag.TYPE);

	private final File regionDir, poiDir, entitiesDir;

	private LongOpenHashSet allRegionFiles = new LongOpenHashSet();
	private LongOpenHashSet allPoiFiles = new LongOpenHashSet();
	private LongOpenHashSet allEntityFiles = new LongOpenHashSet();

	public Pruner(File regionDir, File poiDir, File entitiesDir) {
		this.regionDir = regionDir;
		this.poiDir = poiDir;
		this.entitiesDir = entitiesDir;
	}

	private MCAFile loadMCAFile(File file) throws IOException {
		MCAFile mcaFile = new MCAFile(file);
//		mcaFile.load(inhabitedTimeSelector);
		mcaFile.load();
		return mcaFile;
	}

	private boolean skipChunk(Chunk chunk, long inhabitedTime) {
		if (chunk == null || chunk.isEmpty()) {
			return true;
		}
		long chunkInhabitedTime = chunk.getData().getLong("InhabitedTime");
		return chunkInhabitedTime > inhabitedTime;
	}

	private void loadAllFiles() {
		allRegionFiles = loadFiles(regionDir);
		allPoiFiles = loadFiles(poiDir);
		allEntityFiles = loadFiles(entitiesDir);
	}

	private LongOpenHashSet loadFiles(File dir) {
		LongOpenHashSet regions = new LongOpenHashSet();
		dir.list((d, n) -> {
			Matcher m = MCA_FILE_PATTERN.matcher(n);
			if (m.find()) {
				int x = Integer.parseInt(m.group("x"));
				int z = Integer.parseInt(m.group("z"));
				regions.add(toLong(x, z));
				return true;
			}
			return false;
		});
		return regions;
	}

	private long toLong(int x, int z) {
		return (long) x << 32 | z & 0xFFFFFFFFL;
	}

	private Point toPoint(long l) {
		return new Point((int) (l >> 32), (int) l);
	}

	private File toFile(File parent, Point p) {
		return new File(parent, String.format("r.%d.%d.mca", p.x(), p.z()));
	}

	public void prune(long inhabitedTime, int radius, LongOpenHashSet whitelist) {
		// TODO: implement radius. we will store all remaining chunks in temporary files.
		// when we find chunks outside of the current region, we check if there is already a temporary file and we copy
		// all remaining chunks from the external region file into that temp file. at the end we copy all temp files
		// into the source directory, and delete all empty region files.

		loadAllFiles();

		for (long f : allRegionFiles) {
			Point region = toPoint(f);
			File regionFile = toFile(regionDir, region);

			System.out.printf("pruning chunks in %s\n", regionFile);

			MCAFile mcaFile;
			try {
				mcaFile = loadMCAFile(regionFile);
			} catch (IOException ex) {
				System.out.printf("failed to load mca file %s\n", regionFile);
				continue;
			}

			int emptyChunks = 0;
			for (Chunk chunk : mcaFile) {
//				if (chunk != null) {
//					System.out.println("checking chunk " + chunk.getX() + " " + chunk.getZ());
//				}
				if (!skipChunk(chunk, inhabitedTime) && !whitelist.contains(toLong(chunk.getX(), chunk.getZ()))) {
					mcaFile.setChunkAt(chunk.getX(), chunk.getZ(), null);
				}
				if (chunk == null || chunk.isEmpty()) {
					emptyChunks++;
				}
			}

			// delete mca file entirely if it contains no chunks anymore
			if (emptyChunks == 1024) {
				if (!regionFile.delete()) {
					System.out.printf("failed to delete mca file %s\n", regionFile);
					continue;
				}
			}

			File tempFile;
			try {
				tempFile = File.createTempFile(regionFile.getName(), null, null);
			} catch (IOException e) {
				System.out.printf("failed to create temp file for mca file %s\n", regionFile);
				continue;
			}

			try (MCAFileHandle handle = new MCAFileHandle(
				regionFile.getParentFile(),
				new SeekableFile(tempFile, "rw"),
				MCCFileHandler.DEFAULT_HANDLER
			)) {
				mcaFile.save(handle);
			} catch (IOException ex) {
				System.out.printf("failed to save mca file %s\n", regionFile);
				continue;
			}

			try {
				Files.move(tempFile.toPath(), regionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				System.out.printf("failed to move temp mca file %s to %s\n", tempFile, regionFile);
				continue;
			}
		}
	}

	private record Point(int x, int z) {

		@Override
		public String toString() {
			return String.format("<%d, %d>", x, z);
		}
	}
}
