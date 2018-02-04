package ro.amihai.dht.bucketstonodes.dao;

import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.list;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toConcurrentMap;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.BucketsToNodesJsonParser;
import ro.amihai.dht.bucketstonodes.BucketsToNodesStatistics;
import ro.amihai.dht.node.NodeAddress;

@Component
public class BucketsToNodesDAOFileSystem implements BucketsToNodesDAO {
	
	private Logger logger = LoggerFactory.getLogger(BucketsToNodesDAOFileSystem.class);
	
	
	@Value("${bucketsToNodes.storeDirectory}")
	private Path storeDirectory;
	
	@Autowired
	private BucketsToNodesStatistics bucketsToNodesStatistics;
	
	@Autowired
	private BucketsToNodesJsonParser bucketsToNodesJsonParser;
	
	private String BUCKET_FILE_NAME = "%d";
	
	@PostConstruct
	public void init() throws IOException {
		createDirectories(storeDirectory);
	}
	
	@Override
	public void saveOrUpdate(Map<Integer, Set<NodeAddress>> bucketsToNodes) {
		bucketsToNodes.entrySet().forEach(this::storeBucketOnDisk);
		bucketsToNodesStatistics.updateStatistics();
	}
	
	@Override
	public void saveOrUpdate(Integer bucket, Set<NodeAddress> nodeAddreses) {
		storeBucketOnDisk(bucket, nodeAddreses);
		bucketsToNodesStatistics.updateStatistics();
	}
	
	@Override
	public Optional<Map<Integer, Set<NodeAddress>>> load() {
		try {
			Map<Integer, Set<NodeAddress>> bucketsToNodes = 
					list(storeDirectory)
						.collect( 
								toConcurrentMap(
										this::bucketFromFileName, 
										bucketsToNodesJsonParser::readNodeAddressesFromFile)
								);
			return ofNullable(bucketsToNodes.isEmpty() ? null : bucketsToNodes);
		} catch (IOException e) {
			logger.error("Cannot load from Disk the Buckets TO Nodes mapping");
			return Optional.empty();
		}
	}
	
	private Integer bucketFromFileName(Path bucketPath) {
		return Integer.parseInt(bucketPath.getFileName().toString());
	}
	
	private boolean storeBucketOnDisk(Map.Entry<Integer, Set<NodeAddress>> entry) {
		return storeBucketOnDisk(entry.getKey(), entry.getValue());
	}
	
	private boolean storeBucketOnDisk(Integer bucket, Set<NodeAddress> nodeAddresses) {
		Path bucketFilePath = Paths.get(storeDirectory.toString(), format(BUCKET_FILE_NAME, bucket));
		return bucketsToNodesJsonParser.writeNodeAddressesToFile(bucketFilePath.toFile(), nodeAddresses);
	}

}
