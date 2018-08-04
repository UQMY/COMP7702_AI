import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

public class RRT {
	static int count = 0;
	static boolean conti = false;
	Tester tester = new Tester();
	ProblemSpec ps = new ProblemSpec();
	/* Number of ASVs */
	int asvAmount;
	KDTree kdTree;
	/* If the initial state is clockwise, the value is 1; otherwise -1; */
	int clockwise;

	/**
	 * Constructor
	 *
	 * @param problemFile
	 *            the file with problem.
	 */
	public RRT(String problemFile) throws IOException {
		ps.loadProblem(problemFile);
		asvAmount = ps.getASVCount();
		KDTreeNode root = new KDTreeNode(ps.getInitialState());
		kdTree = new KDTree(root, asvAmount);
		clockwise = calcClochWise(ps.getInitialState(), 0);
	}

	public static void main(String[] args) throws IOException {
		while (conti != true) {
			RRT rrt = new RRT(args[0]);
			KDTreeNode destinationNode = null;
			destinationNode = rrt.perform();
			if (conti == true) {
				rrt.writeResults(destinationNode, args[1]);
			} else
				continue;
		}
	}

	/**
	 * Check whether the initial state is clockwise or not
	 *
	 * @return returns -1 if not clockwise. returns 1 if clockwise.
	 */
	public int calcClochWise(ASVConfig asvConfig, int i) {
		double x1 = asvConfig.getPosition(i + 1).getX() - asvConfig.getPosition(i).getX();
		double y1 = asvConfig.getPosition(i + 1).getY() - asvConfig.getPosition(i).getY();
		double x2 = asvConfig.getPosition(i + 2).getX() - asvConfig.getPosition(i + 1).getX();
		double y2 = asvConfig.getPosition(i + 2).getY() - asvConfig.getPosition(i + 1).getY();
		double product = x1 * y2 - y1 * x2;
		if (product < 0) {
			return 1;
		} else if (product > 0) {
			return -1;
		} else {
			if (i < asvAmount - 2) {
				return calcClochWise(asvConfig, i + 1);
			} else {
				return 1;
			}
		}
	}

	/**
	 * Perform RRT
	 *
	 * @return node the node contains the last asvConfig before goal .
	 */
	public KDTreeNode perform() {
		KDTreeNode node = null;
		while (true) {
			// 0.9 to generate random asvConfig, 0.1 to move towards goal// 0.7 to generate
			// random asvConfig,0.2 to generate the random savConfig located
			// in gap area, 0.1 to move towards goal
			ASVConfig randomASVConfig;
			double randomNumber = Math.random();
			if (randomNumber < 0.7) {
				randomASVConfig = sample();
				count++;
			} else if (randomNumber > 0.7 && randomNumber < 0.9) {
				randomASVConfig = gapsample();
				count++;
			} else {
				randomASVConfig = ps.getGoalState();
				count++;
			}

			// Search for the nearest node.
			KDTreeNode nearestNode = kdTree.linearSearch(new KDTreeNode(randomASVConfig));
			node = expand(nearestNode, randomASVConfig);
			if (node != null) {
				conti = true;
				break;
			} else if (count == 5000) {
				count = 0;
				break;
			}
		}
		return node;
	}

	/**
	 * Expand from given random nearestNode to a random ASVConfig.
	 *
	 * @param nearestNode
	 *            the nearest node in the tree to the random ASVConfig.
	 * @param randomASVConfig
	 *            the random ASVConfig move towards.
	 * @return node the node contains the last asvConfig before goal .
	 */
	private KDTreeNode expand(KDTreeNode nearestNode, ASVConfig randomASVConfig) {
		ASVConfig nearestASVConfig = new ASVConfig(nearestNode.getAsvConfig());
		if (nearestASVConfig.equals(randomASVConfig)) {
			return null;
		}

		int coefficient = calcCoefficient(nearestASVConfig, randomASVConfig);

		ASVConfig asvConfig;
		double x = randomASVConfig.getPosition(0).getX() - nearestASVConfig.getPosition(0).getX();
		double y = randomASVConfig.getPosition(0).getY() - nearestASVConfig.getPosition(0).getY();

		double deltaX = x / coefficient;
		double deltaY = y / coefficient;

		double[] angles = new double[asvAmount - 1];
		double[] deltaAngle = new double[asvAmount - 1];

		double goalAngle = Math.atan2((randomASVConfig.getPosition(1).getY() - randomASVConfig.getPosition(0).getY()),
				(randomASVConfig.getPosition(1).getX() - randomASVConfig.getPosition(0).getX()));
		double startAngle = Math.atan2(
				(nearestASVConfig.getPosition(1).getY() - nearestASVConfig.getPosition(0).getY()),
				(nearestASVConfig.getPosition(1).getX() - nearestASVConfig.getPosition(0).getX()));
		double angle = goalAngle - startAngle;
		angles[0] = startAngle;
		deltaAngle[0] = angle / coefficient;

		for (int i = 1; i < asvAmount - 1; i++) {
			double product = (nearestASVConfig.getPosition(i).getX() - nearestASVConfig.getPosition(i - 1).getX())
					* (nearestASVConfig.getPosition(i).getX() - nearestASVConfig.getPosition(i + 1).getX())
					+ (nearestASVConfig.getPosition(i).getY() - nearestASVConfig.getPosition(i - 1).getY())
							* (nearestASVConfig.getPosition(i).getY() - nearestASVConfig.getPosition(i + 1).getY());
			double dis1 = nearestASVConfig.getPosition(i).distance(nearestASVConfig.getPosition(i - 1));
			double dis2 = nearestASVConfig.getPosition(i).distance(nearestASVConfig.getPosition(i + 1));
			double angle1 = Math.acos(product / dis1 / dis2);
			double product2 = (randomASVConfig.getPosition(i).getX() - randomASVConfig.getPosition(i - 1).getX())
					* (randomASVConfig.getPosition(i).getX() - randomASVConfig.getPosition(i + 1).getX())
					+ (randomASVConfig.getPosition(i).getY() - randomASVConfig.getPosition(i - 1).getY())
							* (randomASVConfig.getPosition(i).getY() - randomASVConfig.getPosition(i + 1).getY());
			double dis3 = randomASVConfig.getPosition(i).distance(randomASVConfig.getPosition(i - 1));
			double dis4 = randomASVConfig.getPosition(i).distance(randomASVConfig.getPosition(i + 1));
			double angle2 = Math.acos(product2 / dis3 / dis4);
			angles[i] = angle1;
			deltaAngle[i] = (angle2 - angle1) / coefficient;
		}

		List<ASVConfig> list = new ArrayList<>();
		double totalDistance = 0.0;
		while ((asvConfig = validStep(nearestASVConfig, angles, deltaAngle, deltaX, deltaY)) != null) {
			list.add(asvConfig);
			totalDistance += asvConfig.totalDistance(nearestASVConfig);
			nearestASVConfig = asvConfig;
			for (int i = 1; i < asvAmount - 1; i++) {
				angles[i - 1] += deltaAngle[i - 1];
			}
			if (tester.isValidStep(asvConfig, ps.getGoalState())) {
				KDTreeNode node = new KDTreeNode(asvConfig);
				node.addAllNodeList(list);
				node.setParrentNode(nearestNode);
				node.setDistance(totalDistance);
				return node;
			}
		}
		if (!list.isEmpty()) {
			KDTreeNode node = new KDTreeNode(list.get(list.size() - 1));
			node.addAllNodeList(list);
			node.setParrentNode(nearestNode);
			node.setDistance(totalDistance);
			kdTree.addNode(node);
		}
		return null;
	}

	/**
	 * Check whether the step is valid between two ASVConfig.
	 *
	 * @param nearestASVConfig
	 *            the nearest ASVConfig.
	 * @param angles
	 *            list of angles of ASVConfig.
	 * @param deltaAngles
	 *            the increment of each angle.
	 * @param deltaX
	 *            the increment of x .
	 * @param deltaY
	 *            the increment of y.
	 * @return asvConfig the next ASVConfig after one valid step.
	 */
	public ASVConfig validStep(ASVConfig nearestASVConfig, double[] angles, double[] deltaAngles, double deltaX,
			double deltaY) {
		double[] positions = new double[asvAmount * 2];

		for (int i = 0; i < 2; i++) {
			Point2D point = nearestASVConfig.getASVPositions().get(i);
			double x = point.getX() + deltaX;
			double y = point.getY() + deltaY;
			positions[2 * i] = x;
			positions[2 * i + 1] = y;
		}

		double angle1 = deltaAngles[0];
		double x = (positions[2] - positions[0]) * Math.cos(angle1) - (positions[3] - positions[1]) * Math.sin(angle1)
				+ positions[0];
		double y = (positions[2] - positions[0]) * Math.sin(angle1) + (positions[3] - positions[1]) * Math.cos(angle1)
				+ positions[1];
		positions[2] = x;
		positions[3] = y;

		for (int i = 1; i < asvAmount - 1; i++) {
			double angle = (angles[i] + deltaAngles[i]) * clockwise;
			x = (positions[2 * (i - 1)] - positions[2 * i]) * Math.cos(angle)
					- (positions[2 * (i - 1) + 1] - positions[2 * (i) + 1]) * Math.sin(angle) + positions[2 * (i)];

			y = (positions[2 * (i - 1)] - positions[2 * i]) * Math.sin(angle)
					+ (positions[2 * (i - 1) + 1] - positions[2 * (i) + 1]) * Math.cos(angle) + positions[2 * (i) + 1];
			positions[2 * (i + 1)] = x;
			positions[2 * (i + 1) + 1] = y;
		}

		ASVConfig asvConfig = new ASVConfig(positions);
		if (testValidation(asvConfig)) {
			if (tester.isValidStep(nearestASVConfig, asvConfig)) {
				return asvConfig;
			}
		}
		return null;

	}

	/**
	 * Create random ASVConfig.
	 *
	 * @return asvConfig the random ASVConfig.
	 */
	public ASVConfig sample() {
		while (true) {
			List<Point2D> asvList = new ArrayList<>();

			Random random = new Random();
			double angle = random.nextDouble() * 2 * Math.PI - Math.PI;
			double x = random.nextDouble();
			double y = random.nextDouble();
			Point2D point = calculatePoint(angle, x, y);
			asvList.add(point);
			double lastAngle = angle;
			for (int i = 1; i < asvAmount; i++) {
				double nextAngle = random.nextDouble() * (lastAngle - angle + 2 * Math.PI) + angle - 2 * Math.PI;
				x = point.getX();
				y = point.getY();
				point = calculatePoint(nextAngle, x, y);
				lastAngle = nextAngle;
				asvList.add(point);
			}

			double[] asvPos = new double[asvAmount * 2];
			for (int i = 0; i < asvList.size(); i++) {
				asvPos[2 * i] = asvList.get(i).getX();
				asvPos[2 * i + 1] = asvList.get(i).getY();
			}

			ASVConfig asvConfig = new ASVConfig(asvPos);
			if (testValidation(asvConfig)) {
				return asvConfig;
			}
		}
	}
	
	/**
	 * Create random ASVConfig in gap area.
	 *
	 * @return asvConfig the random ASVConfig which is located in gap area.
	 */
	public ASVConfig gapsample() {
		while (true) {
			List<Point2D> asvList = new ArrayList<>();

			Random random = new Random();
			double angle = random.nextDouble() * 2 * Math.PI - Math.PI;
			double x = 0;
			double y = 0;
			double height = 0;
			List<ArrayList<Obstacle>> gapArea = buildGapArea(ps);
			for (ArrayList<Obstacle> al : gapArea) {
				ArrayList<Obstacle> alo = al;
				for (Obstacle o : alo) {
					if (o.getRect().getHeight() > height) {
						x = o.getRect().getX() + o.getRect().getWidth() * random.nextDouble();
						y = o.getRect().getY() + o.getRect().getHeight() * random.nextDouble();
						height = o.getRect().getHeight();
					}
				}
			}
			Point2D point = calculatePoint(angle, x, y);
			asvList.add(point);
			double lastAngle = angle;
			for (int i = 1; i < asvAmount; i++) {
				double nextAngle = (random.nextDouble() * (lastAngle - angle + 2 * Math.PI) + angle - 2 * Math.PI)
						* 0.6;
				x = point.getX();
				y = point.getY();
				point = calculatePoint(nextAngle, x, y);
				lastAngle = nextAngle;
				asvList.add(point);
			}

			double[] asvPos = new double[asvAmount * 2];
			for (int i = 0; i < asvList.size(); i++) {
				asvPos[2 * i] = asvList.get(i).getX();
				asvPos[2 * i + 1] = asvList.get(i).getY();
			}

			ASVConfig asvConfig = new ASVConfig(asvPos);
			if (testValidation(asvConfig)) {
				return asvConfig;
			}
			// return asvConfig;
		}
	}

	/**
	 * Calculate the next point based on the coordinates of the point and angle
	 *
	 * @return point the next point.
	 */
	public Point2D calculatePoint(double angle, double x, double y) {
		double deltaX = 0.05 * Math.cos(angle);
		double deltaY = 0.05 * Math.sin(angle);
		Point2D point = new Point2D.Double(x + deltaX, y + deltaY);
		return point;
	}

	/**
	 * Create random ASVConfig.
	 *
	 * @return asvConfig the random ASVConfig.
	 */
	int calcCoefficient(ASVConfig initialState, ASVConfig goalState) {
		double maxDistance = 0;
		for (int i = 0; i < initialState.getASVCount(); i++) {
			double distance = initialState.getPosition(i).distance(goalState.getPosition(i));
			if (maxDistance < distance) {
				maxDistance = distance;
			}
		}
		int coefficient = (int) (maxDistance / 0.0005);
		return coefficient;
	}

	/**
	 * Checks whether the asvConfig is valid.
	 *
	 * @param asvConfig
	 *            ASVConfig to be checked
	 * @return returns true if valid; returns false if invalid.
	 */
	public boolean testValidation(ASVConfig asvConfig) {
		boolean a1 = tester.hasValidBoomLengths(asvConfig);
		boolean a2 = tester.isConvex(asvConfig);
		boolean a3 = tester.hasEnoughArea(asvConfig);
		boolean a4 = tester.fitsBounds(asvConfig);
		boolean a5 = tester.hasCollision(asvConfig, ps.getObstacles());
		if (a1 && a2 && a3 && a4 && !a5) {
			return true;
		}
		return false;
	}

	/**
	 * Write path into file.
	 *
	 * @param destinationNode
	 *            the last node to the goal.
	 * @param outputFileName
	 *            the name of the output file.
	 * 
	 */
	public void writeResults(KDTreeNode destinationNode, String outputFileName) throws IOException {
		File file = new File(outputFileName);
		BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
		Stack<KDTreeNode> path = new Stack<>();
		int numberOfLines = 1;
		double totalDistance = destinationNode.getAsvConfig().totalDistance(ps.getGoalState());
		while (destinationNode.getParrentNode() != null) {
			path.push(destinationNode);
			numberOfLines += 1 + destinationNode.getNodeList().size();
			totalDistance += destinationNode.getDistance();
			destinationNode = destinationNode.getParrentNode();
		}
		output.write(numberOfLines + " " + totalDistance + '\n');
		output.write(ps.getInitialState().toString() + '\n');
		while (!path.isEmpty()) {
			KDTreeNode currentNode = path.pop();
			for (ASVConfig asvConfig : currentNode.getNodeList()) {
				output.write(asvConfig.toString() + '\n');
			}
			output.write(currentNode.getAsvConfig().toString() + '\n');
		}
		output.write(ps.getGoalState().toString());
		output.close();
	}
	/**
	 * Build gap area which are between the obstacles.
	 * 
	 * @param ps
	 *            instance of probemSpec.
	 * @return a list of all ArrayList which include all gap area.
	 */
	public static List<ArrayList<Obstacle>> buildGapArea(ProblemSpec ps) {
		List<ArrayList<Obstacle>> gapArea = new ArrayList<ArrayList<Obstacle>>();
		List<Obstacle> obsss = ps.getObstacles();
		Iterator<Obstacle> it = obsss.iterator();

		while (it.hasNext()) {
			Obstacle ob = it.next();
			ArrayList<Obstacle> gp = new ArrayList<Obstacle>();
			gp.add(ob);
			it.remove();
			while (it.hasNext()) {
				Obstacle oc = it.next();
				if (oc.getRect().getX() == ob.getRect().getX()) {
					gp.add(oc);
					it.remove();
				}
			}

			// build two rectangles which are the top border and bottom border.
			gp.add(new Obstacle(ob.getRect().getX(), 0.0, ob.getRect().getWidth(), 0.0));
			gp.add(new Obstacle(ob.getRect().getX(), 1.0, ob.getRect().getWidth(), 0.0));
			// order the obstacle
			Collections.sort(gp, new ObstacleComparator());
			ArrayList<Obstacle> gap = new ArrayList<Obstacle>();
			for (int i = 1, j = 0; i < gp.size(); i++, j++) {
				double high = gp.get(i).getRect().getY() - gp.get(j).getRect().getY() - gp.get(j).getRect().getHeight();
				if (high != 0.0)
					gap.add(new Obstacle(gp.get(j).getRect().getX(),
							gp.get(j).getRect().getY() + gp.get(j).getRect().getHeight(),
							gp.get(j).getRect().getWidth(), high));
			}
			gapArea.add(gap);
		}
		return gapArea;
	}

}
