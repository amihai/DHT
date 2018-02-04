package ro.amihai.dht.gossip;

import static ro.amihai.dht.health.NodeStatus.UNBALANCED;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import ro.amihai.dht.health.NodeHealth;
import ro.amihai.dht.node.NodeAddress;

@Component
public class GossipScheduler {

	private Logger logger = LoggerFactory.getLogger(GossipScheduler.class);
	
	@Autowired
	private GossipRegistry gossipRegistry;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private NodeHealth nodeHealth;
	
	@Autowired
	private GossipMemebers gossipMemebers;
	
	@Scheduled(fixedRateString="${gossip.fixedRate}")
	private void gossip() {
		if (UNBALANCED == nodeHealth.getNodeStatus()) {
			logger.debug("Current node is not yet balanced. Gossip will start after the node is balanced");
		} else {
			logger.trace("Current node is balanced. Start Gossip scheduler");
			
			Queue<Gossip> gossipToDo = gossipRegistry.getGossipToDo();
			Set<NodeAddress> shuffledGossipMembers = gossipMemebers.shuffledGossipMembers();
			while(!gossipToDo.isEmpty()) {
				Gossip nextGossip = gossipToDo.poll();
				CircularFifoQueue<Gossip> gossipsDone = gossipRegistry.getGossipsDone();
				if(gossipsDone.contains(nextGossip)) {
					logger.info("Gossip already sent: {}", nextGossip);
				} else {
					shuffledGossipMembers.forEach(node -> gossipToNode(nextGossip, node));
					gossipsDone.add(nextGossip);
					logger.info("Gossip done for: {}", nextGossip);
				}
			}
		}
	}
	
	private boolean gossipToNode(Gossip gossip, NodeAddress nodeAddress) {
		logger.debug("Start sending gossip {} to node {}", gossip, nodeAddress);
		try {
			URI uriGossip = nodeAddress.getURI("/gossip", null);
			restTemplate.put(uriGossip, gossip);
			logger.debug("Done sending gossip {} to node {}", gossip, nodeAddress);
			return true;
		} catch (URISyntaxException e) {
			logger.error("Cannot gossip to node", e);
		}
		return false;
	}
	
}
