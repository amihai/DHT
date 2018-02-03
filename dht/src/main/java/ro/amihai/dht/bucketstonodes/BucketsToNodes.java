package ro.amihai.dht.bucketstonodes;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.dao.BucketsToNodesDAOFileSystem;
import ro.amihai.dht.bucketstonodes.dao.BucketsToNodesDAONetwork;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;

@Component
public class BucketsToNodes {
	
	private Map<Integer, Set<NodeAddress>> bucketsToNodes;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private BucketsToNodesDAOFileSystem fileSystemDAO;
	
	@Autowired
	private BucketsToNodesDAONetwork networkDAO;
	
	@PostConstruct
	public void init() {
		bucketsToNodes = networkDAO.load().orElseGet(() -> fileSystemDAO.load().orElseGet(this::initialize) ); 
		fileSystemDAO.saveOrUpdate(bucketsToNodes);
	}

	public void merge(Map<Integer, Set<NodeAddress>> bucketsToNodesToAdd) {
		bucketsToNodes = Stream.of(bucketsToNodes, bucketsToNodesToAdd)
			.map(Map::entrySet)
			.flatMap(Collection::stream)
			.collect(Collectors.toConcurrentMap(
					Map.Entry::getKey, 
					Map.Entry::getValue, 
					(set1, set2) -> { 
						set1.addAll(set2); return set1;
						})
					);
		
		fileSystemDAO.saveOrUpdate(bucketsToNodes);
	}
	
	public void remove(Integer bucket, NodeAddress nodeAdress) {
		bucketsToNodes.getOrDefault(bucket, Collections.emptySet()).remove(nodeAdress);
		fileSystemDAO.saveOrUpdate(bucketsToNodes);
	}
	
	public void removeBucketFromNode(Integer bucket, NodeAddress nodeAddress) {
		 bucketsToNodes.getOrDefault(bucket, emptySet()).remove(nodeAddress);
		 fileSystemDAO.saveOrUpdate(bucketsToNodes);
	}
	
	public Map<Integer, Set<NodeAddress>> getBucketsToNodes() {
		if (null != bucketsToNodes) {
			return unmodifiableMap(bucketsToNodes);
		} else {
			return Collections.emptyMap();
		}
		
	}
	
	private Map<Integer, Set<NodeAddress>> initialize() {
		 return IntStream.range(0, nodeProperties.getNoOfBuckets())
			.boxed()
			.collect(Collectors.toConcurrentMap(identity(), i -> new HashSet<>(asList(nodeProperties.getNodeAddress()))));
	}
	
}
