package ro.amihai.dht.node.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ro.amihai.dht.bucketstonodes.BucketsToNodesStatistics;

/**
 * This REST service will be used by the internal topology balancing mechanism
 *
 */
@RestController("/node")
public class NodeAdmin {

	@Autowired
	private BucketsToNodesStatistics bucketsToNodesStatistics;
	
	@RequestMapping(method={RequestMethod.GET},value={"/buckets"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Set<Integer> getBuckets() {
		return bucketsToNodesStatistics.getBucketsInCurrentNode();
	}
}
