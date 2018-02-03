package ro.amihai.dht.keyvaluestore.size;

import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.util.function.Function.identity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.BucketsToNodesStatistics;

@Component
public class BucketsSizeCache {

	private Logger logger = LoggerFactory.getLogger(BucketsSizeCache.class);
	
	private Map<Integer, BucketSize> bucketSize = new ConcurrentHashMap<>();
	
	@Autowired
	private BucketsToNodesStatistics bucketsToNodesStatistics;
	
	@Value("${keyValue.storeDirectory}")
	private Path storeDirectory;
	
	@Scheduled(fixedRateString="${bucketsSizeCache.refresh.rate}")
	private void updateBucketsSizeCache() {
		updateBucketSizeFromCurrentNode();
		
		updateBucketSizeFromNetwork();
	}

	public Map<Integer, BucketSize> getBucketSize() {
		return bucketSize;
	}
	
	public void updateBucketSizeFromCurrentNode() {
		Map<Integer, BucketSize> bucketsSizeFromFS = bucketsToNodesStatistics.getBucketsInCurrentNode()
			.stream().map(this::getBucketSizeFromFileSystem)
			.flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::empty))
			.collect(Collectors.toMap(bucketSize -> bucketSize.getBucket(), identity()));
		
		mergeBucketSize(bucketsSizeFromFS);
	}
	
	public void updateBucketSizeFromNetwork() {
		//TODO call external
	}
	
	private void mergeBucketSize(Map<Integer, BucketSize> bucketsSizeToBeMerged) {
		bucketSize = Stream.of(bucketSize, bucketsSizeToBeMerged)
			.map(Map::entrySet)
			.flatMap(Collection::stream)
			.collect(
					Collectors.toConcurrentMap(
							Map.Entry::getKey, 
							Map.Entry::getValue,
							this::latestBucketSize
							)
					);
		
	}
	
	private BucketSize latestBucketSize(BucketSize b1, BucketSize b2) {
		return b1.getLastUpdate() > b2.getLastUpdate() ? b1 : b2;
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
