package net.querz.worldpruner.prune;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import net.querz.mca.Chunk;
import net.querz.mca.MCAFile;
import net.querz.mca.MCAFileHandle;
import net.querz.mca.MCCFileHandler;
import net.querz.mca.seekable.SeekableFile;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.Tag;
import net.querz.worldpruner.selection.PrunerSelectionStreamTagVisitor;
import net.querz.worldpruner.selection.Selection;
import net.querz.worldpruner.prune.structures.StructureManager;
import net.querz.worldpruner.selection.Point;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pruner {

	public static final Pattern MCA_FILE_PATTERN = Pattern.compile("^r\\.(?<x>-?\\d+)\\.(?<z>-?\\d+)\\.mca$");

	private final PruneData pruneData;
	private final int radiusSquared;

	private LongOpenHashSet allRegionFiles = new LongOpenHashSet();
	private LongOpenHashSet allPoiFiles = new LongOpenHashSet();
	private LongOpenHashSet allEntityFiles = new LongOpenHashSet();

	private Selection selection;
	private final StructureManager structureManager;

	public Pruner(PruneData pruneData) {
		this.pruneData = pruneData;
		this.radiusSquared = pruneData.radius() * pruneData.radius();
		this.structureManager = new StructureManager(this);
	}

	private MCAFile loadMCAFile(File file) throws IOException {
		MCAFile mcaFile = new MCAFile(file);
		mcaFile.load(new MCAFileHandle(
			file.getParentFile(),
			new SeekableFile(file, "r"),
			MCCFileHandler.DEFAULT_HANDLER,
			PrunerSelectionStreamTagVisitor::new
		));
		return mcaFile;
	}

	public boolean skipChunk(Chunk chunk) {
		if (chunk == null || chunk.isEmpty()) {
			return true;
		}
		if (chunk.getData().contains("Level", Tag.COMPOUND)) {
			CompoundTag level = chunk.getData().getCompound("Level");
			return level.getLongOrDefault("InhabitedTime", Long.MAX_VALUE) > pruneData.inhabitedTime();
		} else {
			return chunk.getData().getLongOrDefault("InhabitedTime", Long.MAX_VALUE) > pruneData.inhabitedTime();
		}
	}

	private void loadAllFiles() {
		allRegionFiles = loadFiles(pruneData.regionDir());
		allPoiFiles = loadFiles(pruneData.poiDir());
		allEntityFiles = loadFiles(pruneData.entitiesDir());
	}

	private LongOpenHashSet loadFiles(File dir) {
		if (dir == null) {
			return new LongOpenHashSet(0);
		}
		LongOpenHashSet regions = new LongOpenHashSet();
		dir.list((d, n) -> {
			Matcher m = MCA_FILE_PATTERN.matcher(n);
			if (m.find()) {
				int x = Integer.parseInt(m.group("x"));
				int z = Integer.parseInt(m.group("z"));
				regions.add(new Point(x, z).asLong());
				return true;
			}
			return false;
		});
		return regions;
	}

	private void deFragment(File file, ShortOpenHashSet whitelist) throws IOException {

		// if the file only contains the header or the whitelist is empty we delete it
		if (file.length() <= 8192 || whitelist != null && whitelist.isEmpty()) {
			file.delete();
			return;
		}

		File tempFile = File.createTempFile(file.getName(), null, null);

		int globalOffset = 2; // chunk data starts at 8192 (after 2 sectors)
		int skippedChunks = 0;

		try (RandomAccessFile temp = new RandomAccessFile(tempFile, "rw");
			 RandomAccessFile source = new RandomAccessFile(file, "r")) {

			for (short i = 0; i < 1024; i++) {
				if (whitelist != null && !whitelist.contains(i)) {
					skippedChunks++;
					continue;
				}

				// read chunk data from source
				source.seek(i * 4);

				int offset = source.read() << 16;
				offset |= (source.read() & 0xFF) << 8;
				offset |= source.read() & 0xFF;

				int sectors = source.read();
				if (offset == 0 || sectors <= 0) {
					skippedChunks++;
					continue;
				}

				source.seek(4096L + i * 4);
				int timestamp = source.readInt();

				source.seek(4096L * offset);
				int dataLength = sectors * 4096;
				BufferedInputStream dis = new BufferedInputStream(new FileInputStream(source.getFD()), dataLength);
				byte[] data = new byte[dataLength];
				dis.read(data);

				// white chunk data to temp
				temp.seek(i * 4L);
				temp.write(globalOffset >>> 16);
				temp.write(globalOffset >> 8 & 0xFF);
				temp.write(globalOffset & 0xFF);

				temp.write(sectors);

				temp.seek(4096 + i * 4L);
				temp.writeInt(timestamp);

				temp.seek(globalOffset * 4096L);
				BufferedOutputStream dos = new BufferedOutputStream(new FileOutputStream(temp.getFD()), dataLength);
				dos.write(data);
				globalOffset += sectors;
			}
		}

		// if we skipped all chunks, we just delete the entire mca file
		if (skippedChunks == 1024) {
			if (tempFile.exists()) {
				tempFile.delete();
			}
			file.delete();
		} else {
			Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private File toFile(File parent, Point p) {
		return new File(parent, String.format("r.%d.%d.mca", p.x(), p.z()));
	}

	public void prune(Progress progress) {
		// we start with the selection being the whitelist
		this.selection = pruneData.whitelist();

		progress.setIndeterminate(true);
		progress.setMessage("Indexing files");
		loadAllFiles();

		progress.setIndeterminate(false);
		progress.setMinimum(0);
		progress.setMaximum(allRegionFiles.size());

		// collect all chunks that need to be kept based on InhabitedTime
		for (long f : allRegionFiles) {
			Point region = new Point(f);
			File regionFile = toFile(pruneData.regionDir(), region);

			System.out.printf("collecting chunks in %s\n", regionFile);

			MCAFile mcaFile;
			try {
				mcaFile = loadMCAFile(regionFile);
			} catch (IOException ex) {
				System.out.printf("failed to load mca file %s\n", regionFile);
				progress.increment(1);
				continue;
			}

			for (Chunk chunk : mcaFile) {
				if (chunk != null) {
					structureManager.checkChunk(chunk);
					// check InhabitedTime with radius
					if (skipChunk(chunk)) {
						Point point = new Point(chunk.getX(), chunk.getZ());
						selection.addChunk(point);
						applyRadius(point);
					}
				}
			}

			progress.increment(1);
		}

		selection.addAll(structureManager.calculateChunksToKeep());

		// remove all chunks that need to be deleted
		deFragmentDir(pruneData.regionDir(), allRegionFiles, progress);
		deFragmentDir(pruneData.poiDir(), allPoiFiles, progress);
		deFragmentDir(pruneData.entitiesDir(), allEntityFiles, progress);

		progress.done();
	}

	private void deFragmentDir(File dir, LongOpenHashSet regions, Progress progress) {
		if (dir == null) {
			return;
		}

		progress.setMinimum(0);
		progress.setMaximum(regions.size());
		progress.setValue(0);
		progress.setIndeterminate(false);
		progress.setMessage("DeFragmenting files in " + dir.getName());

		for (long f : regions) {
			Point region = new Point(f);
			File regionFile = toFile(dir, region);

			System.out.printf("pruning chunks in %s\n", regionFile);

			try {
				ShortOpenHashSet selectedChunks = selection.getSelectedChunks(region);
				deFragment(regionFile, selectedChunks);
			} catch (IOException ex) {
				System.out.printf("failed to defragment mca file %s\n", regionFile);
			}

			progress.increment(1);
		}
	}

	private void applyRadius(Point chunk) {
		Point min = chunk.sub(pruneData.radius());
		Point max = chunk.add(pruneData.radius());
		for (int x = min.x(); x <= max.x(); x++) {
			for (int z = min.z(); z <= max.z(); z++) {
				int h = x - chunk.x();
				int v = z - chunk.z();
				double distSquared = h * h + v * v;
				if (distSquared <= radiusSquared) {
					selection.addChunk(new Point(x, z));
				}
			}
		}
	}
}
