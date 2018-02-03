package ro.amihai.dht.bucketstonodes.dao;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ro.amihai.dht.node.NodeAddress;

public interface BucketsToNodesDAO {
	
	public void saveOrUpdate(Map<Integer, Set<NodeAddress>> bucketsToNodes);
	public void saveOrUpdate(Integer bucket, Set<NodeAddress> nodeAddreses);
	public Optional<Map<Integer, Set<NodeAddress>>> load();
	
}
