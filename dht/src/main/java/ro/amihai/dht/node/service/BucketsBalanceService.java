package ro.amihai.dht.node.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import ro.amihai.dht.bucketstonodes.observer.BucketsInCurrentNode;
import ro.amihai.dht.service.keyvaluestore.KeyValue;
import ro.amihai.dht.service.keyvaluestore.KeyValueStorage;

/**
 * This REST service will be used by the internal topology balancing mechanism
 *
 */
@RestController("/bucketsBalance")
public class BucketsBalanceService {

	@Autowired
	private BucketsInCurrentNode bucketsInCurrentNode;
	
	@Autowired
	private KeyValueStorage keyValueStorage;
	
	@ApiOperation("The list of all the buckets stored on current node")
	@RequestMapping(method={RequestMethod.GET},value={"/buckets"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Set<Integer> getBuckets() {
		return bucketsInCurrentNode.getBucketsInCurrentNode();
	}
	
	@ApiOperation("Return the full content of a bucket")
	@RequestMapping(method={RequestMethod.GET},value={"/buckets/{bucket}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<List<KeyValue>> getBucket(@PathVariable("bucket") int bucket) {
		Optional<List<KeyValue>> fullBucket = keyValueStorage.loadBucket(bucket);
		return fullBucket
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.badRequest().body(null));
	}
	
}
