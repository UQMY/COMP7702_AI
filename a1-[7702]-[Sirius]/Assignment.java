import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
/**
 * Class to perform the search.
 * 
 * @author Sirius
 * @version 1.0
 */
public class Assignment {
	
	
	public static void main(String[] args) {
		
		readQuaryInfo(args[0], args[1], args[2]);

	}

	/**
	 * Description: Read queries in the query file.
	 * 
	 * @param environmentFile:
	 *            The name of the environment file.
	 * @param queryFile:
	 *            The name of the query file.   
	 * @param outputFile:
	 *            The name of the output file.
	 */
	public static void readQuaryInfo(String environmentFile, String queryFile, String outputFile ) {
		Graph graph = new Graph(environmentFile);
		try {
			File myFile = new File(queryFile);
			FileReader fileReader = new FileReader(myFile);

			BufferedReader reader = new BufferedReader(fileReader);

			String line = null;
			int i = 0;
			int startRoadNum = 0;
			int endRoadNum = 0;
			String startRoadName = "";
			String endRoadName = "";

			while ((line = reader.readLine()) != null) {
				String[] result = line.split(";");
				for (String token : result) {
					String roadInfo = token.trim();
					String name = roadInfo.replaceFirst("\\d+", "");
					String num = roadInfo.replace(name, "");
					int roadNum = Integer.parseInt(num);
					if (i == 0) {
						startRoadNum = roadNum;
						startRoadName = name;
					} else {
						endRoadNum = roadNum;
						endRoadName = name;
					}
					i++;
				}
				i = 0;		
				search(startRoadNum, graph.getRoadByName(startRoadName), endRoadNum, graph.getRoadByName(endRoadName), outputFile);
				graph.reset();
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Description: Search the map according to the given information.
	 * 
	 * @param startLotNumber:
	 *            The name of the environment file.
	 * @param startRoad:
	 *            The name of the query file.   
	 * @param outputFile:
	 *            The name of the output file.
	 */
	public static void search(int startLotNumber, Road startRoad, int endLotNumber, Road endRoad, String outputFile) throws IOException {
		/* Check whether the startRoad and endRoad are valid or not. */
		if(startRoad == null || endRoad == null) {
			writeResults("Invalid road name\n", outputFile);
			return;
		}
		
		/* Check whether the startLotNumber and endLotNumber are valid not. */
		if(startLotNumber < 1 ||startLotNumber>startRoad.getNLots()||
				endLotNumber < 1 ||endLotNumber>endRoad.getNLots()) {
			writeResults("Invalid lot number\n", outputFile);
			return;
		}
		
		/* Comparator used for priority queue. */
		Comparator<Junction> cmp = new Comparator<Junction>() {
			public int compare(Junction junction1, Junction junction2) {
				return (int) (junction1.getLengthToRoot() - junction2.getLengthToRoot());
			}
		};

		PriorityQueue<Junction> priorityQueue = new PriorityQueue<>(50, cmp);
		
		/* Stores the length of the shortest path from start to the goal. */
		double shortestLength = Double.MAX_VALUE;
		/* The last junction to the goal in the shortest path. */
		Junction endJunction = null;

		/* Check whether the start road is the same as the goal road. */
		if (startRoad.equals(endRoad)) {
			writeResults(startRoad.lengthToLot(startLotNumber, endLotNumber) + " ; " + startRoad.getName() + "\n", outputFile);
			return;
		}

		do {
			if (priorityQueue.isEmpty()) {
				Junction junction1 = startRoad.getJunction1();
				junction1.setLengthToRoot(startRoad.lengthToLot(junction1, startLotNumber));
				Junction junction2 = startRoad.getJunction2();
				junction2.setLengthToRoot(startRoad.lengthToLot(junction2, startLotNumber));
				junction1.setLastRoad(startRoad);
				junction2.setLastRoad(startRoad);
				priorityQueue.add(junction1);
				priorityQueue.add(junction2);
			}
			
			Junction currentJunction = priorityQueue.poll();
			if (currentJunction.getLengthToRoot() > shortestLength) {
				continue;
			}
			/* Gets all road accessible from the current junction. */
			for (Road road : currentJunction.getRoads()) {
				if (!road.getName().equals(endRoad.getName())) {
					/* Get another junction of the road. */
					Junction anotherJunction = road.getAnotherJunction(currentJunction);
					/* The estimated length to root of the another junction. */
					double newLength = currentJunction.getLengthToRoot() + road.getRoadLength();
					/* Check which path to the another junction is the shortest one. */
					if (anotherJunction.getLengthToRoot() != 0 && newLength > anotherJunction.getLengthToRoot()) {
						continue;
					}
					anotherJunction.setLastJunction(currentJunction);
					anotherJunction.setLastRoad(road);
					anotherJunction.setLengthToRoot(newLength);					
					priorityQueue.add(anotherJunction);
				} else {
					double length = currentJunction.getLengthToRoot() + road.lengthToLot(currentJunction, endLotNumber);
					if (shortestLength > length) {
						endJunction = currentJunction;
						shortestLength = length;
					}
					break;
				}
			}
		} while (!priorityQueue.isEmpty());

		/* Check whether the path is found. */
		if (endJunction == null) {
			writeResults("no-path\n", outputFile);
		} else {
			String path = endJunction.getLastRoad().getName() + "-" + endJunction.getName() + "-" + endRoad.getName()
					+ "\n";
			while (endJunction.getLastJunction() != null) {
				endJunction = endJunction.getLastJunction();
				path = endJunction.getLastRoad().getName() + "-" + endJunction.getName() + "-" + path;
			}
			path = shortestLength + " ; " + path;
			writeResults(path, outputFile);
		}
		return;
	}

	/**
	 * Description: Append the result of the query to output file.
	 * 
	 * @param result:
	 *            The result of the query.
 	 *
	 * @param outputFileName:
	 *            The name of the output file.
	 */
	public static void writeResults(String result, String outputFileName) throws IOException {
		File file = new File(outputFileName);
		BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
		output.write(result);
		output.close();
	}
}
