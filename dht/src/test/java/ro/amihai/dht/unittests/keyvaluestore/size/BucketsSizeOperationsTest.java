package ro.amihai.dht.unittests.keyvaluestore.size;

import static java.util.function.Function.identity;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import ro.amihai.dht.keyvaluestore.size.BucketSize;
import ro.amihai.dht.keyvaluestore.size.BucketsSizeOperations;

public class BucketsSizeOperationsTest {

	@Test
	public void testMergeValidMaps() {
		Map<Integer, BucketSize> map1 = Stream.of(new BucketSize(1, 20, 1), new BucketSize(2, 30, 10), new BucketSize(3, 20, 2))
			.collect(Collectors.toMap(bs -> bs.getBucket(), identity()));
		
		Map<Integer, BucketSize> map2 = Stream.of(new BucketSize(1, 30, 10), new BucketSize(2, 20, 1), new BucketSize(3, 30, 20))
				.collect(Collectors.toMap(bs -> bs.getBucket(), identity()));

		Map<Integer, BucketSize> expected = Stream.of(new BucketSize(1, 30, 10), new BucketSize(2, 30, 10), new BucketSize(3, 30, 20))
				.collect(Collectors.toMap(bs -> bs.getBucket(), identity()));
		
		Map<Integer, BucketSize> actual = new BucketsSizeOperations().merge(map1, map2);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testMergeNullMaps() {
		Map<Integer, BucketSize> map1 = Stream.of(new BucketSize(1, 20, 1), new BucketSize(2, 30, 10), new BucketSize(3, 20, 2))
				.collect(Collectors.toMap(bs -> bs.getBucket(), identity()));
		
		Assert.assertEquals(map1, new BucketsSizeOperations().merge(map1, null));
		
		Assert.assertEquals(map1, new BucketsSizeOperations().merge(null, map1));
	}
	
}
