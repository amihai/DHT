package ro.amihai.dht.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import ro.amihai.dht.gossip.GossipRegistry;

@RestController
public class HealthService {

	@Autowired
	private NodeHealth nodeHealth;
	
	@Autowired
	private GossipRegistry gossipRegistry;
	
	@ApiOperation("Return the Health details of the current Node (status, number of buckets, etc")
	@RequestMapping(method={RequestMethod.GET},value={"/health"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public NodeHealth getNodeHealth() {
		nodeHealth.setGossipSize(gossipRegistry.getGossipToDo().size());
		return nodeHealth;
	}
}
