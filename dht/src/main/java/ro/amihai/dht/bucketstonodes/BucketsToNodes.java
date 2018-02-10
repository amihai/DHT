package ro.amihai.dht.bucketstonodes;

import static java.util.function.Function.identity;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.dao.BucketsToNodesDAOFileSystem;
import ro.amihai.dht.bucketstonodes.dao.BucketsToNodesDAONetwork;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;

@Component
public class BucketsToNodes {
	
	private Logger logger = LoggerFactory.getLogger(BucketsToNodes.class);
	
	private Map<Integer, Set<NodeAddress>> bucketsToNodes;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private BucketsToNodesDAOFileSystem fileSystemDAO;
	
	@Autowired
	private BucketsToNodesDAONetwork networkDAO;
	
	@PostConstruct
	public void init() {
		logger.debug("Initialize the mapping");
		bucketsToNodes = networkDAO.load().orElseGet(() -> fileSystemDAO.load().orElseGet(this::initialize) ); 
		fileSystemDAO.saveOrUpdate(bucketsToNodes);
	}

	public void merge(Map<Integer, Set<NodeAddress>> bucketsToNodesToAdd) {
		logger.debug("Merge into the current map {}", bucketsToNodesToAdd);
		
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
	
	public void add(Integer bucket, NodeAddress nodeAdress) {
		logger.debug("Add bucket {} to node {} mapping", bucket, nodeAdress);
		
		bucketsToNodes.getOrDefault(bucket, Collections.emptySet()).add(nodeAdress);
		fileSystemDAO.saveOrUpdate(bucket, bucketsToNodes.get(bucket));
	}
	
	public void remove(Integer bucket, NodeAddress nodeAddress) {
		logger.debug("Remove bucket {} to node {} mapping", bucket, nodeAddress);
		
		bucketsToNodes.getOrDefault(bucket, Collections.emptySet()).remove(nodeAddress);
		fileSystemDAO.saveOrUpdate(bucket, bucketsToNodes.get(bucket));
	}
	
	public void remove(NodeAddress nodeAddress) {
		logger.debug("Remove Node {} from all buckets", nodeAddress);
		bucketsToNodes.entrySet().stream()
		.filter(entry -> entry.getValue().contains(nodeAddress))
		.forEach(entry -> remove(entry.getKey(), nodeAddress));
	}
	
	public void removeAll(Map<Integer, Set<NodeAddress>> bucketsToNodes) {
		logger.debug("Remove all buckest to nodes mapping {} ", bucketsToNodes);
		bucketsToNodes.entrySet().forEach(entry -> {
			entry.getValue().forEach(node -> this.remove(entry.getKey(), node));
		});
	}
	
	public Map<Integer, Set<NodeAddress>> getBucketsToNodes() {
		return Optional.ofNullable(bucketsToNodes)
				.map(Collections::unmodifiableMap)
				.orElseGet(Collections::emptyMap);
	}
	
	private Map<Integer, Set<NodeAddress>> initialize() {
		 return IntStream.range(0, nodeProperties.getNoOfBuckets())
			.boxed()
			.collect(Collectors.toConcurrentMap(
					identity(), 
					i -> Stream.of(nodeProperties.getCurrentNodeAddress()).collect(Collectors.toSet())));
	}
	
}
