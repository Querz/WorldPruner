package net.querz.worldpruner.selection;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestSelection {

	@Nested
	class IsChunkSelected {

		@Test
		void noInversion() {
			Long2ObjectOpenHashMap<ChunkSet> sel = new Long2ObjectOpenHashMap<>();

			// complete region
			sel.put(new Point(0, 0).asLong(), null);
			Selection s = new Selection(sel, false);
			assertTrue(s.isChunkSelected(0, 0));
			assertTrue(s.isChunkSelected(31, 31));
			assertFalse(s.isChunkSelected(32, 32));
			assertFalse(s.isChunkSelected(-1, -1));
		}

		@Test
		void inversion() {
			Long2ObjectOpenHashMap<ChunkSet> sel = new Long2ObjectOpenHashMap<>();
			sel.put(new Point(0, 0).asLong(), null);
			Selection s = new Selection(sel, true);
			assertFalse(s.isChunkSelected(0, 0));
			assertFalse(s.isChunkSelected(31, 31));
			assertTrue(s.isChunkSelected(32, 32));
			assertTrue(s.isChunkSelected(-1, -1));
		}

		@Test
		void noInversionIndividualChunks() {
			Long2ObjectOpenHashMap<ChunkSet> sel = new Long2ObjectOpenHashMap<>();
			Selection s = new Selection(sel, false);
			ChunkSet c = new ChunkSet();
			c.set(new Point(0, 0).asChunkIndex());
			c.set(new Point(5, 8).asChunkIndex());
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
		}

		@Test
		void inversionIndividualChunks() {
			Long2ObjectOpenHashMap<ChunkSet> sel = new Long2ObjectOpenHashMap<>();
			Selection s = new Selection(sel, true);
			ChunkSet c = new ChunkSet();
			c.set(new Point(0, 0).asChunkIndex());
			c.set(new Point(5, 8).asChunkIndex());
			sel.put(new Point(0, 0).asLong(), c);
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

	@Nested
	class AddChunk {

		@Test
		void noInversion() {
			Selection sel = new Selection();
			sel.addChunk(new Point(0, 0));
			assertTrue(sel.isChunkSelected(0, 0));
			assertFalse(sel.isChunkSelected(0, 1));
		}

		@Test
		void noInversionFillingRegion() {
			Selection sel = new Selection();
			for (int x = 0; x < 32; x++) {
				for (int z = 0; z < 32; z++) {
					if (x != 31 || z != 31) {
						sel.addChunk(new Point(x, z));
					}
				}
			}
			assertEquals(1, sel.selection.size());
			assertEquals(1023, sel.selection.get(new Point(0, 0).asLong()).size());

			sel.addChunk(new Point(31, 31));
			assertEquals(1, sel.selection.size());
			assertNull(sel.selection.get(new Point(0, 0).asLong()));
		}

		@Test
		void inversion() {
			Selection sel = new Selection();
			sel.addChunk(new Point(0, 0));
			sel.inverted = true;
			assertFalse(sel.isChunkSelected(0, 0));
			for (int x = 0; x < 32; x++) {
				for (int z = 0; z < 32; z++) {
					if (x != 0 || z != 0) {
						assertTrue(sel.isChunkSelected(x, z), "expected chunk " + x + "|" + z + " to be selected");
					}
				}
			}
		}

		@Test
		void inversionFillingRegion() {
			Selection sel = new Selection();
			sel.addChunk(new Point(0, 0));
			sel.inverted = true;
			sel.addChunk(new Point(0, 0));
			assertTrue(sel.selection.isEmpty());
			assertAllChunksInRegionSelected(sel, 0, 0);
		}
	}

	@Nested
	class Merge {

		@Test
		void noInversion() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.addChunk(new Point(0, 0));
			b.addChunk(new Point(0, 0));
			b.addChunk(new Point(0, 1));

			a.merge(b);
			assertTrue(a.isChunkSelected(0, 0));
			assertTrue(a.isChunkSelected(0, 1));
			assertFalse(a.isChunkSelected(1, 0));
		}

		@Test
		void noInversionFullRegions() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.selection.put(new Point(0, 0).asLong(), null);
			b.selection.put(new Point(0, 0).asLong(), null);
			b.selection.put(new Point(0, 1).asLong(), null);

			a.merge(b);
			assertAllChunksInRegionSelected(a, 0, 0);
			assertAllChunksInRegionSelected(a, 0, 1);
			assertNoChunkInRegionSelected(a, 1, 0);
		}

		@Test
		void noInversionFullIntoPartial() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.addChunk(new Point(0, 0));
			b.selection.put(new Point(0, 0).asLong(), null);

			a.merge(b);
			assertAllChunksInRegionSelected(a, 0, 0);
			assertNoChunkInRegionSelected(a, 1, 0);
		}

		@Test
		void noInversionPartialIntoFull() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.selection.put(new Point(0, 0).asLong(), null);
			b.addChunk(new Point(0, 0));

			a.merge(b);
			assertAllChunksInRegionSelected(a, 0, 0);
			assertNoChunkInRegionSelected(a, 1, 0);
		}

		@Test
		void inversionIntoNoInversion() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.addChunk(new Point(0, 0));
			b.addChunk(new Point(0, 0));
			b.addChunk(new Point(0, 1));
			b.inverted = true;

			a.merge(b);
			assertTrue(a.isChunkSelected(0, 0));
			assertFalse(a.isChunkSelected(0, 1));
			assertTrue(a.isChunkSelected(1, 0));
			assertTrue(a.inverted);
		}

		@Test
		void inversionIntoNoInversionFullRegions() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.selection.put(new Point(0, 0).asLong(), null);
			b.selection.put(new Point(0, 0).asLong(), null);
			b.selection.put(new Point(0, 1).asLong(), null);
			b.inverted = true;

			a.merge(b);
			assertAllChunksInRegionSelected(a, 0, 0);
			assertNoChunkInRegionSelected(a, 0, 1);
			assertAllChunksInRegionSelected(a, 1, 0);
			assertTrue(a.inverted);
		}

		@Test
		void inversionIntoNoInversionFullIntoPartial() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.addChunk(new Point(0, 0));
			b.selection.put(new Point(0, 0).asLong(), null);
			b.inverted = true;

			a.merge(b);
			assertTrue(a.isChunkSelected(0, 0));
			assertFalse(a.isChunkSelected(0, 1));
			assertFalse(a.isChunkSelected(1, 0));
			assertTrue(a.isChunkSelected(-1, 0));
			assertTrue(a.isChunkSelected(0, -1));
			assertTrue(a.inverted);
		}

		@Test
		void inversionIntoNoInversionPartialIntoFull() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.selection.put(new Point(0, 0).asLong(), null);
			b.addChunk(new Point(0, 0));
			b.inverted = true;

			a.merge(b);
			assertTrue(a.selection.isEmpty());
			assertTrue(a.inverted);
		}

		@Test
		void inversionIntoNoInversionNoOverlap() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.addChunk(new Point(0, 0));
			b.addChunk(new Point(32, 0));
			b.selection.put(new Point(0, 1).asLong(), null);
			b.inverted = true;

			a.merge(b);
			assertTrue(a.inverted);
			assertAllChunksInRegionSelected(a, 0, 0);
			assertNoChunkInRegionSelected(a, 0, 1);
			assertFalse(a.isChunkSelected(32, 0));
			assertTrue(a.isChunkSelected(33, 0));
			assertTrue(a.isChunkSelected(32, 1));
			assertTrue(a.isChunkSelected(32, -1));
		}

		@Test
		void noInversionIntoInversion() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.addChunk(new Point(0, 0));
			a.addChunk(new Point(0, 1));
			b.addChunk(new Point(0, 0));
			a.inverted = true;

			a.merge(b);
			assertTrue(a.isChunkSelected(0, 0));
			assertFalse(a.isChunkSelected(0, 1));
			assertTrue(a.isChunkSelected(1, 0));
			assertTrue(a.inverted);
		}

		@Test
		void noInversionIntoInversionFullRegions() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.selection.put(new Point(0, 0).asLong(), null);
			a.selection.put(new Point(0, 1).asLong(), null);
			b.selection.put(new Point(0, 0).asLong(), null);
			a.inverted = true;

			a.merge(b);
			assertAllChunksInRegionSelected(a, 0, 0);
			assertNoChunkInRegionSelected(a, 0, 1);
			assertAllChunksInRegionSelected(a, 1, 0);
			assertTrue(a.inverted);
		}

		@Test
		void noInversionIntoInversionFullIntoPartial() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.addChunk(new Point(0, 0));
			b.selection.put(new Point(0, 0).asLong(), null);
			a.inverted = true;

			a.merge(b);
			assertTrue(a.selection.isEmpty());
			assertTrue(a.inverted);
		}

		@Test
		void noInversionIntoInversionPartialIntoFull() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.selection.put(new Point(0, 0).asLong(), null);
			b.addChunk(new Point(0, 0));
			a.inverted = true;

			a.merge(b);
			assertTrue(a.isChunkSelected(0, 0));
			assertFalse(a.isChunkSelected(0, 1));
			assertFalse(a.isChunkSelected(1, 0));
			assertTrue(a.isChunkSelected(-1, 0));
			assertTrue(a.isChunkSelected(0, -1));
			assertTrue(a.inverted);
		}

		@Test
		void inversionIntoInversion() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.addChunk(new Point(0, 0));
			a.addChunk(new Point(0, 1));
			b.addChunk(new Point(0, 0));
			a.inverted = true;
			b.inverted = true;

			a.merge(b);
			assertFalse(a.isChunkSelected(0, 0));
			assertTrue(a.isChunkSelected(0, 1));
			assertTrue(a.isChunkSelected(1, 0));
			assertTrue(a.inverted);
		}

		@Test
		void inversionIntoInversionFullRegions() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.selection.put(new Point(0, 0).asLong(), null);
			a.selection.put(new Point(0, 1).asLong(), null);
			b.selection.put(new Point(0, 0).asLong(), null);
			a.inverted = true;
			b.inverted = true;

			a.merge(b);
			assertNoChunkInRegionSelected(a, 0, 0);
			assertAllChunksInRegionSelected(a, 0, 1);
			assertAllChunksInRegionSelected(a, 1, 0);
			assertTrue(a.inverted);
		}

		@Test
		void inversionIntoInversionFullIntoPartial() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.addChunk(new Point(0, 0));
			b.selection.put(new Point(0, 0).asLong(), null);
			a.inverted = true;
			b.inverted = true;

			a.merge(b);
			assertFalse(a.isChunkSelected(0, 0));
			assertTrue(a.isChunkSelected(1, 0));
			assertTrue(a.isChunkSelected(0, 1));
			assertTrue(a.isChunkSelected(-1, 0));
			assertTrue(a.isChunkSelected(0, -1));
			assertTrue(a.inverted);
		}

		@Test
		void inversionIntoInversionPartialIntoFull() {
			Selection a = new Selection();
			Selection b = new Selection();
			a.selection.put(new Point(0, 0).asLong(), null);
			b.addChunk(new Point(0, 0));
			a.inverted = true;
			b.inverted = true;

			a.merge(b);
			assertFalse(a.isChunkSelected(0, 0));
			assertTrue(a.isChunkSelected(1, 0));
			assertTrue(a.isChunkSelected(0, 1));
			assertTrue(a.isChunkSelected(-1, 0));
			assertTrue(a.isChunkSelected(0, -1));
			assertTrue(a.inverted);
		}

		@Test
		void inversionIntoInversionYouCompleteMe() {
			Selection a = new Selection();
			Selection b = new Selection();
			for (int x = 0; x < 32; x++) {
				for (int z = 0; z < 32; z++) {
					if (x != 31 || z != 31) {
						a.addChunk(new Point(x, z));
					}
				}
			}
			b.addChunk(new Point(31, 31));
			a.inverted = true;
			b.inverted = true;

			a.merge(b);
			assertTrue(a.selection.isEmpty());
			assertTrue(a.inverted);
		}

	}

	static void assertAllChunksInRegionSelected(Selection s, int x, int z) {
		Point zero = new Point(x, z).regionToChunk();
		for (int cx = zero.x(); cx < zero.x() + 32; cx++) {
			for (int cz = zero.z(); cz < zero.z() + 32; cz++) {
				assertTrue(s.isChunkSelected(cx, cz), "expected all chunks in region " + x + "|" + z + " to be selected, but " + cx + "|" + cz + " is not");
			}
		}
	}

	static void assertNoChunkInRegionSelected(Selection s, int x, int z) {
		Point zero = new Point(x, z).regionToChunk();
		for (int cx = zero.x(); cx < zero.x() + 32; cx++) {
			for (int cz = zero.z(); cz < zero.z() + 32; cz++) {
				assertFalse(s.isChunkSelected(cx, cz), "expected all chunks in region " + x + "|" + z + " not to be selected, but " + cx + "|" + cz + " is");
			}
		}
	}
}
