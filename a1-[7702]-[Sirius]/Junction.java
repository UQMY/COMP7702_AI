import java.util.HashSet;
import java.util.Set;
/**
 * Class represents each junction.
 * 
 * @author Sirius
 * @version 1.0
 */
public class Junction {

	/** The name of the junction. */
	private String name;
	/** Set of the road accessible from this junction. */
	private Set<Road> roads = new HashSet<Road>();
	/** The last junction in the shortest path to this road. */
	private Junction lastJunction = null;
	/** The length to the start point. */
	private double lengthToRoot = 0;
	/** The last road in the shortest path to this road. */
	private Road lastRoad = null;

	/**
	 * Description: Constructor with one arguments.
	 * 
	 * @param name:
	 *            The name of the junction.
	 */
	public Junction(String name) {
		setName(name);
	}

	/* Accessors and Mutators */
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Set<Road> getRoads() {
		return roads;
	}

	public void setRoads(Set<Road> roads) {
		this.roads = roads;
	}

	public void setLengthToRoot(double lengthToRoot) {
		this.lengthToRoot = lengthToRoot;
	}

	public double getLengthToRoot() {
		return lengthToRoot;
	}

	public Junction getLastJunction() {
		return lastJunction;
	}

	public void setLastJunction(Junction lastJunction) {
		this.lastJunction = lastJunction;
	}

	public Road getLastRoad() {
		return lastRoad;
	}

	public void setLastRoad(Road lastRoad) {
		this.lastRoad = lastRoad;
	}

	public void addRoads(Road road) {
		roads.add(road);
	}

	public void removeRoads(Road road) {
		roads.remove(road);
	}

	/* Reset the junction. */
	public void reset() {
		lastJunction = null;		
		lengthToRoot = 0;	
		lastRoad = null;
	}
	
	@Override
	public String toString() {
		return "Junction [name=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Junction other = (Junction) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
