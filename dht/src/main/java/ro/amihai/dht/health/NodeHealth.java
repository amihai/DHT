package ro.amihai.dht.health;

import static ro.amihai.dht.health.NodeStatus.UNBALANCED;

import org.springframework.stereotype.Component;

@Component
public class NodeHealth {

	private NodeStatus nodeStatus = UNBALANCED;
	
	private int noOfBuckets;
	
	private int gossipSize;
	
	public NodeStatus getNodeStatus() {
		return nodeStatus;
	}

	public void setNodeStatus(NodeStatus nodeStatus) {
		this.nodeStatus = nodeStatus;
	}

	public int getNoOfBuckets() {
		return noOfBuckets;
	}

	public void setNoOfBuckets(int noOfBuckets) {
		this.noOfBuckets = noOfBuckets;
	}

	public int getGossipSize() {
		return gossipSize;
	}

	public void setGossipSize(int gossipSize) {
		this.gossipSize = gossipSize;
	}
	
}
