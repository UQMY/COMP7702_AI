package solver;

import java.util.ArrayList;
import java.util.List;

/**
 * Node Class for Monte Carlo Tree Search.
 * 
 * @version: v1.0
 * @Author: Sirius
 * @Date: 11/11/17
 */
public class Node {
	// The state of the node.
	private Tuple state;
	// The value of the node.
	private double value;
	// Parent node.
	private Node parent;
	// List of childNodes.
	private List<Node> children;
	// Level in the tree.
	private int level = 0;
	// The index of child should be expanded.
	private int expandChild;
	// The number of visit.
	private int visitCount;

	public Node(Tuple state, int level) {
		this.state = state;
		expandChild = 0;
		visitCount = 0;
		this.level = level;
		value = 0;
		children = new ArrayList<>();
	}

	public Node(Node node) {
		this(node.getState(), node.getLevel());
	}

	public Tuple getState() {
		return state;
	}

	public void setState(Tuple state) {
		this.state = state;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	public int getVisitCount() {
		return visitCount;
	}

	public void setVisitCount(int visitCount) {
		this.visitCount = visitCount;
	}

	public int getLevel() {
		return level;
	}

	public int getExpandChild() {
		return expandChild;
	}

	public void setExpandChild(int expandChild) {
		this.expandChild = expandChild;
	}

	/**
	 * Add node as child.
	 * 
	 * @param child
	 *            the child node to be added.
	 */
	public void addChildren(Node child) {
		children.add(child);
	}

	/**
	 * Increase the number of visit by 1.
	 */
	public void incrementVisit() {
		visitCount++;
	}

	/**
	 * Get the child node with the largest value.
	 * 
	 * @return the child node with the largest value.
	 */
	public Tuple getChildWithMaxValue() {
		double maxValue = -100;
		Node maxNode = null;
		for (Node node : children) {
			if (node.getValue() > maxValue) {
				maxValue = node.getValue();
				maxNode = node;
			}
		}
		return maxNode.getState().minusTuple(state);
	}
	
	@Override
	public String toString() {
		return state.toString();
	}
}
