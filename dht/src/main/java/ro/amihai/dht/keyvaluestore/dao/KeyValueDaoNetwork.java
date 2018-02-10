package ro.amihai.dht.keyvaluestore.dao;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import ro.amihai.dht.bucketstonodes.observer.BucketsInCurrentNode;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.service.keyvaluestore.KeyValue;

@Component
public class KeyValueDaoNetwork implements KeyValueDao {

	private Logger logger = LoggerFactory.getLogger(KeyValueDaoNetwork.class);
	
	@Autowired
	private BucketsInCurrentNode bucketsInCurrentNode;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Override
	public boolean saveOrUpdate(KeyValue keyValue) {
		logger.debug("Save keyValue {} on the network", keyValue);
		Optional<NodeAddress> externalNodeAddressForKey = bucketsInCurrentNode.externalNodeAddressForKey(keyValue.getKey());
		if (externalNodeAddressForKey.isPresent()) {
			NodeAddress nodeAddress = externalNodeAddressForKey.get();
			logger.debug("Save keyValue {} on the node {}", keyValue, nodeAddress);
			try {
				URI uriPutKeyValue = nodeAddress.getURI("/keyValue", null);
				restTemplate.put(uriPutKeyValue, keyValue);
				return true;
			} catch (URISyntaxException e) {
				logger.error("Cannot save keyValue {} on node {}", keyValue, nodeAddress, e);
			}
		} else {
			logger.error("Cannot find any node that store the keyValue {}", keyValue);
		}
		return false;
	}

	@Override
	public Optional<KeyValue> load(String key) {
		logger.debug("Load the Key {} from the network", key);
		Optional<NodeAddress> externalNodeAddressForKey = bucketsInCurrentNode.externalNodeAddressForKey(key);
		if (externalNodeAddressForKey.isPresent()) {
			NodeAddress nodeAddress = externalNodeAddressForKey.get();
			logger.debug("Load the key {} from the node {}", key, nodeAddress);
			try {
				URI uriGetKeyValue = nodeAddress.getURI("/keyValue/" + key, null);
				ResponseEntity<KeyValue> response = restTemplate.getForEntity(uriGetKeyValue, KeyValue.class);
				if (response.getStatusCode().is2xxSuccessful()) {
					return Optional.of(response.getBody());
				} else {
					logger.error("Cannot find key {} on node {}", key, nodeAddress);
				}
			} catch (URISyntaxException e) {
				logger.error("Cannot load key {} from node {}", key, nodeAddress, e);
			}
		} else {
			logger.error("Cannot find any node that store the keyValue {}", key);
		}
		return Optional.empty();
	}
	
	
	
	@Override
	public Optional<KeyValue> delete(String key) {
		logger.debug("Delete the Key {} from the network", key);
		Optional<NodeAddress> externalNodeAddressForKey = bucketsInCurrentNode.externalNodeAddressForKey(key);
		if (externalNodeAddressForKey.isPresent()) {
			NodeAddress nodeAddress = externalNodeAddressForKey.get();
			logger.debug("Delete the key {} from the node {}", key, nodeAddress);
			try {
				URI uriGetKeyValue = nodeAddress.getURI("/keyValue/" + key, null);
				restTemplate.delete(uriGetKeyValue);
				return Optional.of(new KeyValue(key, null));
			} catch (URISyntaxException e) {
				logger.error("Cannot load key {} from node {}", key, nodeAddress, e);
			}
		} else {
			logger.error("Cannot find any node that store the keyValue {}", key);
		}
		return Optional.empty();
	}

}
