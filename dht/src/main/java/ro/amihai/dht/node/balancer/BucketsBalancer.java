package ro.amihai.dht.node.balancer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.BucketsToNodes;
import ro.amihai.dht.bucketstonodes.BucketsToNodesStatistics;
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
	private BucketsBalancerOperations bucketsBalancerOperations;
	
	@Scheduled(fixedRateString = "${bucketsToNodes.balancing.rate}", initialDelayString = "${bucketsToNodes.balancing.initialDelay}")
	public void balanceBuckets() {
		logger.info("Start the Buckets balancing");
		
		//First we check if any bucket is store on a number of nodes less than the replication factor
		bucketsNotReplicated().entrySet().forEach(bucketsBalancerOperations::copyBucket);
		
		int noOfBucketsInCurrentNode = bucketsToNodesStatistics.getBucketsInCurrentNode().size();
		//Than check if any other node is loaded more than currentNode
		while(noOfBucketsInCurrentNode < minimumNumberOfBucketsPerNode()) {
			logger.info("Start to search a busy node to transfer from");
			
			//Search if other nodes are busy and transfer some buckets from them
			Optional<Entry<NodeAddress, List<Integer>>> busyNode = bucketsToNodesStatistics.getNodesToBuckets()
				.entrySet().stream()
				.filter(entry -> entry.getValue().size() > minimumNumberOfBucketsPerNode()) //Filter the nodes that are not busy
				.sorted((e1, e2) -> e2.getValue().size() - e1.getValue().size())
				.findFirst();
			
			if (busyNode.isPresent()) {
				Optional<Integer> bucketToTransfer = busyNode.get().getValue().stream()
					.filter(bucket -> ! bucketsToNodesStatistics.getBucketsInCurrentNode().contains(bucket)) //Filter buckets already on this node
					.findAny();
				
				if (bucketToTransfer.isPresent()) {
					Integer bucket = bucketToTransfer.get();
					NodeAddress node = busyNode.get().getKey();
					bucketsBalancerOperations.transferBucket(bucket, node);
					logger.info("The bucket {} was transfered from node {} to current node", bucket, node);
				} else {
					logger.error("Cannot find any bucket to transfer");
					break;
				}
			} else {
				logger.error("Cannot find any busy node");
				break;
			}
		}
		
		logger.info("Bucket Balancing is done");
	}
	
	
	
	private Map<Integer, Set<NodeAddress>> bucketsNotReplicated() {
		return bucketsToNodes.getBucketsToNodes().entrySet()
			.stream()
			.filter(entry -> ! entry.getValue().contains(nodeProperties.getCurrentNodeAddress())) //Filter the buckets already stored o current node
			.filter(entry -> entry.getValue().size() < replicationFactor)
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
	
	private int minimumNumberOfBucketsPerNode() {
		long numberOfNodes = bucketsToNodesStatistics.getNumberOfNodes();
		logger.debug("Total number of nodes: {}", numberOfNodes);
		
		int noOfBuckets = nodeProperties.getNoOfBuckets();
		int minimumNumberOfBucketsPerNode = (int) Math.ceil(replicationFactor * noOfBuckets / numberOfNodes); 
		return Math.min(minimumNumberOfBucketsPerNode, noOfBuckets);
	}
}
