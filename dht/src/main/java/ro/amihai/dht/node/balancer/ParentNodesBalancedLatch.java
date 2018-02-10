package ro.amihai.dht.node.balancer;

import static java.lang.String.join;

import java.util.Arrays;
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
import org.springframework.web.client.RestTemplate;

import ro.amihai.dht.health.NodeHealth;
import ro.amihai.dht.health.NodeStatus;
import ro.amihai.dht.node.NodeProperties;

@Component
public class ParentNodesBalancedLatch {

	private Logger logger = LoggerFactory.getLogger(ParentNodesBalancedLatch.class);
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private RestTemplate restTemplate;
	
	private CountDownLatch parentNodesToBeBalanced; 
	
	@PostConstruct
	private void init() {
		Optional<String[]> initializeFromNodes = nodeProperties.getInitializeFromNodes();
		String[] parentNodes = initializeFromNodes.orElseGet(() -> new String[0]);
		if (parentNodes.length == 0) {
			logger.info("No parent to check if balanced");
			parentNodesToBeBalanced = new CountDownLatch(0);
		} else {
			logger.info("Found {} parent nodes. Wait for parent nodes to be balanced", parentNodes.length);
			parentNodesToBeBalanced = new CountDownLatch(1);
			scheduleParentNodesChecking(parentNodes);
		}
	}
	
	public void await() throws InterruptedException {
		parentNodesToBeBalanced.await();
	}

	private void scheduleParentNodesChecking(String[] initializeFromNodes) {
		final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
		
		threadPool.scheduleAtFixedRate(() -> {
			Optional<NodeHealth> unbalancedParent = Arrays.stream(initializeFromNodes)
				.flatMap(this::getNodeHealth)
				.filter(this::isUnbalanced)
				.findFirst();
			
			if (unbalancedParent.isPresent()) {
				logger.info("Node {} is not yet balanced ", unbalancedParent.get());
			} else {
				logger.debug("All nodes are balanced");
				parentNodesToBeBalanced.countDown();
				threadPool.shutdown();
			}
			
		}, 1, 2, TimeUnit.SECONDS);
		
		
	}
	
	private boolean isUnbalanced(NodeHealth nodeHealth) {
		return NodeStatus.UNBALANCED == nodeHealth.getNodeStatus();
	}
	private Stream<NodeHealth> getNodeHealth(String nodeAddress) {
		ResponseEntity<NodeHealth> response = restTemplate.getForEntity(join("/", "http:/", nodeAddress, "health") , NodeHealth.class);
		if (response.getStatusCode().is2xxSuccessful()) {
			return Stream.of(response.getBody());
		}
		return Stream.empty();
	}
}
