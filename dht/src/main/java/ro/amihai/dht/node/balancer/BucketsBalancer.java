package ro.amihai.dht.node.balancer;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import ro.amihai.dht.bucketstonodes.BucketsToNodes;
import ro.amihai.dht.bucketstonodes.BucketsToNodesStatistics;
import ro.amihai.dht.bucketstonodes.dao.BucketsToNodesDAOFileSystem;
import ro.amihai.dht.keyvaluestore.KeyValue;
import ro.amihai.dht.keyvaluestore.dao.KeyValueDAOFileSystem;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;

/**
 * Scheduler that balance the buckets inside topology. 
 * Current node will take new buckets from the network or throw new buckets to the network
 * depends on the number of buckets he already store.
 *
 */
@Component
public class BucketsBalancer {

	private Logger logger = LoggerFactory.getLogger(BucketsBalancer.class);

	@Min(1)
	@Max(64)
	@Value("${bucketsToNodes.balancing.replicationFactor}")
	private int replicationFactor;
	
	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private BucketsToNodesStatistics bucketsToNodesStatistics;
	
	@Autowired
	private KeyValueDAOFileSystem keyValueDAOFileSystem;
	
	@Autowired
	private BucketsToNodesDAOFileSystem bucketsToNodesDAOFileSystem;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Scheduled(fixedRateString = "${bucketsToNodes.balancing.rate}", initialDelayString = "${bucketsToNodes.balancing.initialDelay}")
	public void balanceBuckets() {
		logger.info("Start the Buckets balancing");
		
		//First we check if any bucket is store on a number of nodes less than the replication factor
		bucketsNotReplicated().entrySet().forEach(this::copyBucket);
		
		//Than check if any other node is loaded more than currentNode
		
		//Search if other nodes are busy and transfer some buckets from them
//		while(!isCurrentNodeBalanced()) {
//			
//			
//			//Search a busy node and transfer a bucket from it
//			
//		}
		
	}
	
	private void copyBucket(Map.Entry<Integer, Set<NodeAddress>> bucketAndNodes) {
		final NodeAddress currentNode = nodeProperties.getNodeAddress();
		Optional<NodeAddress> firstNode = bucketAndNodes.getValue().stream()
				.filter(node -> ! node.equals(currentNode)).findFirst();
		Integer bucket = bucketAndNodes.getKey();
		if (firstNode.isPresent()) {
			logger.info("Start to copy bucket {}", bucket);
			try {
				//Copy KeyValues
				ResponseEntity<List> response = restTemplate.getForEntity(firstNode.get().getURI("/buckets/"+bucket, null), List.class);
				response.getBody().stream()
					.forEach(this::saveKeyValue);
				
				logger.info("The Bcuket {} was copied", bucket);
				
				//Add the new mapping in the BucketsToNodes mapping
				Map<Integer, Set<NodeAddress>> newBucketsToNode = of(bucket)
							.collect(
									toMap(Function.identity(), 
											b -> of(currentNode).collect(toSet())
											));
				bucketsToNodesDAOFileSystem.saveOrUpdate(newBucketsToNode);
				
			} catch (RestClientException | URISyntaxException e) {
				logger.error("Cannot copy bucket:", e);
			}
		} else {
			logger.error("Bucket {} is not stored into the network", bucket);
		}
	}
	
	private boolean saveKeyValue(Object json) {
		KeyValue keyValue = new ObjectMapper().convertValue(json, KeyValue.class);
		return keyValueDAOFileSystem.saveOrUpdate(keyValue);
	}
	
	private Map<Integer, Set<NodeAddress>> bucketsNotReplicated() {
		return bucketsToNodes.getBucketsToNodes().entrySet()
			.stream()
			.filter(entry -> ! entry.getValue().contains(nodeProperties.getNodeAddress())) //Filter the buckets already stored o current node
			.filter(entry -> entry.getValue().size() < replicationFactor)
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
	
	private boolean isCurrentNodeBalanced() {
		long numberOfNodes = bucketsToNodesStatistics.getNumberOfNodes();
		logger.debug("Total number of nodes: {}", numberOfNodes);
		
		int noOfBuckets = nodeProperties.getNoOfBuckets();
		int minimumNumberOfBucketsPerNode = (int) Math.ceil(replicationFactor * noOfBuckets / numberOfNodes); 
		minimumNumberOfBucketsPerNode = Math.min(minimumNumberOfBucketsPerNode, noOfBuckets);
		
		logger.debug("Minimum Number Of Buckets Per Node: {}", minimumNumberOfBucketsPerNode);
		
		int noOfBucketsInCurrentNode = bucketsToNodesStatistics.getBucketsInCurrentNode().size();
		
		if (minimumNumberOfBucketsPerNode > noOfBucketsInCurrentNode) {
			int noOfBucketsToCopy = minimumNumberOfBucketsPerNode - noOfBucketsInCurrentNode;
			logger.info("Need to balance the topology. Current node need to copy {} buckets", noOfBucketsToCopy);
			return false;
		} else {
			logger.info("No balancing is necesary for the current node. Current node contains {} buckets", noOfBucketsInCurrentNode);
			return true;
		}
	}
}
