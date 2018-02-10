package ro.amihai.dht.bucketstonodes;

import static java.lang.Math.abs;
import static java.util.Collections.emptySet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;
import ro.amihai.dht.service.health.NodeHealth;

/**
 * For performance, this class cache some statistics that can be retrieved from BucketsToNodes. This statistics are modified only when the BucketsToNodeIsModified
 *
 */
@Component
public class BucketsToNodesStatistics {

	private Logger logger = LoggerFactory.getLogger(BucketsToNodesStatistics.class);
	
	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	private Set<Integer> bucketsInCurrentNode;

	private Map<NodeAddress, List<Integer>> nodesToBuckets;
	
	@Autowired
	private NodeHealth nodeHealth;
	
	private Set<NodeAddress> allNodes;
	
	@PostConstruct
	private void init() {
		logger.debug("Initialize the BucketsToNodes statistics");
		updateStatistics();
	}
	
	public void updateStatistics() {
		logger.debug("Update Statistics");
		
		allNodes = new HashSet<>();
		allNodes.add(nodeProperties.getCurrentNodeAddress());
		allNodes.addAll(bucketsToNodes.getBucketsToNodes().values()
				.stream().flatMap(set -> set.stream()).collect(toSet()));
		
		bucketsInCurrentNode = bucketsToNodes.getBucketsToNodes().entrySet()
				.stream().filter(entry -> entry.getValue().contains(nodeProperties.getCurrentNodeAddress()))
				.map(entry -> entry.getKey())
				.collect(Collectors.toSet());
		
		nodeHealth.setNoOfBuckets(bucketsInCurrentNode.size());
		
		nodesToBuckets = bucketsToNodes.getBucketsToNodes().entrySet()
				.stream().flatMap(this::revertKeyWithValues)
				.collect(groupingBy(Map.Entry::getKey,
						mapping(Map.Entry::getValue,        
                                toList())
						));
	}
	
	private Stream<Map.Entry<NodeAddress, Integer>> revertKeyWithValues(Map.Entry<Integer, Set<NodeAddress>> entry) {
		return entry.getValue().stream()
			.collect(toMap(identity(), na -> entry.getKey()))
			.entrySet().stream();
	}

	public long getNumberOfNodes() {
		return allNodes.size();
	}
	
	public Set<NodeAddress> getAllNodes() {
		return allNodes;
	}

	public Set<Integer> getBucketsInCurrentNode() {
		return bucketsInCurrentNode;
	}
	
	public Map<NodeAddress, List<Integer>> getNodesToBuckets() {
		return nodesToBuckets;
	}
	
	public int bucket(String key) {
		return abs(key.hashCode() % nodeProperties.getNoOfBuckets());
	}
	
	public boolean isBucketOnCurrentNode(String key) {
		return bucketsInCurrentNode.contains(Integer.valueOf(bucket(key)));
	}
	
	public boolean isBucketOnCurrentNode(int bucket) {
		return bucketsInCurrentNode.contains(Integer.valueOf(bucket));
	}
	
	public Optional<NodeAddress> externalNodeAddressForKey(String key) {
		int bucket = bucket(key);
		logger.trace("Search node addres for key {} in bucket {}", key, bucket);
		return bucketsToNodes.getBucketsToNodes().getOrDefault(bucket, emptySet())
				.stream().filter(node -> ! node.equals(nodeProperties.getCurrentNodeAddress()))
				.findFirst();
	}
}
