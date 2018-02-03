package ro.amihai.dht.bucketstonodes;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import ro.amihai.dht.gossip.GossipRegistry;
import ro.amihai.dht.keyvaluestore.size.BucketSize;
import ro.amihai.dht.keyvaluestore.size.BucketsSizeCache;
import ro.amihai.dht.node.NodeAddress;

@RestController
public class BucketsToNodesService {
	
	private Logger logger = LoggerFactory.getLogger(BucketsToNodesService.class);
	
	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	@Autowired
	private GossipRegistry gossipRegistry;
	
	@Autowired
	private BucketsSizeCache bucketsSizeCache;
	
	@ApiOperation("Return the Mapping between Buckets and Nodes")
	@RequestMapping(method={RequestMethod.GET},value={"/bucketsToNodes"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<Integer, Set<NodeAddress>> getBucketsToNodes() {
		return bucketsToNodes.getBucketsToNodes();
	}
	
	@ApiOperation("Return the Size of each Bucket")
	@RequestMapping(method={RequestMethod.GET},value={"/buckets/size"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<Integer, BucketSize> getBucketsSize() {
		return bucketsSizeCache.getBucketSize();
	}
	
	@ApiOperation("Merge the internal Mapping with the one received")
	@RequestMapping(method={RequestMethod.POST},value={"/bucketsToNodes"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<Integer, Set<NodeAddress>> addBucketsToNodes(@RequestBody Map<Integer, Set<NodeAddress>> bucketsToNodesToAdd) {
		bucketsToNodes.merge(bucketsToNodesToAdd);
		gossipRegistry.gossipBucketsToNodesMappingAdded(bucketsToNodesToAdd);
		return bucketsToNodes.getBucketsToNodes();
	}
	
	@ApiOperation("Delete the association between bucket and and the node of host and port received")
	@RequestMapping(method={RequestMethod.DELETE},value={"/bucketsToNodes/{bucket}/{node_host}/{node_port}"})
	public void removeBucketsToNodes(@PathVariable("bucket") Integer bucket, 
			@PathVariable("node_host") String host, @PathVariable("node_port") int port) {
		NodeAddress nodeAdress = new NodeAddress(host, port);
		logger.debug("Remove Buckets To Node association from bucket {} and node {}", bucket, nodeAdress);
		bucketsToNodes.remove(bucket, nodeAdress);
	}

}
