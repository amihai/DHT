package ro.amihai.dht.bucketstonodes;

import static java.nio.file.Files.readAllBytes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ro.amihai.dht.node.NodeAddress;

@Component
public class BucketsToNodesMarshaller {

	private Logger logger = LoggerFactory.getLogger(BucketsToNodesMarshaller.class);
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public Set<NodeAddress> readNodeAddressesFromFile(Path bucketFile) {
		try {
			String json = new String(readAllBytes(bucketFile));
			return mapToSetNodeAddress(json);
		} catch (IOException e) {
			logger.error("Error reading NodeAdresses from file", e);
			return Collections.emptySet();
		}
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
	
	private Set<NodeAddress> mapToSetNodeAddress(String json) {
		try {
			Set<Map<String, Object>> readValue = objectMapper.readValue(json, Set.class);
			return readValue.stream()
					.map(NodeAddress::fromMap)
					.collect(Collectors.toSet());
		} catch (IOException e) {
			logger.error("Error converting Set of NodeAdress Json into Object", e);
			return Collections.emptySet();
		}
	}
	
}
