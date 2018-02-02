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

import ro.amihai.dht.node.NodeAddress;

@RestController(value="/")
public class BucketsToNodesService {
	
	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	@RequestMapping(method={RequestMethod.GET},value={"*"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<Integer, Set<NodeAddress>> getBucketsToNodes() {
		return bucketsToNodes.getBucketsToNodes();
	}
	
	@RequestMapping(method={RequestMethod.POST},value={"*"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<Integer, Set<NodeAddress>> addBucketsToNodes(@RequestBody Map<Integer, Set<NodeAddress>> bucketsToNodesToAdd) {
		bucketsToNodes.merge(bucketsToNodesToAdd);
		return bucketsToNodes.getBucketsToNodes();
	}
	
	@RequestMapping(method={RequestMethod.DELETE},value={"*/{bucket}/{node_host}/{node_port}"})
	public void removeBucketsToNodes(@PathVariable("bucket") Integer bucket, 
			@PathVariable("node_host") String host, @PathVariable("node_port") int port) {
		bucketsToNodes.remove(bucket, new NodeAddress(host, port));
	}

}
