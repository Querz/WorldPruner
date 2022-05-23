package net.querz.worldpruner.selection;

import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import it.unimi.dsi.fastutil.shorts.ShortIterable;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortPredicate;
import javax.annotation.Nonnull;
import java.io.Serializable;

public class ChunkSet implements ShortIterable, Serializable, Cloneable {

	long[] words = new long[16];
	short setBits = 0;

	public static final ChunkSet EMPTY_SET = new ChunkSet().immutable();

	public void set(int index) {
		if (!get(index)) {
			setBits++;
		}
		words[index >> 6] |= (1L << index);
	}

	public void clear(int index) {
		if (get(index)) {
			setBits--;
		}
		words[index >> 6] &= ~(1L << index);
	}

	public void clear() {
		for (int i = 0; i < 16; i++) {
			words[i] = 0L;
		}
	}

	public boolean get(int index) {
		return (words[index >> 6] & (1L << index)) != 0;
	}

	public void merge(ChunkSet other) {
		for (short i = 0; i < 1024; i++) {
			if (other.get(i)) {
				set(i);
			}
		}
	}

	@Override
	public ChunkSet clone() {
		ChunkSet clone;
		try {
			clone = (ChunkSet) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
		clone.words = words.clone();
		clone.setBits = setBits;
		return clone;
	}

	public short size() {
		return setBits;
	}

	public boolean isEmpty() {
		return setBits == 0;
	}

	@Override
	@Nonnull
	public ShortIterator iterator() {
		return new ChunkIterator();
	}

	@Override
	public void forEach(ShortConsumer action) {
		for (short i = 0; i < 1024; i++) {
			if (get(i)) {
				action.accept(i);
			}
		}
	}

	public void removeIf(ShortPredicate predicate) {
		for (short i = 0; i < 1024; i++) {
			if (predicate.test(i)) {
				clear(i);
			}
		}
	}

	private class ChunkIterator implements ShortIterator {

		short index = 0;

		@Override
		public short nextShort() {
			return (short) (index - 1);
		}

		@Override
		public boolean hasNext() {
			while (index < 1024) {
				if (get(index++)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void forEachRemaining(ShortConsumer action) {
			for (; index < 1024; index++) {
				if (get(index)) {
					action.accept(index);
				}
			}
		}
	}

	public ChunkSet immutable() {
		return new ImmutableChunkSet(this);
	}

	private static class ImmutableChunkSet extends ChunkSet {

		ImmutableChunkSet(ChunkSet chunkSet) {
			words = chunkSet.words.clone();
			setBits = chunkSet.setBits;
		}

		@Override
		public void set(int index) {
			throw new UnsupportedOperationException("cannot modify immutable ChunkSet");
		}

		@Override
		public void clear(int index) {
			throw new UnsupportedOperationException("cannot modify immutable ChunkSet");
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException("cannot modify immutable ChunkSet");
		}

		@Override
		public void removeIf(ShortPredicate predicate) {
			throw new UnsupportedOperationException("cannot modify immutable ChunkSet");
		}

		@Override
		public void merge(ChunkSet other) {
			throw new UnsupportedOperationException("cannot modify immutable ChunkSet");
		}
	}
}