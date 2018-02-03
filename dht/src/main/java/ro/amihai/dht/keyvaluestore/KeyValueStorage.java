package ro.amihai.dht.keyvaluestore;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.BucketsToNodesStatistics;
import ro.amihai.dht.keyvaluestore.dao.KeyValueDAOFileSystem;
import ro.amihai.dht.keyvaluestore.dao.KeyValueDaoNetwork;

@Component
public class KeyValueStorage {
	
	private Logger logger = LoggerFactory.getLogger(KeyValueStorage.class);
	
	@Autowired
	private BucketsToNodesStatistics bucketsToNodesStatistics;
	
	@Autowired
	private KeyValueDAOFileSystem keyValueDAOFileSystem;
	
	@Autowired
	private KeyValueDaoNetwork keyValueDAONetwork;
	
	@Value("${keyValue.storeDirectory}")
	private Path storeDirectory;
	
	public boolean store(KeyValue keyValue) {
		if (bucketsToNodesStatistics.isBucketOnCurrentNode(keyValue.getKey())) {
			logger.debug("Store key {} on current node", keyValue.getKey());
			return keyValueDAOFileSystem.saveOrUpdate(keyValue);
		} else {
			logger.debug("Store key {} on the network", keyValue.getKey());
			return keyValueDAONetwork.saveOrUpdate(keyValue);
		}
	}
	
	public Optional<KeyValue> load(String key) {
		if (bucketsToNodesStatistics.isBucketOnCurrentNode(key)) {
			logger.debug("Load key {} from the current node", key);
			return keyValueDAOFileSystem.load(key);
		} else {
			logger.debug("Load key {} from the network", key);
			return keyValueDAONetwork.load(key);
		}
	}
	
	public Optional<KeyValue> delete(String key) {
		if (bucketsToNodesStatistics.isBucketOnCurrentNode(key)) {
			logger.debug("Delete key {} from the current node", key);
			return keyValueDAOFileSystem.delete(key);
		} else {
			logger.debug("Delete key {} from the network", key);
			return keyValueDAONetwork.delete(key);
		}
	}
	
	public Optional<List<KeyValue>> loadBucket(int bucket) {
		Optional<List<KeyValue>> fullBucket = Optional.empty();
		if(bucketsToNodesStatistics.isBucketOnCurrentNode(bucket)) {
			Path bucketFilePath = Paths.get(storeDirectory.toString(), valueOf(bucket));
			fullBucket = Optional.of(emptyList());
			if (bucketFilePath.toFile().exists()) {
				try {
					List<KeyValue> keyValues = Files.list(bucketFilePath)
							.map(path -> path.getFileName().toString())
							.map(keyValueDAOFileSystem::load)
							.flatMap(this::optionalToStream)
							.collect(toList());
					fullBucket = Optional.of(keyValues);
				} catch (IOException e) {
					logger.error("Cannot load bucket from disk", bucket);
					fullBucket = Optional.empty();
				}
			} 
		} else {
			logger.error("Bucket {} is not stored on current node", bucket);
		}
		return fullBucket;
	}
	
	private Stream<KeyValue> optionalToStream(Optional<KeyValue> optional) {
		return optional.map(Stream::of).orElseGet(Stream::empty);
	}
}
