import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
/**
 * Class represents the graph which contain all roads and junctions.
 * 
 * @author Sirius
 * @version 1.0
 */
public class Graph {
	/** Stores all junctions */
	private Map<String, Junction> junctions = new HashMap<String, Junction>();
	/** Stores all Roads */
	private Map<String, Road> roads = new HashMap<String, Road>();
	
	/**
	 * Description: Constructor with one argument.
	 * 
	 * @param fileName:
	 *            The name of the input file.
	 */
	public Graph(String fileName) {
		readRoadInfo(fileName);
	}

	/**
	 * Description: Gets the junction by the name of the junction.
	 * 
	 * @param junctionName:
	 *            The name of the junction.
	 *            
	 * @return junction:
	 *            The junction found by name.
	 */
	public Junction getJunctionByName(String junctionName) {
		return junctions.get(junctionName);
	}

	/**
	 * Description: Gets the road by the name of the road.
	 * 
	 * @param roadName:
	 *            The name of the road.
	 *            
	 * @return road:
	 *            The road found by name.
	 */
	public Road getRoadByName(String roadName) {
		return roads.get(roadName);
	}

	/**
	 * Description: Reads input file to creates graph.
	 * 
	 * @param fileName:
	 *            The name of the input file.
	 */
	public void readRoadInfo(String fileName) {
		try {
			File file = new File(fileName);
			FileReader fileReader = new FileReader(file);

			BufferedReader reader = new BufferedReader(fileReader);

			String line = null;
			int i = 0;
			String roadName = "";
			String startJunction = "";
			String endJunction = "";
			int roadLength = 0;
			int lotsNumber = 0;

			while ((line = reader.readLine()) != null) {
				String[] result = line.split(";");
				for (String token : result) {

					switch (i) {
					case 0:
						roadName = token.trim();
						break;
					case 1:
						startJunction = token.trim();
						break;
					case 2:
						endJunction = token.trim();
						break;
					case 3:
						roadLength = Integer.parseInt(token.trim());
						break;
					case 4:
						lotsNumber = Integer.parseInt(token.trim());
						break;
					}
					i++;
				}
				i = 0;
				createGraph(roadName, startJunction, endJunction, roadLength, lotsNumber);
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Description: Creates graph.
	 * 
	 * @param roadName:
	 *            The name of the road.
	 * @param junctionName1:
	 *            The name of the first junction.   
	 * @param junctionName2:
	 *            The name of the second junction.
	 * @param roadLength:
	 *            The length of the road.  
	 * @param nLots:
	 *            The number of the lots on the road.
	 */
	public void createGraph(String roadName, String junctionName1, String junctionName2, int roadLength, int nLots) {
		Junction junction1;
		Junction junction2;
		if (junctions.containsKey(junctionName1)) {
			junction1 = junctions.get(junctionName1);
		} else {
			junction1 = new Junction(junctionName1);
			junctions.put(junctionName1, junction1);
		}
		if (junctions.containsKey(junctionName2)) {
			junction2 = junctions.get(junctionName2);
		} else {
			junction2 = new Junction(junctionName2);
			junctions.put(junctionName2, junction2);
		}

		Road road = new Road(roadName, junction1, junction2, roadLength, nLots);
		road.setJunction1(junction1);
		road.setJunction2(junction2);
		junction1.addRoads(road);
		junction2.addRoads(road);
		roads.put(roadName, road);
	}
	
	/* Reset all junctions in the graph. */
	public void reset() {
		for (Junction junction : junctions.values()) {
		   junction.reset();
		}
	}
	
}