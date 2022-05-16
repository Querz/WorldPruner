package net.querz.worldpruner.prune.structures;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.querz.mca.Chunk;
import net.querz.worldpruner.prune.ErrorHandler;
import net.querz.worldpruner.prune.Pruner;
import net.querz.worldpruner.selection.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class StructureManager {

	private static final Logger LOGGER = LogManager.getLogger(StructureManager.class);

	private final Pruner pruner;
	private final ErrorHandler errorHandler;
	private final Map<StructureID, StructureData> cachedStructures;
	private final HashSet<StructureID> structuresThatShouldBeKept;

	public StructureManager(Pruner pruner, ErrorHandler errorHandler) {
		this.pruner = pruner;
		this.errorHandler = errorHandler;
		this.cachedStructures = new HashMap<>();
		this.structuresThatShouldBeKept = new HashSet<>();
	}

	public void checkChunk(Chunk chunk) {
		List<StructureData> structureData = StructureData.fromChunk(chunk);
		for (StructureData data : structureData) {
			cachedStructures.put(data.structureID(), data);
		}

		if (pruner.skipChunk(chunk)) {
			structuresThatShouldBeKept.addAll(StructureData.getStructureReferences(chunk));
			structuresThatShouldBeKept.addAll(structureData.stream().map(StructureData::structureID).toList());
		}
	}

	public LongOpenHashSet calculateChunksToKeep() {
		LongOpenHashSet chunksToKeep = new LongOpenHashSet();
		for (StructureID structure : structuresThatShouldBeKept) {
			StructureData data = cachedStructures.get(structure);
			if (data == null) {
				LOGGER.warn("Failed find structure {} at {}", structure.id(), new Point(structure.coords()).chunkToBlock());
				continue;
			}
			for (StructureData.BoundingBox boundingBox : data.getBoundingBoxes()) {
				chunksToKeep.addAll(boundingBox.getChunksInside());
			}
		}
		LOGGER.info("Keeping {} chunks containing structures", chunksToKeep.size());
		return chunksToKeep;
	}

}
