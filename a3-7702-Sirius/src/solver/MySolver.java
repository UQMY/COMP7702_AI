package solver;

/**
 * Synchronous value Iteration\
 * 
 * @version: v1.0
 * @Author: Sirius
 * @Date: 11/11/17
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.text.html.MinimalHTMLWriter;

import problem.VentureManager;
import problem.Matrix;
import problem.ProblemSpec;

public class MySolver implements FundingAllocationAgent {

	private ProblemSpec spec = new ProblemSpec();
	private VentureManager ventureManager;
	private List<Matrix> probabilities;
	/* Policy. */
	HashMap<Tuple, Tuple> policy;
	/* Transition function. */
	private List<Matrix> transitionFunc;
	/* Action space. */
	private List<Tuple> actionSpace;
	/* State space. */
	private List<Tuple> stateSpace;
	/* Reward function. */
	private List<List<Double>> rewards;
	/* Used for checking convergence. */
	final private double MINERROR = 1e-7;

	public MySolver(ProblemSpec spec) throws IOException {
		this.spec = spec;
		ventureManager = spec.getVentureManager();
		probabilities = spec.getProbabilities();
		transitionFunc = new ArrayList<>();
		initStateSpace();
		initActionSpace();
		initRewords();
		initTransitionFunc();
	}

	public void doOfflineComputation() {
		// Do value iteration.
		/* The number of states in state space.*/
		int statesNum = stateSpace.size();
		/* Value function V(s).*/
		double[] values = new double[statesNum];
		/* Value function V'(s).*/
		double[] valuesNew = new double[statesNum];
		/* Policy.*/
		policy = new HashMap<>();

		for(int i = 0; i < valuesNew.length; ++i) {
			valuesNew[i] = Double.NEGATIVE_INFINITY;
		}
		// Do value iteration.
		boolean isConverge = false;
		while (!isConverge) {
			// Used to check convergence.
			isConverge = true;
			// Loop for each state.
			for (int i = 0; i < statesNum; ++i) {
				// Calculate value for each action.
				for (Tuple a : actionSpace) {
					double value = 0.0;
					// Update value function
					value = calculateValue(i, a, values);
				
					if (value > valuesNew[i]) {
						valuesNew[i] = value;
						policy.put(stateSpace.get(i), a);
					}
				}
				if (valuesNew[i] - values[i] > MINERROR) {
					isConverge = false;
					values[i] = valuesNew[i];
				}
				
			}
		}		
	}
	

	/**
	 * Initialize transition function.
	 */
	void initTransitionFunc() {
		int stateNum = ventureManager.getMaxManufacturingFunds() + 1;
		for (int i = 0; i < ventureManager.getNumVentures(); ++i) {
			double[][] transFunc = new double[stateNum][stateNum];
			// Calculate transition function for each venture.
			for (int row = 0; row < stateNum; row++) {
				for (int col = 0; col < stateNum; col++) {
					if (row < col) {
						transFunc[row][col] = 0;
					} else if (row >= col && col > 0) {
						transFunc[row][col] = probabilities.get(i).get(row, row - col);
					} else {
						for (int k = row; k < stateNum; k++) {
							transFunc[row][col] += probabilities.get(i).get(row, k);
						}
					}
				}
			}
			Matrix matrix = new Matrix(transFunc);
			transitionFunc.add(matrix);
		}
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
	 * Initialize reward function.
	 */
	void initRewords() {
		rewards = new ArrayList<>();
		// For each venture
		for (int i = 0; i < ventureManager.getNumVentures(); i++) {
			List<Double> reward = new ArrayList<>();
			// For each state
			for (int j = 0; j < ventureManager.getMaxManufacturingFunds() + 1; ++j) {
				double r = 0.0;
				// For each order
				for (int k = 0; k < ventureManager.getMaxManufacturingFunds() + 1; ++k) {
					r = r + (Math.min(j, k) * spec.getSalePrices().get(i) * 0.6 * probabilities.get(i).get(j, k)
							- Math.max(0, k - j) * spec.getSalePrices().get(i) * 0.25 * probabilities.get(i).get(j, k));
				}
				reward.add(r);
			}
			rewards.add(reward);
		}
	}

	/**
	 * Check whether the state is valid.
	 * 
	 * @param state
	 *            The state to be checked.
	 */
	double calculateValue(int state, Tuple action, double[] values) {
		double value = 0.0;
		Tuple stateNew = stateSpace.get(state).addTuple(action);
		// check if the action is valid
		if (!isValidState(stateNew.getValues())) {
			return -10;
		}
		// immediate reward
		for (int i = 0; i < ventureManager.getNumVentures(); ++i) {
			value += rewards.get(i).get(stateNew.getValue(i));
		}
		// for each s'
		for (int i = 0; i < stateSpace.size(); ++i) {
			if (!stateNew.greaterThan(stateSpace.get(i))) {
				continue;
			}
			double t = 1.0;
			// for each venture
			for (int j = 0; j < ventureManager.getNumVentures(); ++j) {
				t *= transitionFunc.get(j).get(stateNew.getValue(j), stateSpace.get(i).getValue(j));
			}
			value += spec.getDiscountFactor() * t * values[i];
		}
		return value;
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

	public List<Integer> generateAdditionalFundingAmounts(List<Integer> manufacturingFunds, int numFortnightsLeft) {

		List<Integer> additionalFunding = new ArrayList<Integer>();
		int[] tuple = new int[manufacturingFunds.size()];
		for (int i = 0; i < tuple.length; ++i) {
			tuple[i] = manufacturingFunds.get(i);
		}
		Tuple state = new Tuple(tuple.length, tuple);
		Tuple action = policy.get(state);

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

	/* For test use. */
	public static void main(String[] args) {
		ProblemSpec spec;
		try {
			spec = new ProblemSpec("testcases/platinum1.txt");
			MySolver mySolver = new MySolver(spec);
			long start = System.currentTimeMillis();
			mySolver.doOfflineComputation();
			long end = System.currentTimeMillis();
			System.out.println("Time is: " + (end - start) + " ms.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
