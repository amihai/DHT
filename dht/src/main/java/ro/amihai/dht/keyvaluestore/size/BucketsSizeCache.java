package ro.amihai.dht.keyvaluestore.size;

import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.util.function.Function.identity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import ro.amihai.dht.bucketstonodes.BucketsToNodesJsonParser;
import ro.amihai.dht.bucketstonodes.BucketsToNodesStatistics;
import ro.amihai.dht.gossip.GossipMemebers;
import ro.amihai.dht.node.NodeAddress;

@Component
public class BucketsSizeCache {

	private Logger logger = LoggerFactory.getLogger(BucketsSizeCache.class);
	
	private Map<Integer, BucketSize> bucketsSize = new HashMap<>();
	
	@Autowired
	private BucketsToNodesStatistics bucketsToNodesStatistics;
	
	@Value("${keyValue.storeDirectory}")
	private Path storeDirectory;
	
	@Autowired
	private BucketsSizeOperations bucketsSizeOperations;
	
	@Autowired
	private GossipMemebers gossipMemebers;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private BucketsToNodesJsonParser bucketsToNodesJsonParser;
	
	@Scheduled(fixedRateString="${bucketsSizeCache.refresh.rate}")
	private void updateBucketsSizeCache() {
		logger.trace("Start to update the size of the buckets");
		updateBucketSizeFromCurrentNode();
		
		updateBucketSizeFromNetwork();
		logger.trace("Done updating the size of the buckets");
	}

	public Map<Integer, BucketSize> getBucketsSize() {
		return bucketsSize;
	}
	
	private void updateBucketSizeFromCurrentNode() {
		logger.debug("Start to update the size of the buckets from the current node");
		Map<Integer, BucketSize> bucketsSizeFromFS = bucketsToNodesStatistics.getBucketsInCurrentNode()
			.stream().map(this::getBucketSizeFromFileSystem)
			.flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::empty))
			.collect(Collectors.toMap(bucketSize -> bucketSize.getBucket(), identity()));
		
		bucketsSize = bucketsSizeOperations.merge(bucketsSizeFromFS, bucketsSize);
		logger.debug("Done updating the size of the buckets from the current node");
	}
	
	private void updateBucketSizeFromNetwork() {
		logger.debug("Start to update the size of the buckets from the network");
		Map<Integer, BucketSize> bucketSizeFromNetwork = gossipMemebers.shuffledGossipMembers()
			.stream()
			.map(this::getBucketsSizeFromNode)
			.flatMap(optional -> optional.map(Stream::of).orElse(Stream.empty())) //Stream of Map<Integer, BucketSize>
			.map(Map::entrySet)
			.flatMap(Set::stream)
			.collect(Collectors.toMap(
						Map.Entry::getKey, 
						Map.Entry::getValue,
						bucketsSizeOperations::latestBucketSize)
					);
		
		bucketsSize = bucketsSizeOperations.merge(bucketSizeFromNetwork, bucketsSize);
		logger.debug("Done updating the size of the buckets from the network");
	}
	
	private Optional<Map<Integer, BucketSize>> getBucketsSizeFromNode(NodeAddress nodeAddress) {
		logger.debug("Start to read the buckets size from node {}", nodeAddress);
		try {
			URI uriGetBucketsSize = nodeAddress.getURI("/buckets/size", null);
			ResponseEntity<Map> response = restTemplate.getForEntity(uriGetBucketsSize, Map.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				Map<String, Map<String, Object>> jsonAsMap = response.getBody();
				return Optional.ofNullable(bucketsToNodesJsonParser.mapToBucketsSize(jsonAsMap));
			}
		} catch (URISyntaxException e) {
			logger.error("Cannot read the buckets size from node {}:", nodeAddress, e);
		}
		return Optional.empty();
	}
	
	private Optional<BucketSize> getBucketSizeFromFileSystem(Integer bucket) {
		Path bucketFilePath = Paths.get(storeDirectory.toString(), valueOf(bucket));
		if (bucketFilePath.toFile().exists()) {
			long bucketSize;
			try {
				bucketSize = Files.list(bucketFilePath).count();
				return Optional.of(new BucketSize(bucket, bucketSize, currentTimeMillis()));
			} catch (IOException e) {
				logger.error("Cannot read bucket size from disk", e);
			}
		}
		return Optional.empty();
	}
}
