/**
 * Class represents each road.
 * 
 * @author Sirius
 * @version 1.0
 */
public class Road {
	/** The name of the road. */
	private String name;
	/** The first junction of the road. */
	private Junction junction1;
	/** The second junction of the road. */
	private Junction junction2;
	/** The length of the road. */
	private int roadLength;
	/** The number of lots in the road. */
	private int nLots;

	/**
	 * Description: Constructor with five arguments.
	 * 
	 * @param name:
	 *            The name of the team.
	 * @param junction1:
	 *            The name of the team.
	 * @param junction2:
	 *            The name of the team.
	 * @param roadLength:
	 *            The name of the team.
	 * @param nLots:
	 *            The name of the team.
	 */
	public Road(String name, Junction junction1, Junction junction2, int roadLength, int nLots) {
		this.name = name;
		this.junction1 = junction1;
		this.junction2 = junction2;
		this.roadLength = roadLength;
		this.nLots = nLots;
	}

	/* Accessors and Mutators */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Junction getJunction1() {
		return junction1;
	}

	public void setJunction1(Junction junction1) {
		this.junction1 = junction1;
	}

	public Junction getJunction2() {
		return junction2;
	}

	public void setJunction2(Junction junction2) {
		this.junction2 = junction2;
	}

	public int getRoadLength() {
		return roadLength;
	}

	public void setRoadLength(int roadLength) {
		this.roadLength = roadLength;
	}

	public int getNLots() {
		return nLots;
	}

	public void setNLots(int nLots) {
		this.nLots = nLots;
	}

	/**
	 * Description: Gets another junction of the road.
	 * 
	 * @param junction:
	 *            The original junction used to get another junction.
	 * 
	 * @return junction:
	 * 			  Returns junction1 or junction2 according to the given junction.
	 */
	public Junction getAnotherJunction(Junction junction) {
		if (junction.equals(junction1)) {
			return junction2;
		} else {
			return junction1;
		}
	}

	/**
	 * Description: Calculates the length between the given junction and lot.
	 * 
	 * @param junction:
	 *            The junction to be calculated.
	 * @param lotNumber:
	 *            The number of the lot to be calculated.
	 */
	public double lengthToLot(Junction junction, int lotNumber) {
		double length = (2.0 * roadLength / nLots) * ((lotNumber - 1) / 2 + 0.5);
		if (junction.getName().equals(junction2.getName())) {
			length = roadLength - length;
		}
		return length;
	}

	/**
	 * Description: Calculates the length between the given junction and lot.
	 * 
	 * @param lotNumber1:
	 *            The number of the first lot to be calculated.
	 * @param lotNumber2:
	 *            The number of the second lot to be calculated.
	 */
	public double lengthToLot(int lotNumber1, int lotNumber2) {
		double length = (2.0 * roadLength / nLots) * Math.abs((lotNumber2 - 1) / 2 - (lotNumber1 - 1) / 2);
		return length;
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
		Road other = (Road) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Road [roadName=" + name + ", junction1=" + junction1.getName() + ", junction2=" + junction2.getName()
				+ ", roadLength=" + roadLength + ", nLots=" + nLots + "]";
	}
}
