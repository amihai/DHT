package ro.amihai.dht.bucketstonodes.observer;

import static java.lang.Math.abs;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

import java.util.Observable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.BucketsToNodes;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;
import ro.amihai.dht.service.health.NodeHealth;

@Component
public class BucketsInCurrentNode extends BucketsToNodesObserver {

	private Logger logger = LoggerFactory.getLogger(BucketsInCurrentNode.class);
	
	@Autowired
	private BucketsToNodes bucketsToNodes;

	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private NodeHealth nodeHealth;
	
	private Set<Integer> bucketsInCurrentNode;

	
	public Set<Integer> getBucketsInCurrentNode() {
		return unmodifiableSet(bucketsInCurrentNode);
	}
	
	public boolean isBucketOnCurrentNode(String key) {
		return bucketsInCurrentNode.contains(Integer.valueOf(bucket(key)));
	}
	
	public boolean isBucketOnCurrentNode(int bucket) {
		return bucketsInCurrentNode.contains(Integer.valueOf(bucket));
	}
	
	public int bucket(String key) {
		return abs(key.hashCode() % nodeProperties.getNoOfBuckets());
	}
	
	public Optional<NodeAddress> externalNodeAddressForKey(String key) {
		int bucket = bucket(key);
		logger.trace("Search node addres for key {} in bucket {}", key, bucket);
		return bucketsToNodes.getBucketsToNodes().getOrDefault(bucket, emptySet())
				.stream().filter(node -> ! node.equals(nodeProperties.getCurrentNodeAddress()))
				.findFirst();
	}
	
	@Override
	public void update(Observable o, Object arg) {
		bucketsInCurrentNode = bucketsToNodes.getBucketsToNodes().entrySet()
				.stream().filter(entry -> entry.getValue().contains(nodeProperties.getCurrentNodeAddress()))
				.map(entry -> entry.getKey())
				.collect(Collectors.toSet());
		
		nodeHealth.setNoOfBuckets(bucketsInCurrentNode.size());
	}

}
