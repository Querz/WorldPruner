package net.querz.worldpruner.selection;

public record Point(int x, int z) {

	public Point() {
		this(0, 0);
	}

	public Point(long l) {
		this((int) l, (int) (l >> 32));
	}

	public Point(short relativeChunk) {
		this(relativeChunk, relativeChunk >> 5);
	}

	public Point add(int x, int z) {
		return new Point(this.x + x, this.z + z);
	}

	public Point add(Point p) {
		return add(p.x, p.z);
	}

	public Point add(int i) {
		return add(i, i);
	}

	public Point sub(int x, int z) {
		return new Point(this.x - x, this.z - z);
	}

	public Point sub(Point p) {
		return sub(p.x, p.z);
	}

	public Point sub(int i) {
		return sub(i, i);
	}

	public Point mul(int x, int z) {
		return new Point(this.x * x, this.z * z);
	}

	public Point mul(Point p) {
		return mul(p.x, p.z);
	}

	public Point mul(int i) {
		return mul(i, i);
	}

	public Point div(int x, int z) {
		return new Point(this.x / x, this.z / z);
	}

	public Point div(float x, float z) {
		return new Point((int) (this.x / x), (int) (this.z / z));
	}

	public Point div(Point p) {
		return div(p.x, p.z);
	}

	public Point div(int i) {
		return div(i, i);
	}

	public Point div(float f) {
		return div(f, f);
	}

	public Point mod(int x, int z) {
		return new Point(this.x % x, this.z % z);
	}

	public Point mod(float x, float z) {
		return new Point((int) (this.x % x), (int) (this.z % z));
	}

	public Point mod(Point p) {
		return mod(p.x, p.z);
	}

	public Point mod(int i) {
		return mod(i, i);
	}

	public Point mod(float f) {
		return mod(f, f);
	}

	public Point and(int i) {
		return new Point(x & i, z & i);
	}

	public Point abs() {
		return new Point(x < 0 ? x * -1 : x, z < 0 ? z * -1 : z);
	}

	public Point shiftRight(int i) {
		return new Point(x >> i, z >> i);
	}

	public Point shiftLeft(int i) {
		return new Point(x << i, z << i);
	}

	public Point blockToRegion() {
		return shiftRight(9);
	}

	public Point regionToBlock() {
		return shiftLeft(9);
	}

	public Point regionToChunk() {
		return shiftLeft(5);
	}

	public Point blockToChunk() {
		return shiftRight(4);
	}

	public Point chunkToBlock() {
		return shiftLeft(4);
	}

	public Point chunkToRegion() {
		return shiftRight(5);
	}

	public long asLong() {
		return (long) z << 32 | x & 0xFFFFFFFFL;
	}

	// converts this absolute chunk coordinate into a relative chunk coordinate
	public Point normalizeChunkInRegion() {
		return new Point(x & 0x1F, z & 0x1F);
	}

	// returns a short containing relative chunk coordinates (0|0 to 31|31).
	// only the first 1024 bits are populated which allows easy looping without having to convert.
	public short getAsRelativeChunk() {
		Point n = normalizeChunkInRegion();
		return (short) (n.z << 5 | n.x & 0x1F);
	}
}