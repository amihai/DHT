package ro.amihai.dht.node.balancer;

import static java.lang.String.join;
import static ro.amihai.dht.health.NodeStatus.BALANCED;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import ro.amihai.dht.health.NodeHealth;
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
			logger.info("Found {} parent nodes. Wait for at least one to be balanced", parentNodes.length);
			parentNodesToBeBalanced = new CountDownLatch(1); //At least on node to be balanced
			scheduleParentNodesChecking(parentNodes);
		}
	}
	
	public void awaitForAParentNodeToBeBalanced() throws InterruptedException {
		parentNodesToBeBalanced.await();
	}

	private void scheduleParentNodesChecking(String[] initializeFromNodes) {
		final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(initializeFromNodes.length);
		for (String initializeFromNode: initializeFromNodes) {
			threadPool.scheduleAtFixedRate(() -> {
				ResponseEntity<NodeHealth> response = restTemplate.getForEntity(join("/", "http:/", initializeFromNode, "health") , NodeHealth.class);
				if (response.getStatusCode().is2xxSuccessful()) {
					NodeHealth nodeHealth = response.getBody();
					if (BALANCED == nodeHealth.getNodeStatus() && 0 == nodeHealth.getGossipSize()) {
						logger.info("Found one parent BALANCED");
						parentNodesToBeBalanced.countDown();
						threadPool.shutdown();
					}
				}
			}, 1, 2, TimeUnit.SECONDS);
		}
	}
		

}
