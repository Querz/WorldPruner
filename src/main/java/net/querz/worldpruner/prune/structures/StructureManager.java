package net.querz.worldpruner.prune.structures;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.querz.mca.Chunk;
import net.querz.worldpruner.prune.Pruner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

public class StructureManager {

    private static final Logger LOGGER = LogManager.getLogger(StructureManager.class);

    private Pruner pruner;
    private Map<StructureID, StructureData> cachedStructures;
    private HashSet<StructureID> structuresThatShouldBeKept;

    public StructureManager(Pruner pruner) {
        this.pruner = pruner;
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
            structuresThatShouldBeKept.addAll(structureData.stream().map(StructureData::structureID).toList()); //TODO check if this is necessary
        }
    }

    public LongOpenHashSet calculateChunksToKeep() {
        LongOpenHashSet chunksToKeep = new LongOpenHashSet();
        for (StructureID structure : structuresThatShouldBeKept) {
            StructureData data = cachedStructures.get(structure);
            if (data == null) {
                //TODO log error
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
