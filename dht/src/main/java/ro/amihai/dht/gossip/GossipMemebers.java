package ro.amihai.dht.gossip;

import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.observer.AllNodes;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;

@Component
public class GossipMemebers {

	private Logger logger = LoggerFactory.getLogger(GossipMemebers.class);
	
	@Autowired
	private AllNodes allNodes;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	/**
	 * Return a shuffled set of nodes that can be use to gossip with.
	 */
	public Set<NodeAddress> shuffledGossipMembers() {
		//More than half of the nodes
		int gossipMembersSize = (allNodes.getAllNodes().size() / 2) + 1;
		
		logger.trace("GossipMembersSize is {}", gossipMembersSize);
		
		List<NodeAddress> allNodeExceptCurrent = allNodes.getAllNodes().stream()
			.filter(this::excludeCurrentNode)
			.collect(toList());
		
		shuffle(allNodeExceptCurrent);
		
		logger.trace("Suffled node addresses {}", allNodeExceptCurrent);
		
		return allNodeExceptCurrent.stream()
				.limit(gossipMembersSize)
				.collect(toSet());
	}
	
	private boolean excludeCurrentNode(NodeAddress nodeAddress) {
		return ! nodeAddress.equals(nodeProperties.getCurrentNodeAddress());
	}
}
