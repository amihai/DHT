package ro.amihai.dht.bucketstonodes.observer;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.BucketsToNodes;
import ro.amihai.dht.node.NodeAddress;

/**
 * For performance, this class cache some statistics that can be retrieved from BucketsToNodes. This statistics are modified only when the BucketsToNodeIsModified
 *
 */
@Component
public class NodesToBuckets extends BucketsToNodesObserver {

	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	private Map<NodeAddress, List<Integer>> nodesToBuckets;
	
	
	public Map<NodeAddress, List<Integer>> getNodesToBuckets() {
		return Collections.unmodifiableMap(nodesToBuckets);
	}
	
	@Override
	public void update(Observable o, Object arg) {
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
}
