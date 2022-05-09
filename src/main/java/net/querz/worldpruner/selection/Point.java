package net.querz.worldpruner.selection;

public record Point(int x, int z) {

	public Point() {
		this(0, 0);
	}

	public Point(long l) {
		this((int) (l >> 32), (int) l);
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
		return (long) x << 32 | z & 0xFFFFFFFFL;
	}

	// converts this absolute chunk coordinate into a relative chunk coordinate
	public Point normalizeChunkInRegion() {
		int nx = x % 32;
		nx = nx < 0 ? 32 + nx : nx;
		int nz = z % 32;
		nz = nz < 0 ? 32 + nz : nz;
		return new Point(nx, nz);
	}

	@Override
	public String toString() {
		return String.format("<%d, %d>", x, z);
	}
}