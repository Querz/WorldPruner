package net.querz.worldpruner.selection;

import net.querz.nbt.CompoundTag;
import net.querz.nbt.LongTag;
import net.querz.nbt.TagType;
import net.querz.nbt.io.stream.StreamTagVisitor;
import net.querz.nbt.io.stream.TagSelector;
import net.querz.nbt.io.stream.TagTree;
import java.util.*;

public class PrunerSelectionStreamTagVisitor extends StreamTagVisitor {

	private boolean inhabitedTimeDone = false;
	private boolean structuresDone = false;
	private final Set<TagType<?>> wantedTypes;
	private final Deque<TagTree> stack = new ArrayDeque<>();

	private static final TagSelector inhabitedTimeSelectorLegacy = new TagSelector("Level", "InhabitedTime", LongTag.TYPE);
	private static final TagSelector inhabitedTimeSelector = new TagSelector("InhabitedTime", LongTag.TYPE);
	private static final TagSelector structuresSelectorLegacy = new TagSelector("Level", "Structures", CompoundTag.TYPE);
	private static final TagSelector structuresSelector = new TagSelector("structures", CompoundTag.TYPE);

	public PrunerSelectionStreamTagVisitor() {
		this(inhabitedTimeSelectorLegacy, inhabitedTimeSelector, structuresSelectorLegacy, structuresSelector);
	}

	private PrunerSelectionStreamTagVisitor(TagSelector... selectors) {
		Set<TagType<?>> wt = new HashSet<>();
		TagTree tree = new TagTree();

		for (TagSelector selector : selectors) {
			tree.addEntry(selector);
			wt.add(selector.type());
		}

		stack.push(tree);
		wt.add(CompoundTag.TYPE);
		wantedTypes = Collections.unmodifiableSet(wt);
	}

	@Override
	public ValueResult visitRootEntry(TagType<?> type) {
		return type != CompoundTag.TYPE ? ValueResult.RETURN : super.visitRootEntry(type);
	}

	@Override
	public EntryResult visitEntry(TagType<?> type) {
		TagTree tree = stack.element();
		if (depth() > tree.depth()) {
			return super.visitEntry(type);
		} else if (inhabitedTimeDone && structuresDone) {
			return EntryResult.RETURN;
		} else {
			return !wantedTypes.contains(type) ? EntryResult.SKIP : super.visitEntry(type);
		}
	}

	@Override
	public EntryResult visitEntry(TagType<?> type, String name) {
		TagTree tree = stack.element();
		if (depth() > tree.depth()) {
			return super.visitEntry(type, name);
		} else if (tree.selected().remove(name, type)) {
			if (name.equals("InhabitedTime")) {
				if (!structuresDone) {
					if (tree.tree().get("Level") != null) {
						tree.tree().get("Level").selected().remove("InhabitedTime");
					} else {
						tree.selected().remove("InhabitedTime");
					}
				}
				inhabitedTimeDone = true;
			} else {
				if (!inhabitedTimeDone) {
					if (tree.tree().get("Level") != null) {
						tree.tree().get("Level").selected().remove("Structures");
					} else {
						tree.selected().remove("structures");
					}
				}
				structuresDone = true;
			}
			return super.visitEntry(type, name);
		} else {
			if (type == CompoundTag.TYPE) {
				TagTree child = tree.tree().get(name);
				if (child != null) {
					stack.push(child);
					return super.visitEntry(type, name);
				}
			}
			return EntryResult.SKIP;
		}
	}

	@Override
	public ValueResult visitContainerEnd() {
		if (depth() == stack.element().depth()) {
			stack.pop();
		}
		return super.visitContainerEnd();
	}

	public boolean isDone() {
		return inhabitedTimeDone && structuresDone;
	}
}
