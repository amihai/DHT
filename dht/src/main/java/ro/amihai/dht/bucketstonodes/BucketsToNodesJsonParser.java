package ro.amihai.dht.bucketstonodes;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.nio.file.Files.readAllBytes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ro.amihai.dht.keyvaluestore.size.BucketSize;
import ro.amihai.dht.node.NodeAddress;

@Component
public class BucketsToNodesJsonParser {

	private Logger logger = LoggerFactory.getLogger(BucketsToNodesJsonParser.class);
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	
	public Map<Integer, Set<NodeAddress>> fromJson(Map<String, List<Map<String, String>>> jsonMap) {
		return jsonMap.entrySet().stream()
			.collect(Collectors.toConcurrentMap(
					entry -> Integer.valueOf(entry.getKey()), 
					entry-> mapToListToSetNodeAddress(entry.getValue())));
	}
	
	public Map<Integer, BucketSize> fromBuckesSizeJson(Map<String, Map<String, Object>> jsonMap) {
		return jsonMap.entrySet().stream()
			.collect(Collectors.toConcurrentMap(
					entry -> Integer.valueOf(entry.getKey()), 
					entry-> maptToBucketSize(entry.getValue())));
	}
	
	private Set<NodeAddress> mapToListToSetNodeAddress(List<Map<String, String>> nodeAddressesDetails) {
		return nodeAddressesDetails.stream()
				.map(this::fromJsonMapDetailsToNodeAdress)
				.collect(Collectors.toSet());
	}
	private Set<NodeAddress> mapToSetNodeAddress(String json) {
		try {
			Set<Map> readValue = objectMapper.readValue(json, Set.class);
			return readValue.stream()
					.map(this::fromJsonMapDetailsToNodeAdress)
					.collect(Collectors.toSet());
		} catch (IOException e) {
			logger.error("Error converting Set of NodeAdress Json into Object", e);
			return Collections.emptySet();
		}
	}
	
	public Map<Integer, BucketSize> mapToBucketsSize(Map<String, Map<String, Object>> jsonMap) {
		return jsonMap.entrySet().stream()
				.collect(Collectors.toMap(
						entry -> Integer.valueOf(entry.getKey()), 
						entry-> maptToBucketSize(entry.getValue())));
	}
	
	public BucketSize maptToBucketSize(Map<String, Object> jsonAsMap) {
		int bucket = Integer.parseInt(valueOf(jsonAsMap.get("bucket")));
		long size = Long.parseLong(valueOf(jsonAsMap.get("size")));
		long lastUpdate = Long.parseLong(valueOf(jsonAsMap.get("lastUpdate")));
		return new BucketSize(bucket, size, lastUpdate);
	}
	
	public boolean writeNodeAddressesToFile(File bucketFile, Set<NodeAddress> nodeAddresses) {
		try {
			objectMapper.writeValue(bucketFile, nodeAddresses);
		} catch (IOException e) {
			logger.error("Error writing NodeAdresses to file", e);
			return false;
		}
		return true;
	}
	
	public Set<NodeAddress> readNodeAddressesFromFile(Path bucketFile) {
		try {
			String json = new String(readAllBytes(bucketFile));
			return mapToSetNodeAddress(json);
		} catch (IOException e) {
			logger.error("Error reading NodeAdresses from file", e);
			return Collections.emptySet();
		}
	}
	
	private NodeAddress fromJsonMapDetailsToNodeAdress(Map nodeDetails) {
		String host = valueOf(nodeDetails.get("host"));
		int port = parseInt(valueOf(nodeDetails.get("port")));
		return new NodeAddress(host, port);
	}
	
}
