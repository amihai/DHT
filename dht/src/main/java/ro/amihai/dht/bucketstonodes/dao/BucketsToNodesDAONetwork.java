package ro.amihai.dht.bucketstonodes.dao;

import static java.lang.String.join;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import ro.amihai.dht.bucketstonodes.BucketsToNodesTypeConvertor;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;

@Component
public class BucketsToNodesDAONetwork implements BucketsToNodesDAO {

	private Logger logger = LoggerFactory.getLogger(BucketsToNodesDAONetwork.class);
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private BucketsToNodesTypeConvertor bucketsToNodesTypeConvertor;
	
	@Override
	public Optional<Map<Integer, Set<NodeAddress>>> load() {
		logger.debug("Load Buckets To Nodes mapping from the network");
		
		Optional<String[]> initializeFromNodes = nodeProperties.getInitializeFromNodes();
		if (initializeFromNodes.isPresent()) {
			for (String nodeAddress : initializeFromNodes.get()) {
				try {
				ResponseEntity<Map> response = restTemplate.getForEntity(join("/", "http:/", nodeAddress, "bucketsToNodes") , Map.class);
					if (response.getStatusCode().is2xxSuccessful()) {
						Map<Integer, Set<NodeAddress>> fromJson = bucketsToNodesTypeConvertor.toBucketsToNodesMap(response.getBody());
						return Optional.of(fromJson);
					} else {
						logger.warn("Node {} is not responding", nodeAddress);
					}
				} catch (ResourceAccessException e) {
					logger.error("Error calling the peers nodes:", e);
				}
				
			}
		}
		return Optional.empty();
	}

	@Override
	public void saveOrUpdate(Integer bucket, Set<NodeAddress> nodeAddreses) {
		throw new UnsupportedOperationException("The Buckets TO Nodes mapping is propagated on the newtwork using the Gossip Service");
	}
	
	@Override
	public void saveOrUpdate(Map<Integer, Set<NodeAddress>> bucketsToNodes) {
		throw new UnsupportedOperationException("The Buckets TO Nodes mapping is propagated on the newtwork using the Gossip Service");
	}


}
