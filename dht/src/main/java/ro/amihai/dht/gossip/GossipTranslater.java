package ro.amihai.dht.gossip;

import static ro.amihai.dht.gossip.GossipAction.ADD;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.BucketsToNodes;
import ro.amihai.dht.bucketstonodes.BucketsToNodesStatistics;
import ro.amihai.dht.keyvaluestore.KeyValue;
import ro.amihai.dht.keyvaluestore.dao.KeyValueDAOFileSystem;

/**
 * Apply a Gossip on the Current Node 
 */
@Component
public class GossipTranslater {

	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	@Autowired
	private BucketsToNodesStatistics bucketsToNodesStatistics;
	
	@Autowired
	private KeyValueDAOFileSystem keyValueDAOFileSystem;
	
	public void applyGossipToCurrentNode(Gossip gossip) {
		if (null != gossip.getBucketsToNodes()) {
			if (ADD == gossip.getAction()) {
				bucketsToNodes.merge(gossip.getBucketsToNodes());
			} else {
				bucketsToNodes.removeAll(gossip.getBucketsToNodes());
			}
		}
		KeyValue keyValue = gossip.getKeyValue();
		if (null != keyValue && bucketsToNodesStatistics.isBucketOnCurrentNode(keyValue.getKey())) {
			if (ADD == gossip.getAction()) {
				keyValueDAOFileSystem.saveOrUpdate(keyValue);
			} else {
				keyValueDAOFileSystem.delete(keyValue.getKey());
			}
		}
	}
	
}
