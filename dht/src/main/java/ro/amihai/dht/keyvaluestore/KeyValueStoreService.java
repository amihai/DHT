package ro.amihai.dht.keyvaluestore;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ro.amihai.dht.keyvaluestore.size.BucketsSizeCache;

@RestController()
public class KeyValueStoreService {

	@Autowired
	private KeyValueStorage keyValueStorage;
	
	@Autowired
	private BucketsSizeCache bucketsSizeCache;
	
	@RequestMapping(method={RequestMethod.PUT},value={"/keyValue"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<KeyValue> put(@Valid @RequestBody KeyValue keyValue) {
		if (keyValueStorage.store(keyValue)) {
			return ResponseEntity.ok(keyValue);
		} else {
			return ResponseEntity.unprocessableEntity().body(keyValue);
		}
	}
	
	@RequestMapping(method={RequestMethod.GET},value={"/keyValue/{key}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<KeyValue> get(@PathVariable("key") String key) {
		Optional<KeyValue> keyValue = keyValueStorage.load(key);
		return keyValue
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.unprocessableEntity().body(null));
	}
	
	@RequestMapping(method={RequestMethod.GET},value={"/keyValue/size"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Long> getSize() {
		return ResponseEntity.ok(bucketsSizeCache.getBucketSize().entrySet()
				.stream().mapToLong(entry -> entry.getValue().getSize())
				.sum());
	}
	
	@RequestMapping(method={RequestMethod.DELETE},value={"/keyValue/{key}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<KeyValue> delete(@PathVariable("key") String key) {
		Optional<KeyValue> keyValue = keyValueStorage.delete(key);
		return keyValue
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.unprocessableEntity().body(null));
	}
}
