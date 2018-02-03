package ro.amihai.dht.bucketstonodes;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import ro.amihai.dht.node.NodeAddress;

@RestController
public class BucketsToNodesService {
	
	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	@ApiOperation("Return the Mapping between Buckets and Nodes")
	@RequestMapping(method={RequestMethod.GET},value={"/bucketsToNodes"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<Integer, Set<NodeAddress>> getBucketsToNodes() {
		return bucketsToNodes.getBucketsToNodes();
	}
	
	@ApiOperation("Merge the internal Mapping with the one received")
	@RequestMapping(method={RequestMethod.POST},value={"/bucketsToNodes"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<Integer, Set<NodeAddress>> addBucketsToNodes(@RequestBody Map<Integer, Set<NodeAddress>> bucketsToNodesToAdd) {
		bucketsToNodes.merge(bucketsToNodesToAdd);
		return bucketsToNodes.getBucketsToNodes();
	}
	
	@ApiOperation("Delete the association between bucket and and the node of host and port received")
	@RequestMapping(method={RequestMethod.DELETE},value={"/bucketsToNodes/{bucket}/{node_host}/{node_port}"})
	public void removeBucketsToNodes(@PathVariable("bucket") Integer bucket, 
			@PathVariable("node_host") String host, @PathVariable("node_port") int port) {
		bucketsToNodes.remove(bucket, new NodeAddress(host, port));
	}

}
