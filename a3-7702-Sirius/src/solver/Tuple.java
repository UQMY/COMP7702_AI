package solver;

import java.util.Arrays;

/**
 * Tuple for action and state.
 * v1.0
 * Author: Yu Miao 
 * Date: 11/11/17
 */
public class Tuple {
	/* Values in tuple. */
	private int[] values;
	/* Number of values in tuple. */
	private int num;
	
	public Tuple(int num, int[] values){
		this.num = num;
		if(num == values.length) {
			this.values = new int[num];
			for(int i = 0; i < num; ++i) {
				this.values[i] = values[i];
			}
		}
	}
	
	public int[] getValues() {
		return values;
	}

	public void setValues(int[] values) {
		this.values = values;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
	
	/**
	 * Gets the value from tuple based on index.
	 * 
	 * @param index
	 * 		the index of the value.
	 * @return value
	 * 		value in the tuple.
	 * 		returns -1 if index is invalid. 	
	 */
	public int getValue(int index) {
		int value = -1;
		if(index < num) {
			value = values[index];
		}
		return value;
	}
	
	/**
	 * Adds two tuples.
	 * 
	 * @param tuple
	 * 		the tuple to be added.
	 * @return result
	 * 		the sum of two tuples.
	 * 		returns null two tuples have different number of elements.
	 */
	public Tuple addTuple(Tuple tuple) {
		if(num == tuple.getNum()) {
			int[] sum = new int[num];
			for(int i = 0; i < num; ++i) {
				sum[i] = this.values[i] + tuple.getValue(i);
			}
			Tuple result = new Tuple(num, sum);
			return result;
		}
		return null;
	}
	
	/**
	 * Get the difference between two tuples.
	 * 
	 * @param tuple
	 * 		the subtracter tuple.
	 * @return result
	 * 		the different between two tuples.
	 * 		returns null if two tuples have different number of elements.
	 */
	public Tuple minusTuple(Tuple tuple) {
		if(num == tuple.getNum()) {
			int[] sum = new int[num];
			for(int i = 0; i < num; ++i) {
				sum[i] = this.values[i] - tuple.getValue(i);
			}
			Tuple result = new Tuple(num, sum);
			return result;
		}
		return null;
	}
	
	/**
	 * Checks whether all elements are greater than the given tuple.
	 * 
	 * @param tuple
	 * 		the tuple to be compared to.
	 * @return result
	 * 		true when all elements are greater than the given tuple.
	 * 		else, false.
	 */
	public boolean greaterThan(Tuple tuple) {
		boolean result = true;
		for(int i = 0; i < num; i++) {
			if(values[i] < tuple.getValue(i)) {
				result = false;
			}
		}
		return result;
	}
	
	@Override
	public String toString() {
		String result = "(";
		result += values[0];
		for(int i = 1; i < num; ++i) {
			result += (", " + values[i]);  
		}
		result += ")";
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + num;
		result = prime * result + Arrays.hashCode(values);
		return result;
	}

	@Override
	public boolean equals(Object other) {
		try {
			Tuple c = (Tuple) other;
			boolean result = true;
			for(int i = 0; i < num; ++i) {
				if(values[i] != c.getValue(i)) {
					result = false;
				}
			}
			return result;
		}
		catch (ClassCastException cce) {
			return false;
		}
	}	
}
