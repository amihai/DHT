package ro.amihai.dht.node.balancer;

import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import ro.amihai.dht.gossip.GossipMemebers;
import ro.amihai.dht.health.NodeHealth;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;

@Component
public class OverlayNetworkStableLatch {

	private Logger logger = LoggerFactory.getLogger(OverlayNetworkStableLatch.class);
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private GossipMemebers gossipMemebers;
	
	private CountDownLatch overlayNetworkStable; 
	
	@PostConstruct
	private void init() {
		Optional<String[]> initializeFromNodes = nodeProperties.getInitializeFromNodes();
		String[] parentNodes = initializeFromNodes.orElseGet(() -> new String[0]);
		if (parentNodes.length == 0) {
			logger.info("No parent to check if balanced. Current Node is the first");
			overlayNetworkStable = new CountDownLatch(0);
		} else {
			logger.info("Found {} parent nodes. Wait for the Overlay Netowrk to be balanced", parentNodes.length);
			overlayNetworkStable = new CountDownLatch(1);
			scheduleOverlayNetworkChecking();
		}
	}
	
	public void await() throws InterruptedException {
		overlayNetworkStable.await();
	}
	
	private void scheduleOverlayNetworkChecking() {
		final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
			threadPool.scheduleAtFixedRate(() -> {
				Optional<NodeHealth> unbalancedNode = gossipMemebers.shuffledGossipMembers()
					.stream()
					.flatMap(this::getNodeHealth)
					.filter(this::isUnbalanced)
					.findFirst();
					
				if (unbalancedNode.isPresent()) {
					logger.info("Node {} is not yet balanced ", unbalancedNode.get());
				} else {
					logger.debug("All nodes are balanced");
					overlayNetworkStable.countDown();
					threadPool.shutdown();
				}
				
			}, 1, 2, TimeUnit.SECONDS);
		}

	private boolean isUnbalanced(NodeHealth nodeHealth) {
		return nodeHealth.getGossipSize() > 0; 
	}
	
	private Stream<NodeHealth> getNodeHealth(NodeAddress nodeAddress) {
		try {
			ResponseEntity<NodeHealth> response = restTemplate.getForEntity(nodeAddress.getURI("/health", null) , NodeHealth.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				return Stream.of(response.getBody());
			}
		} catch (RestClientException | URISyntaxException e) {
			logger.error("Error when try to get the health of the Node {}", nodeAddress, e);
		}
		return Stream.empty();
	}

}
