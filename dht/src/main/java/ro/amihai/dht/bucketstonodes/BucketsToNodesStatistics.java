package ro.amihai.dht.bucketstonodes;

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.amihai.dht.node.NodeProperties;

/**
 * For performance, this class cache some statistics that can be retrieved from BucketsToNodes. This statistics are modified only when the BucketsToNodeIsModified
 *
 */
@Component
public class BucketsToNodesStatistics {

	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	private Set<Integer> bucketsInCurrentNode;
	
	private long numberOfNodes;
	
	@PostConstruct
	private void init() {

	}
	
	public void updateStatistics() {
		numberOfNodes = bucketsToNodes.getBucketsToNodes().values()
				.stream().flatMap(setOfNodes -> setOfNodes.stream())
				.distinct().count();
		
		bucketsInCurrentNode = bucketsToNodes.getBucketsToNodes().entrySet()
				.stream().filter(entry -> entry.getValue().contains(nodeProperties.getNodeAddress()))
				.map(entry -> entry.getKey())
				.collect(Collectors.toSet());
	}

	public long getNumberOfNodes() {
		return numberOfNodes;
	}

	public Set<Integer> getBucketsInCurrentNode() {
		return bucketsInCurrentNode;
	}

	
	
}
