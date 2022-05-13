package net.querz.worldpruner.selection;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestPoint {

	@Nested
	class NormalizeChunkInRegion {

		@Test
		void zero() {
			Point p = new Point(32, 32);
			Point p2 = p.normalizeChunkInRegion();
			assertEquals(new Point(0, 0), p2);
		}

		@Test
		void positive() {
			Point p = new Point(63, 63);
			Point p2 = p.normalizeChunkInRegion();
			assertEquals(new Point(31, 31), p2);
		}

		@Test
		void negative() {
			Point p = new Point(-1, -1);
			Point p2 = p.normalizeChunkInRegion();
			assertEquals(new Point(31, 31), p2);
		}

		@Test
		void negativeLower() {
			Point p = new Point(-31, -31);
			Point p2 = p.normalizeChunkInRegion();
			assertEquals(new Point(1, 1), p2);
		}

		@Test
		void negativeZero() {
			Point p = new Point(-32, -32);
			Point p2 = p.normalizeChunkInRegion();
			assertEquals(new Point(0, 0), p2);
		}

		@Test
		void moreNegative() {
			Point p = new Point(-63, -63);
			Point p2 = p.normalizeChunkInRegion();
			assertEquals(new Point(1, 1), p2);
		}

		@Test
		void moreNegativeZero() {
			Point p = new Point(-64, -64);
			Point p2 = p.normalizeChunkInRegion();
			assertEquals(new Point(0, 0), p2);
		}
	}

	@Test
	void blah() {

		for (int i = 0; i < 1024; i++) {
			int cz = i >> 5;
			int cx = i - cz * 32;

			Point p = new Point(cx, cz);

			System.out.println(p.getAsRelativeChunk());
		}
	}
}
