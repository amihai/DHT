package ro.amihai.dht.keyvaluestore.dao;

import java.util.Optional;

import ro.amihai.dht.service.keyvaluestore.KeyValue;

public interface KeyValueDao {

	public boolean saveOrUpdate(KeyValue keyValue);
	
	public Optional<KeyValue> load(String key);
	
	public Optional<KeyValue> delete(String key);
}
