package ro.amihai.dht.bucketstonodes;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toConcurrentMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ro.amihai.dht.keyvaluestore.size.BucketSize;
import ro.amihai.dht.node.NodeAddress;

@Component
public class BucketsToNodesTypeConvertor {

	public Map<Integer, Set<NodeAddress>> toBucketsToNodesMap(Map<String, List<Map<String, Object>>> jsonMap) {
		return jsonMap.entrySet().stream()
			.collect(toConcurrentMap(
					entry -> Integer.valueOf(entry.getKey()), 
					entry-> mapToListToSetNodeAddress(entry.getValue())));
	}
	
	public Map<Integer, BucketSize> toBuckesSizeMap(Map<String, Map<String, Object>> jsonMap) {
		return jsonMap.entrySet().stream()
			.collect(toConcurrentMap(
					entry -> Integer.valueOf(entry.getKey()), 
					entry-> maptToBucketSize(entry.getValue())));
	}
	
	private BucketSize maptToBucketSize(Map<String, Object> jsonAsMap) {
		int bucket = parseInt(valueOf(jsonAsMap.get("bucket")));
		long size = parseLong(valueOf(jsonAsMap.get("size")));
		long lastUpdate = parseLong(valueOf(jsonAsMap.get("lastUpdate")));
		return new BucketSize(bucket, size, lastUpdate);
	}
	
	
	private Set<NodeAddress> mapToListToSetNodeAddress(List<Map<String, Object>> nodeAddressesDetails) {
		return nodeAddressesDetails.stream()
				.map(NodeAddress::fromMap)
				.collect(Collectors.toSet());
	}
	
}
