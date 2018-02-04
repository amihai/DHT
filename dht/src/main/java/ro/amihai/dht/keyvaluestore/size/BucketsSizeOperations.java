package ro.amihai.dht.keyvaluestore.size;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

@Component
public class BucketsSizeOperations {

	public Map<Integer, BucketSize> merge(Map<Integer, BucketSize> m1, Map<Integer, BucketSize> m2) {
		return Stream.of(
					Optional.ofNullable(m1).map(Stream::of).orElseGet(Stream::empty), 
					Optional.ofNullable(m2).map(Stream::of).orElseGet(Stream::empty)
					)
				.flatMap(Function.identity())
				.map(Map::entrySet)
				.flatMap(Collection::stream)
				.collect(
					Collectors.toMap(
							Map.Entry::getKey, 
							Map.Entry::getValue,
							this::latestBucketSize
							)
					);
	}
	
	public BucketSize latestBucketSize(BucketSize b1, BucketSize b2) {
		return b1.getLastUpdate() > b2.getLastUpdate() ? b1 : b2;
	}
}
