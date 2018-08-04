package solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import problem.Matrix;
import problem.ProblemSpec;
import problem.VentureManager;

/**
 * Monte Carlo Tree Search.
 * 
 * @version: v1.0
 * @Author: Sirius
 * @Date: 11/11/17
 */
public class MCTS implements FundingAllocationAgent {
	/* Problem specification. */
	private ProblemSpec spec = new ProblemSpec();
	/* Venture Manager. */
	private VentureManager ventureManager;
	/* Probabilities. */
	private List<Matrix> probabilities;
	/* Action space. */
	private List<Tuple> actionSpace;
	/* State space. */
	private List<Tuple> stateSpace;
	/* Time limit. */
	final long MAXTIME = 25000;
	/* Used for sampling. */
	private Random random = new Random();
	/* Allocation of funding. */
	private List<Integer> fundsAllocation;
	/* Indicate whether to print out additional information. */
	private boolean verbose = false;

	public MCTS(ProblemSpec spec) throws IOException {
		this.spec = spec;
		ventureManager = spec.getVentureManager();
		probabilities = spec.getProbabilities();
		initStateSpace();
		initActionSpace();
	}

	/**
	 * Initialize state space.
	 */
	void initStateSpace() {
		stateSpace = new ArrayList<Tuple>();
		for (int i = 0; i < Math.pow(10, ventureManager.getNumVentures() - 1)
				* ventureManager.getMaxManufacturingFunds() + 1; i++) {
			int[] state = format(i);
			if (isValidState(state)) {
				Tuple t = new Tuple(ventureManager.getNumVentures(), state);
				stateSpace.add(t);
			}
		}
	}

	/**
	 * Transform the tuple to list.
	 * 
	 * @param number
	 *            The state in integer format. (a,b,c) as abc
	 */
	int[] format(int number) {
		List<Integer> list = new ArrayList<>();
		int base = (int) Math.pow(10, ventureManager.getNumVentures() - 1);
		while (base > 1) {
			int a = number / base;
			list.add(a);
			number = number % base;
			base = base / 10;
		}
		list.add(number);
		int[] result = new int[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			result[i] = list.get(i);
		}
		return result;
	}

	/**
	 * Check whether the state is valid.
	 * 
	 * @param state
	 *            The state to be checked.
	 */
	boolean isValidState(int[] state) {
		int sum = 0;
		for (int s : state) {
			sum += s;
		}
		if (sum < ventureManager.getMaxManufacturingFunds() + 1) {
			return true;
		}
		return false;
	}

	/**
	 * Initialize action space.
	 */
	void initActionSpace() {
		actionSpace = new ArrayList<Tuple>();
		for (int i = 0; i < Math.pow(10, ventureManager.getNumVentures() - 1) * ventureManager.getMaxAdditionalFunding()
				+ 1; i++) {
			int[] action = format(i);
			if (isValidAction(action)) {
				Tuple t = new Tuple(ventureManager.getNumVentures(), action);
				actionSpace.add(t);
			}
		}
	}

	/**
	 * Check whether the action is valid.
	 * 
	 * @param action
	 *            The action to be checked.
	 */
	boolean isValidAction(int[] action) {
		int sum = 0;
		for (int a : action) {
			sum += a;
		}
		if (sum < ventureManager.getMaxAdditionalFunding() + 1) {
			return true;
		}
		return false;
	}

	/**
	 * Perform the MCTS to search optimal action.
	 * 
	 * @param state
	 *            The current state.
	 * @param level
	 *            The level of the state.
	 * @return The optimal action.
	 */
	public Tuple searchAction(Tuple state, int level) {

		Node rootNode = new Node(state, level);
		long startTime = System.currentTimeMillis();
		// Define an end time as a terminating condition.
		while ((System.currentTimeMillis() - startTime) < MAXTIME) {
			Node promisingNode = selectPromisingNode(rootNode);
			// Do simulation
			double playoutResult = simulateRandomPlayout(promisingNode);
			backPropogation(promisingNode, playoutResult);
		}
		// Get the optimal action.
		Tuple optimalAction = rootNode.getChildWithMaxValue();
		return optimalAction;
	}

	/**
	 * Select the promising node to expand.
	 * 
	 * @param rootNode
	 *            The root of the tree.
	 * @return The promising node.
	 */
	private Node selectPromisingNode(Node rootNode) {
		Node node = rootNode;
		int i = rootNode.getLevel();
		// Not expand.
		while(true) {
			if (node.getChildren().size() == 0) {
				expandNode(node);
			}
			// Expand but not simulate all children.
			int childNotExpand = node.getExpandChild();
			if (childNotExpand < node.getChildren().size()) {
				node.setExpandChild(childNotExpand + 1);
				return node.getChildren().get(childNotExpand);
			} else {
				// Constraint expand levels to three.
				// Reduce time for expand and do more simulation.
				node = findBestNodeWithUCT(node);
				if (node.getLevel() - i > 2) {	
					break;
				}
				i++;
			}
		}
		return node;
	}

	/**
	 * Determine the best node to expand by UCT.
	 * 
	 * @param rootNode
	 *            The parent node.
	 * @return The best node.
	 */
	public static Node findBestNodeWithUCT(Node node) {
		int parentVisit = node.getVisitCount();
		return Collections.max(node.getChildren(),
				Comparator.comparing(c -> uctValue(parentVisit, c.getValue(), c.getVisitCount())));
	}

	/**
	 * Calculate UCT value.
	 * 
	 * @param totalVisit
	 *            The total visit of the node.
	 * @param value
	 *            The value of the node.
	 * @param nodeVisit
	 *            The visit of particular child node.
	 * @return UCT value.
	 */
	public static double uctValue(int totalVisit, double value, int nodeVisit) {
		if (nodeVisit == 0) {
			return Integer.MAX_VALUE;
		}
		return value + 1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);
	}

	/**
	 * Expand the node. Create children nodes and add to the list.
	 * 
	 * @param node
	 *            Node to expand.
	 */
	private void expandNode(Node node) {
		// Return possible states after possible actions.
		Tuple state = node.getState();
		for (Tuple action : actionSpace) {
			Tuple stateNew = state.addTuple(action);
			if (isValidState(stateNew.getValues())) {
				Node newNode = new Node(stateNew, node.getLevel() + 1);
				newNode.setParent(node);
				node.getChildren().add(newNode);
			}
		}
	}

	/**
	 * Do simulation.
	 * 
	 * @param node
	 *            Node to expand.
	 */
	private double simulateRandomPlayout(Node node) {
		Tuple tempState = node.getState();
		double value = simulate(tempState, 10);

		return value;
	}

	/**
	 * Perform backPropogation. Update the value of nodes in the path.
	 * 
	 * @param nodeToExplore
	 *            Last node in the path.
	 * @param playoutResult
	 *            The result of the simulation.
	 */
	private void backPropogation(Node nodeToExplore, double playoutResult) {
		Node tempNode = nodeToExplore;
		while (tempNode != null) {
			double value = tempNode.getValue() * tempNode.getVisitCount();
			tempNode.incrementVisit();
			tempNode.setValue((value + playoutResult) / tempNode.getVisitCount());
			tempNode = tempNode.getParent();
		}
	}

	/**
	 * Simulate a fortnight. A runtime exception is thrown if the additional funds
	 * allocation is invalid. If the additional funds allocation is valid, the
	 * customer order demand is sampled and the current fortnight is advanced.
	 * 
	 * @param solver
	 * @param numFortnightsLeft
	 */
	public double simulate(Tuple currentState, int fortnightLeft) {
		double totalProfit = 0;
		fundsAllocation = new ArrayList<>();
		for (int i = 0; i < currentState.getNum(); i++) {
			fundsAllocation.add(currentState.getValue(i));
		}

		for (int n = 0; n < fortnightLeft; n++) {
			// compute profit for this week
			double profit = 0.0;
			// record manufacturing funds at start of fortnight

			// ##### Simulate customer orders
			List<Integer> orders = sampleCustomerOrders(fundsAllocation);

			for (int j = 0; j < orders.size(); j++) {
				// compute profit from sales
				int sold = Math.min(orders.get(j), fundsAllocation.get(j));
				profit += (sold * spec.getSalePrices().get(j) * 0.6);

				// compute missed opportunity penalty
				int missed = orders.get(j) - sold;
				profit -= (missed * spec.getSalePrices().get(j) * 0.25);

				// update manufacturing fund levels
				fundsAllocation.set(j, fundsAllocation.get(j) - sold);
			}

			// record manufacturing fund levels after customer orders
			List<Integer> afterOrderFunds = new ArrayList<Integer>(fundsAllocation);

			// ##### Get additional funding amounts
			List<Integer> additionalFunding = generateAdditionalFundingAmounts(afterOrderFunds);

			if (additionalFunding.size() != ventureManager.getNumVentures()) {
				throw new IllegalArgumentException("Invalid additional funding list size");
			}

			// ##### Apply additional funds to manufacturing fund levels
			int totalAdditional = 0;
			int totalFunds = 0;
			for (int i = 0; i < additionalFunding.size(); i++) {
				totalAdditional += additionalFunding.get(i);
				fundsAllocation.set(i, fundsAllocation.get(i) + additionalFunding.get(i));
				totalFunds += fundsAllocation.get(i);
			}
			if (totalAdditional > ventureManager.getMaxAdditionalFunding()) {
				throw new IllegalArgumentException("Amount of additional funding is too large.");
			}
			if (totalFunds > ventureManager.getMaxManufacturingFunds()) {
				throw new IllegalArgumentException("Maximum manufacturing funds exceeded.");
			}
			// update total profit
			totalProfit += (Math.pow(spec.getDiscountFactor(), n - 1) * profit);

			if (verbose) {
				System.out.println();
				System.out.println("Fortnight " + n);
			}
		}
		return totalProfit;
	}

	public List<Integer> generateAdditionalFundingAmounts(List<Integer> manufacturingFunds) {
		// Used to store state.
		List<Integer> additionalFunding = new ArrayList<Integer>();
		// Choose random action
		Random random = new Random();

		int[] tuple = new int[manufacturingFunds.size()];
		for (int i = 0; i < tuple.length; ++i) {
			tuple[i] = manufacturingFunds.get(i);
		}
		Tuple state = new Tuple(tuple.length, tuple);

		Tuple action;
		while (true) {
			int index = random.nextInt(actionSpace.size());
			action = actionSpace.get(index);
			Tuple newState = state.addTuple(action);
			if (isValidState(newState.getValues())) {
				break;
			}
		}

		for (int i = 0; i < action.getNum(); i++) {
			int funding = action.getValue(i);
			additionalFunding.add(funding);
		}
		return additionalFunding;
	}

	/**
	 * Uses the currently loaded stochastic model to sample customer order demand.
	 * Note that user wants may exceed the amount in the manufacturing fund
	 * 
	 * @param state
	 *            The manufacturing funds allocation
	 * @return Customer orders as list of item quantities
	 */
	public List<Integer> sampleCustomerOrders(List<Integer> state) {
		List<Integer> wants = new ArrayList<Integer>();
		for (int k = 0; k < ventureManager.getNumVentures(); k++) {
			int i = state.get(k);
			List<Double> prob = probabilities.get(k).getRow(i);
			wants.add(sampleIndex(prob));
		}
		return wants;
	}

	/**
	 * Returns an index sampled from a list of probabilities
	 * 
	 * @precondition probabilities in prob sum to 1
	 * @param prob
	 * @return an int with value within [0, prob.size() - 1]
	 */
	public int sampleIndex(List<Double> prob) {
		double sum = 0;
		double r = random.nextDouble();
		for (int i = 0; i < prob.size(); i++) {
			sum += prob.get(i);
			if (sum >= r) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get additional funding allocation based the current manufacturing funding.
	 * 
	 * @param nodeToExplore
	 *            Last node in the path.
	 * @param playoutResult
	 *            The result of the simulation.
	 */
	public List<Integer> generateAdditionalFundingAmounts(List<Integer> manufacturingFunds, int numFortnightsLeft) {

		List<Integer> additionalFunding = new ArrayList<Integer>();
		int[] tuple = new int[manufacturingFunds.size()];
		for (int i = 0; i < tuple.length; ++i) {
			tuple[i] = manufacturingFunds.get(i);
		}
		Tuple state = new Tuple(tuple.length, tuple);
		Tuple action = searchAction(state, numFortnightsLeft);

		int totalManufacturingFunds = 0;
		for (int i : manufacturingFunds) {
			totalManufacturingFunds += i;
		}
		int totalAdditional = 0;
		for (int i = 0; i < ventureManager.getNumVentures(); i++) {
			if (totalManufacturingFunds >= ventureManager.getMaxManufacturingFunds()
					|| totalAdditional >= ventureManager.getMaxAdditionalFunding()) {
				additionalFunding.add(0);
			} else {
				int funding = action.getValue(i);
				additionalFunding.add(funding);
				totalAdditional += funding;
				totalManufacturingFunds += funding;
			}
		}
		return additionalFunding;
	}

	public static void main(String[] args) {
		ProblemSpec spec;
		try {
			spec = new ProblemSpec("testcases/platinum1.txt");
			MCTS mcts = new MCTS(spec);
			int[] state = { 0, 0, 0 };
			Tuple tuple = new Tuple(3, state);
			Tuple action = mcts.searchAction(tuple, 1);
			System.out.println("Action is " + action);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void doOfflineComputation() {
		// TODO Auto-generated method stub
		// Do not need in this class.
	}

}
