package ro.amihai.dht.service.keyvaluestore;

import static org.springframework.http.ResponseEntity.accepted;

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

import io.swagger.annotations.ApiOperation;
import ro.amihai.dht.keyvaluestore.size.BucketsSizeCache;

@RestController()
public class KeyValueStoreService {

	@Autowired
	private KeyValueStorage keyValueStorage;
	
	@Autowired
	private BucketsSizeCache bucketsSizeCache;
	
	@ApiOperation("Add a new Key Value to the Storage")
	@RequestMapping(method={RequestMethod.PUT},value={"/keyValue"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<KeyValue> put(@Valid @RequestBody KeyValue keyValue) {
		if (keyValueStorage.store(keyValue)) {
			return ResponseEntity.ok(keyValue);
		} else {
			return ResponseEntity.unprocessableEntity().body(keyValue);
		}
	}
	
	@ApiOperation("Add a Key Value Pair from the Storage")
	@RequestMapping(method={RequestMethod.GET},value={"/keyValue/{key}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<KeyValue> get(@PathVariable("key") String key) {
		Optional<KeyValue> keyValue = keyValueStorage.load(key);
		return keyValue
				.map(ResponseEntity::ok)
				.orElseGet(accepted()::build);
	}
	
	@ApiOperation("Return the number of uniques Pairs in the Storage")
	@RequestMapping(method={RequestMethod.GET},value={"/keyValue/size"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Long> getSize() {
		return ResponseEntity.ok(bucketsSizeCache.getBucketsSize().entrySet()
				.stream().mapToLong(entry -> entry.getValue().getSize())
				.sum());
	}
	
	@ApiOperation("Delete a Pair from Storage")
	@RequestMapping(method={RequestMethod.DELETE},value={"/keyValue/{key}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<KeyValue> delete(@PathVariable("key") String key) {
		Optional<KeyValue> keyValue = keyValueStorage.delete(key);
		return keyValue
				.map(ResponseEntity::ok)
				.orElseGet(accepted()::build);
	}
}
