package ro.amihai.dht.node.balancer;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.BucketsToNodesStatistics;
import ro.amihai.dht.node.NodeProperties;

/**
 * Service that balance the buckets inside topology. 
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
	private NodeProperties nodeProperties;
	
	@Autowired
	private BucketsToNodesStatistics bucketsToNodesStatistics;
	
	@Scheduled(fixedRateString = "${bucketsToNodes.balancing.rate}", initialDelayString = "${bucketsToNodes.balancing.initialDelay}")
	public void balanceBuckets() {
		logger.info("Start the Buckets balancing");
		
		while(!isCurrentNodeBalanced()) {
			//TODO start 
		}
		
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
