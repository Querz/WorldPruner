package net.querz.worldpruner.prune.structures;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.querz.mca.Chunk;
import net.querz.nbt.*;
import net.querz.worldpruner.selection.Point;
import net.querz.worldpruner.selection.Selection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record StructureData(StructureID structureID, List<BoundingBox> boundingBoxes) {

	public static StructureData empty(StructureID structureID) {
		return new StructureData(structureID, new ArrayList<>());
	}

	public static CompoundTag getReferences(CompoundTag root) {
		if (root.contains("structures", Tag.COMPOUND)) {
			return root.getCompound("structures").getCompoundOrDefault("References", null);
		}
		if (root.contains("Level", Tag.COMPOUND) && root.getCompound("Level").contains("Structures", Tag.COMPOUND)) {
			return root.getCompound("Level").getCompound("Structures").getCompoundOrDefault("References", null);
		}
		return null;
	}

	public static CompoundTag getStarts(CompoundTag root) {
		if (root.contains("structures", Tag.COMPOUND)) {
			return root.getCompound("structures").getCompoundOrDefault("starts", null);
		}
		if (root.contains("Level", Tag.COMPOUND) && root.getCompound("Level").contains("Structures", Tag.COMPOUND)) {
			return root.getCompound("Level").getCompound("Structures").getCompoundOrDefault("Starts", null);
		}
		return null;
	}

	public static List<StructureID> getStructureReferences(Chunk chunk) {
		List<StructureID> references = new ArrayList<>();
		CompoundTag data = chunk.getData();
		if (data == null) {
			return references;
		}

		CompoundTag referenceTag = getReferences(data);
		if (referenceTag == null) {
			return references;
		}

		for (Map.Entry<String, Tag> entry : referenceTag) {
			if (entry.getValue() instanceof LongArrayTag longTag) {
				for (long refs : longTag.getValue()) {
					references.add(new StructureID(refs, entry.getKey()));
				}
			}
		}
		return references;
	}

	public boolean isChunkInside(Chunk chunk) {
		for (BoundingBox boundingBox : boundingBoxes) {
			if (boundingBox.isIn(chunk)) {
				return true;
			}
		}
		return false;
	}

	public List<BoundingBox> getBoundingBoxes() {
		return boundingBoxes;
	}

	public Selection toSelection() {
		Selection selection = new Selection();
		for (BoundingBox boundingBox : boundingBoxes) {
			for (long point : boundingBox.getChunksInside()) {
				selection.addChunk(point);
			}
		}
		return selection;
	}

	public static List<StructureData> fromChunk(Chunk chunk) {
		List<StructureData> structureList = new ArrayList<>();
		CompoundTag data = chunk.getData();
		if (data == null) {
			return structureList;
		}

		CompoundTag starts = getStarts(data);
		if (starts == null) {
			return structureList;
		}

		for (Map.Entry<String, Tag> tag : starts) {
			if (!(tag.getValue() instanceof CompoundTag structureData)) {
				continue;
			}
			List<BoundingBox> boundingBoxes = new ArrayList<>();
			BoundingBox bb = BoundingBox.fromTag(structureData, "BB");
			if (bb != null) {
				boundingBoxes.add(bb);
			}

			if (!structureData.contains("Children", Tag.LIST)) {
				continue;
			}
			ListTag children = structureData.getList("Children");

			for (Tag child : children) {
				if (!(child instanceof CompoundTag childData)) {
					continue;
				}
				boundingBoxes.add(BoundingBox.fromTag(childData, "BB"));
			}

			structureList.add(new StructureData(new StructureID(new Point(chunk.getX(), chunk.getZ()).asLong(), tag.getKey()), boundingBoxes/*, structureData*/));
		}
		return structureList;
	}

	public static Selection selectionFromChunk(Chunk chunk) {
		Selection selection = new Selection();
		List<StructureData> structureData = fromChunk(chunk);
		for (StructureData structure : structureData) {
			selection.merge(structure.toSelection());
		}
		return selection;
	}

	public record BoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		@Nullable
		static BoundingBox fromTag(CompoundTag tag, String name) {
			if (!tag.contains(name, CompoundTag.INT_ARRAY)) {
				return null;
			}
			int[] ints = tag.getIntArray(name);
			if (ints.length < 6) {
				return null;
			}
			return new BoundingBox(ints[0], ints[1], ints[2], ints[3], ints[4], ints[5]);
		}

		public boolean intersects(BoundingBox other) {
			return minX <= other.maxX && maxX >= other.minX && minY <= other.maxY && maxY >= other.minY && minZ <= other.maxZ && maxZ >= other.minZ;
		}

		public static BoundingBox fromChunk(Chunk chunk) {
			int chunkX = chunk.getX();
			int chunkZ = chunk.getZ();
			return new BoundingBox(chunkX * 16, Integer.MIN_VALUE, chunkZ * 16, chunkX * 16 + 16, Integer.MAX_VALUE, chunkZ * 16 + 16);
		}

		public boolean isIn(Chunk chunk) {
			return intersects(fromChunk(chunk));
		}

		public LongOpenHashSet getChunksInside() {
			LongOpenHashSet chunks = new LongOpenHashSet();
			for (int x = minX - 16; x <= maxX + 16; x += 16) {
				for (int z = minZ - 16; z <= maxZ + 16; z += 16) {
					chunks.add(new Point(x, z).blockToChunk().asLong());
				}
			}
			return chunks;
		}
	}

}
