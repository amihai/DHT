package ro.amihai.dht.unittests.bucketstonodes;

import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import ro.amihai.dht.bucketstonodes.BucketsToNodesTypeConvertor;
import ro.amihai.dht.keyvaluestore.size.BucketSize;
import ro.amihai.dht.node.NodeAddress;

public class BucketsToNodesTypeConvertorTest {

	
	private static final int NO_OF_BUCKETS = 64;
	private static final int MAX_PORT = 49151;
	private static final int MIN_PORT = 1024;
	
	@Test
	public void testToBuckesSizeMap() {
		Map<String, Map<String, Object>> jsonMap = expectedBucketsSizeJson();
		Map<Integer, BucketSize> actualBucketsSize = new BucketsToNodesTypeConvertor().toBuckesSizeMap(jsonMap);
		
		assertEquals("The number of BucketsSize after convertion is incorrect", jsonMap.entrySet().size(), actualBucketsSize.size());
		
		actualBucketsSize.forEach(this::checkBucketSize);
	}
	
	@Test
	public void testToBucketsToNodesMap() {
		Map<String, List<Map<String, Object>>> jsonMap = expectedBucketsToNodesJson();
		Map<Integer, Set<NodeAddress>> bucketsToNodesMap = new BucketsToNodesTypeConvertor().toBucketsToNodesMap(jsonMap);
		
		assertEquals("The number of buckets after convertion is incorrect", jsonMap.entrySet().size(), bucketsToNodesMap.size());
		
		jsonMap.entrySet()
			.forEach(entry -> compareValues(entry.getValue(), bucketsToNodesMap.get(Integer.valueOf(entry.getKey()))));
	}
	
	
	private void checkBucketSize(Integer bucket, BucketSize bucketSize) {
		int expectedBucket = bucket.intValue();
		long expectedSize = dummySize(expectedBucket);
		long expectedLastUpdate = dummyLastUpdate(expectedBucket);
		Assert.assertEquals("Bucket Id is not set properly", expectedBucket, bucketSize.getBucket());
		Assert.assertEquals("Bucket Size is not set properly", expectedSize, bucketSize.getSize());
		Assert.assertEquals("Bucket LastUpdated is not set properly", expectedLastUpdate, bucketSize.getLastUpdate());
	}
	
	private Map<String, Map<String, Object>> expectedBucketsSizeJson() {
		return IntStream.rangeClosed(1, NO_OF_BUCKETS)
				.boxed()
				.collect(Collectors.toMap(
						String::valueOf, 
						this::bucketSizeFromBucket));
	}
	
	private Map<String, Object> bucketSizeFromBucket(Integer bucket) {
		return Stream.of(
					new AbstractMap.SimpleEntry<>("bucket", bucket),
					new AbstractMap.SimpleEntry<>("size", dummySize(bucket)),
					new AbstractMap.SimpleEntry<>("lastUpdate", dummyLastUpdate(bucket))
				).collect(Collectors.toMap(
						Map.Entry::getKey, 
						Map.Entry::getValue));
	}
	
	private void compareValues(List<Map<String, Object>> jsonValue, Set<NodeAddress> objectValue) {
		assertNotNull("Cannot find any list of nodes for bucket", objectValue);
		Set<NodeAddress> expectedObjects = jsonValue.stream()
			.map(NodeAddress::fromMap)
			.collect(Collectors.toSet());
		assertEquals("The Set of objects after convertion are not equals", expectedObjects, objectValue); 
	}
	
	private Map<String, List<Map<String, Object>>> expectedBucketsToNodesJson() {
		return IntStream.rangeClosed(1, NO_OF_BUCKETS)
			.boxed()
			.collect(Collectors.toMap(String::valueOf, this::listOfNodes));
	}
	
	private List<Map<String, Object>> listOfNodes(int noOfNodes) {
		return ThreadLocalRandom.current().ints(noOfNodes, MIN_PORT, MAX_PORT)
			.boxed()
			.map(this::nodeAddressFromPort)
			.collect(Collectors.toList());
	}
	
	private Map<String, Object> nodeAddressFromPort(Integer port) {
		return nodeAddressAsMap("localhost", port);
	}
	
	private Map<String, Object> nodeAddressAsMap(String host, Integer port) {
		return Stream.of(
					new AbstractMap.SimpleEntry<>("host", host),
					new AbstractMap.SimpleEntry<>("port", port))
				.collect(
						toMap(
								Map.Entry::getKey, 
								Map.Entry::getValue));
	}
	
	private long dummySize(int bucket) {
		return 20 * bucket;
	}
	
	private long dummyLastUpdate(int bucket) {
		return 1000 * bucket;
	}
}
