package ro.amihai.dht.gossip;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static ro.amihai.dht.gossip.GossipAction.ADD;
import static ro.amihai.dht.gossip.GossipAction.REMOVE;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ro.amihai.dht.keyvaluestore.KeyValue;
import ro.amihai.dht.node.NodeAddress;

@Component
public class GossipRegistry {

	private Logger logger = LoggerFactory.getLogger(GossipRegistry.class);
	
	@Value("${gossip.doneSize}")
	private int gossipsDoneSize; 
	
	/**
	 * We need to store a history of gossips to avoid infinite loop of the same gossip into the network.
	 */
	private CircularFifoQueue<Gossip> gossipsDone;
	
	private Queue<Gossip> gossipToDo = new ConcurrentLinkedQueue<>();
	
	@PostConstruct
	private void init() {
		gossipsDone = new CircularFifoQueue<Gossip>(gossipsDoneSize);
	}
	
	public boolean registerGossip(Gossip gossip) {
		if (gossipsDone.contains(gossip)) {
			logger.info("Gossip already done:" , gossip);
			return false;
		} else {
			logger.info("Gossip registered:" , gossip);
			gossipToDo.add(gossip);
			return true;
		}
	}
	
	public void gossipKeyRemoved(String key) {
		logger.debug("Register gossip of Removing key {}", key);
		gossipToDo.add(new Gossip(REMOVE, new KeyValue(key, null)));
	}

	public void gossipKeyValueAdded(KeyValue keyValue) {
		logger.debug("Register gossip of adding key value mapping {}", keyValue);
		gossipToDo.add(new Gossip(ADD, keyValue));
	}
	
	public void gossipBucketsToNodesMappingAdded(Map<Integer, Set<NodeAddress>> bucketsToNodesToAdd) {
		gossipToDo.add(new Gossip(ADD, bucketsToNodesToAdd));
	}
	
	public void gossipBucketsToNodesMappingRemoved(Integer bucket, NodeAddress nodeAddress) {
		Map<Integer, Set<NodeAddress>> mappingToRemove = new HashMap<>();
		mappingToRemove.put(bucket, of(nodeAddress).collect(toSet()));
		gossipToDo.add(new Gossip(ADD, mappingToRemove));
	}
	
	public CircularFifoQueue<Gossip> getGossipsDone() {
		return gossipsDone;
	}

	public Queue<Gossip> getGossipToDo() {
		return gossipToDo;
	}
	
}
