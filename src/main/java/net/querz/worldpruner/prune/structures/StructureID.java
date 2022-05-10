package net.querz.worldpruner.prune.structures;

record StructureID(long coords, String id) {

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StructureID that = (StructureID) o;
		if (coords != that.coords) return false;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		int result = (int) (coords ^ (coords >>> 32));
		result = 31 * result + id.hashCode();
		return result;
	}
}
