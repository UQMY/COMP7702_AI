import java.util.ArrayList;
import java.util.List;

public class KDTreeNode {
	/* Distance from parentNode */
	double distance; 
	/* ASVConfig of this node */
    ASVConfig asvConfig;
    /* Parent Node */
    KDTreeNode parentNode;
    /* All ASVConfig from parentNode to this node */
    List<ASVConfig> nodeList = new ArrayList<ASVConfig>();
    
    public KDTreeNode(ASVConfig asv) {
    	this.asvConfig = asv;
    }
    
    public void addNodeList(ASVConfig asvConfig) {
    	nodeList.add(asvConfig);
    }
    
    public void addAllNodeList(List<ASVConfig> nodeList) {
    	for(ASVConfig asvConfig: nodeList) {
    		this.nodeList.add(asvConfig);
    	}
    }
    
    public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public KDTreeNode getParentNode() {
		return parentNode;
	}

	public void setParentNode(KDTreeNode parentNode) {
		this.parentNode = parentNode;
	}

	public List<ASVConfig> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<ASVConfig> nodeList) {
		this.nodeList = nodeList;
	}

	public KDTreeNode getParrentNode() {
  		return parentNode;
  	}
  	public void setParrentNode(KDTreeNode parrentNode) {
  		this.parentNode = parrentNode;
  	}
  	
	public ASVConfig getAsvConfig() {
		return asvConfig;
	}

	public void setAsvConfig(ASVConfig asvConfig) {
		this.asvConfig = asvConfig;
	}

	
	
	
}
