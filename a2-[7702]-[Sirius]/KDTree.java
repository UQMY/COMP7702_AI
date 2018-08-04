import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class KDTree {
	KDTreeNode root;
	List<KDTreeNode> tree = new ArrayList<>();
	
	public KDTree(KDTreeNode root, int dimension) {
		this.root = root;
		tree.add(root);
	}
	
	public void addNode(KDTreeNode childNode) {
		tree.add(childNode);
	}
	
	public KDTreeNode linearSearch(KDTreeNode randomNode) {
		KDTreeNode nearestNode = root;
		double minDistance = 100;
		for(KDTreeNode node : tree) {
			double tempDistance = calDistance(node, randomNode);
			if(minDistance > tempDistance) {
				nearestNode = node;
				minDistance = tempDistance;
			}	
		}
		return nearestNode;
	}
	
	public double calDistance(KDTreeNode parentNode, KDTreeNode randomNode) {
		double distance = 0;
		for(int i = 0; i < parentNode.getAsvConfig().getASVCount(); i++) {
			distance += Math.pow(2, parentNode.getAsvConfig().getPosition(i).distance(
					randomNode.getAsvConfig().getPosition(i)));
		}
		distance = Math.sqrt(distance);
		return distance;
	}
	public double calDistance(ASVConfig parentNode, ASVConfig randomNode) {
		double distance = 0;
		for(int i = 0; i < parentNode.getASVCount(); i++) {
			distance += Math.pow(parentNode.getPosition(i).distance(
					randomNode.getPosition(i)), 2);
		}
		distance = Math.sqrt(distance);
		return distance;
	}
}
