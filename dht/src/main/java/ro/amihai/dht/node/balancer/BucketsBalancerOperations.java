package ro.amihai.dht.node.balancer;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import ro.amihai.dht.bucketstonodes.BucketsToNodes;
import ro.amihai.dht.keyvaluestore.KeyValue;
import ro.amihai.dht.keyvaluestore.dao.KeyValueDAOFileSystem;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;

@Component
public class BucketsBalancerOperations {

	private Logger logger = LoggerFactory.getLogger(BucketsBalancerOperations.class);

	@Min(1)
	@Max(64)
	@Value("${bucketsToNodes.balancing.replicationFactor}")
	private int replicationFactor;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private KeyValueDAOFileSystem keyValueDAOFileSystem;
	
	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	@Autowired
	private RestTemplate restTemplate;
	
	public void transferBucket(Integer bucket, NodeAddress node) {
		logger.info("Start to transfer bucket {} from node {}", bucket, node);
		copyBucketFromNode(bucket, node);
		
		logger.info("Remove bucket {} to node {} mapping from the busy node", bucket, node);
		try {
			URI deleteBucketToNodeMappingURL = node.getURI(String.join("/", "/bucketsToNodes", valueOf(bucket), node.getHost(), valueOf(node.getPort())), null);
			restTemplate.delete(deleteBucketToNodeMappingURL);
			bucketsToNodes.remove(bucket, node);
		} catch (URISyntaxException e) {
			logger.error("Cannot remve the bucket to node mapping", e);
		}
		
	}
	
	public boolean copyBucket(Map.Entry<Integer, Set<NodeAddress>> bucketAndNodes) {
		Optional<NodeAddress> firstNode = bucketAndNodes.getValue().stream()
				.filter(node -> ! node.equals(nodeProperties.getCurrentNodeAddress()))
				.findFirst();
		Integer bucket = bucketAndNodes.getKey();
		if (firstNode.isPresent()) {
			NodeAddress nodeAddress = firstNode.get();
			logger.info("Start to copy bucket {} from node ", bucket, nodeAddress);
			return copyBucketFromNode(bucket, nodeAddress);
		} else {
			logger.error("Bucket {} is not stored into the network", bucket);
		}
		return false;
	}

	private boolean copyBucketFromNode(Integer bucket, NodeAddress nodeAddress) {
		try {
			//Copy KeyValues
			ResponseEntity<List> response = restTemplate.getForEntity(nodeAddress.getURI("/buckets/"+bucket, null), List.class);
			response.getBody().stream()
				.forEach(this::saveKeyValue);
			
			logger.info("The Bucket {} was copied", bucket);
			
			//Add the new mapping in the BucketsToNodes mapping
			bucketsToNodes.add(bucket, nodeProperties.getCurrentNodeAddress());
			return addRemoteMapping(nodeAddress, bucket, nodeProperties.getCurrentNodeAddress());
		} catch (RestClientException | URISyntaxException e) {
			logger.error("Cannot copy bucket:", e);
		}
		return false;
	}
	
	private boolean addRemoteMapping(NodeAddress targetNode, Integer bucket, NodeAddress nodeToBeMapped) {
		Map<Integer, Set<NodeAddress>> newMapping = new HashMap<>();
		newMapping.put(bucket, of(nodeToBeMapped).collect(toSet()));
		try {
			URI addMappingURI = targetNode.getURI("/bucketsToNodes", null);
			ResponseEntity<Map> response = restTemplate.postForEntity(addMappingURI, newMapping, Map.class);
			return response.getStatusCode().is2xxSuccessful();
		} catch (URISyntaxException e) {
			logger.error("Cannot update the remote mapping from the node {}", targetNode);
		}
		return false;
	}
	
	private boolean saveKeyValue(Object json) {
		KeyValue keyValue = new ObjectMapper().convertValue(json, KeyValue.class);
		return keyValueDAOFileSystem.saveOrUpdate(keyValue);
	}
	
}
