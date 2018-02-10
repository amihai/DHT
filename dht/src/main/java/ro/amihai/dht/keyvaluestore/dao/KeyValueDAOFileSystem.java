package ro.amihai.dht.keyvaluestore.dao;

import static java.lang.String.valueOf;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.observer.BucketsInCurrentNode;
import ro.amihai.dht.service.keyvaluestore.KeyValue;

@Component
public class KeyValueDAOFileSystem implements KeyValueDao {

	private Logger logger = LoggerFactory.getLogger(KeyValueDAOFileSystem.class);
	
	@Value("${keyValue.storeDirectory}")
	private Path storeDirectory;
	
	@Autowired
	private BucketsInCurrentNode bucketsInCurrentNode;
	
	@PostConstruct
	public void init() throws IOException {
		createDirectories(storeDirectory);
	}
	
	@Override
	public boolean saveOrUpdate(KeyValue keyValue) {
		String key = keyValue.getKey();
		int bucket = bucketsInCurrentNode.bucket(key);

		logger.debug("Save key {} in bucket {}", key, bucket);
		
		Path bucketFilePath = Paths.get(storeDirectory.toString(), valueOf(bucket));
		try {
			createDirectories(bucketFilePath);
			Path keyFilePath = Paths.get(bucketFilePath.toString(), key);
			Files.write(keyFilePath, Arrays.asList(keyValue.getValue()), CREATE, TRUNCATE_EXISTING);
		} catch (IOException e) {
			logger.error("Cannot save keyValue on disk:", e);
			return false;
		}
		
		return true;
	}

	@Override
	public Optional<KeyValue> load(String key) {
		int bucket = bucketsInCurrentNode.bucket(key);
		
		logger.debug("Load key {} from bucket {}", key, bucket);
		
		Path bucketFilePath = Paths.get(storeDirectory.toString(), valueOf(bucket));
		if (bucketFilePath.toFile().exists()) {
			Path keyFilePath = Paths.get(bucketFilePath.toString(), key);
			if (keyFilePath.toFile().exists()) {
				try {
					String value = new String(Files.readAllBytes(keyFilePath)).trim();
					return Optional.of(new KeyValue(key, value));
				} catch (IOException e) {
					logger.error("Cannot read value from disk:", e);
				}
			}
		}
		
		return Optional.empty();
	}

	@Override
	public Optional<KeyValue> delete(String key) {
		int bucket = bucketsInCurrentNode.bucket(key);
		
		logger.debug("Delete key {} from bucket {}", key, bucket);
		
		Path bucketFilePath = Paths.get(storeDirectory.toString(), valueOf(bucket));
		if (bucketFilePath.toFile().exists()) {
			Path keyFilePath = Paths.get(bucketFilePath.toString(), key);
			if (keyFilePath.toFile().exists()) {
				try {
					String value = new String(Files.readAllBytes(keyFilePath));
					deleteIfExists(keyFilePath);
					return Optional.of(new KeyValue(key, value));
				} catch (IOException e) {
					logger.error("Cannot read value from disk:", e);
				}
			} else {
				logger.info("Key {} does not exists", key);
			}
		}
		
		return Optional.empty();
	}

}
