package net.querz.worldpruner.selection;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.querz.worldpruner.util.Point;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestSelection {

	@Test
	public void testIsChunkSelected() {
		Long2ObjectOpenHashMap<LongOpenHashSet> sel = new Long2ObjectOpenHashMap<>();

		// complete region
		sel.put(new Point(0, 0).asLong(), null);
		Selection s = new Selection(sel, false);
		assertTrue(s.isChunkSelected(0, 0));
		assertTrue(s.isChunkSelected(31, 31));
		assertFalse(s.isChunkSelected(32, 32));
		assertFalse(s.isChunkSelected(-1, -1));

		// region inverted
		s.inverted = true;
		assertFalse(s.isChunkSelected(0, 0));
		assertFalse(s.isChunkSelected(31, 31));
		assertTrue(s.isChunkSelected(32, 32));
		assertTrue(s.isChunkSelected(-1, -1));

		// individual chunks in a region
		s.inverted = false;
		LongOpenHashSet c = new LongOpenHashSet();
		c.add(new Point(0, 0).asLong());
		c.add(new Point(5, 8).asLong());
		sel.put(new Point(0, 0).asLong(), c);
		assertTrue(s.isChunkSelected(0, 0));
		assertTrue(s.isChunkSelected(5, 8));
		assertFalse(s.isChunkSelected(5, 7));
		assertFalse(s.isChunkSelected(5, 9));
		assertFalse(s.isChunkSelected(4, 8));
		assertFalse(s.isChunkSelected(6, 8));
		assertFalse(s.isChunkSelected(31, 31));
		assertFalse(s.isChunkSelected(32, 32));
		assertFalse(s.isChunkSelected(-1, -1));

		// individual chunks in a region inverted
		s.inverted = true;
		assertFalse(s.isChunkSelected(0, 0));
		assertFalse(s.isChunkSelected(5, 8));
		assertTrue(s.isChunkSelected(5, 7));
		assertTrue(s.isChunkSelected(5, 9));
		assertTrue(s.isChunkSelected(4, 8));
		assertTrue(s.isChunkSelected(6, 8));
		assertTrue(s.isChunkSelected(31, 31));
		assertTrue(s.isChunkSelected(32, 32));
		assertTrue(s.isChunkSelected(-1, -1));
	}
}
