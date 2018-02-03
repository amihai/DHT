package ro.amihai.dht.gossip;

import static java.util.Collections.shuffle;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import ro.amihai.dht.bucketstonodes.BucketsToNodesStatistics;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;

@Component
public class GossipScheduler {

	private Logger logger = LoggerFactory.getLogger(GossipScheduler.class);
	
	@Autowired
	private GossipRegistry gossipRegistry;
	
	@Autowired
	private BucketsToNodesStatistics bucketsToNodesStatistics;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Scheduled(fixedRateString="${gossip.fixedRate}")
	private void gossip() {
		Queue<Gossip> gossipToDo = gossipRegistry.getGossipToDo();
		while(!gossipToDo.isEmpty()) {
			Gossip nextGossip = gossipToDo.poll();
			CircularFifoQueue<Gossip> gossipsDone = gossipRegistry.getGossipsDone();
			if(gossipsDone.contains(nextGossip)) {
				logger.info("Gossip already sent: {}", nextGossip);
			} else {
				gossipMembers().forEach(node -> gossipToNode(nextGossip, node));
				gossipsDone.add(nextGossip);
				logger.info("Gossip done for: {}", nextGossip);
			}
		}
	}
	
	private boolean gossipToNode(Gossip gossip, NodeAddress nodeAddress) {
		logger.debug("Start sending gossip {} to node {}", gossip, nodeAddress);
		try {
			URI uriGossip = nodeAddress.getURI("/gossip", null);
			restTemplate.put(uriGossip, gossip);
			logger.debug("Done sending gossip {} to node {}", gossip, nodeAddress);
		} catch (URISyntaxException e) {
			logger.error("Cannot gossip to node", e);
		}
		return false;
	}
	
	private Set<NodeAddress> gossipMembers() {
		//More than half of the nodes
		int gossipMembersSize = (bucketsToNodesStatistics.getAllNodes().size() / 2) + 1;
		
		logger.debug("GossipMembersSize is {}", gossipMembersSize);
		
		List<NodeAddress> allNodeExceptCurrent = bucketsToNodesStatistics.getAllNodes().stream()
			.filter(node -> ! node.equals(nodeProperties.getCurrentNodeAddress()))
			.collect(Collectors.toList());
		
		shuffle(allNodeExceptCurrent);
		
		logger.debug("Suffled node addresses {}", allNodeExceptCurrent);
		
		return allNodeExceptCurrent.stream()
				.limit(gossipMembersSize)
				.collect(Collectors.toSet());
	}
}
