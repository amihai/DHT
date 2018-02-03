package ro.amihai.dht.keyvaluestore.dao;

import java.util.Optional;

import org.springframework.stereotype.Component;

import ro.amihai.dht.keyvaluestore.KeyValue;

@Component
public class KeyValueDaoNetwork implements KeyValueDao {

	@Override
	public boolean saveOrUpdate(KeyValue keyValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<KeyValue> load(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<KeyValue> delete(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}
