package ro.amihai.dht.bucketstonodes.observer;

import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;

@Component
public class AllNodes  extends BucketsToNodesObserver {

	private Set<NodeAddress> allNodes;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Override
	public void update(Observable o, Object arg) {
		Set<NodeAddress> nodes = new HashSet<>();
		nodes.add(nodeProperties.getCurrentNodeAddress());
		nodes.addAll(bucketsToNodes.getBucketsToNodes().values()
				.stream().flatMap(set -> set.stream()).collect(toSet()));
		this.allNodes = nodes;
	}
	
	public long getNumberOfNodes() {
		return allNodes.size();
	}
	
	public Set<NodeAddress> getAllNodes() {
		return allNodes;
	}

}
