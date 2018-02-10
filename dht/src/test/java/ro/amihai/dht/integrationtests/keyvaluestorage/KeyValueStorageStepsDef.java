package ro.amihai.dht.integrationtests.keyvaluestorage;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import ro.amihai.dht.bucketstonodes.BucketsToNodesStatistics;
import ro.amihai.dht.integrationtests.SpringIntegrationStepDef;
import ro.amihai.dht.keyvaluestore.KeyValue;
import ro.amihai.dht.keyvaluestore.dao.KeyValueDAOFileSystem;
import ro.amihai.dht.node.NodeProperties;
import ro.amihai.dht.tests.util.RandomKeySpliterator;


@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties= {"server.port=8002"})
public class KeyValueStorageStepsDef extends SpringIntegrationStepDef {

	private Logger logger = LoggerFactory.getLogger(KeyValueStorageStepsDef.class);
	
	@Autowired
	private BucketsToNodesStatistics bucketsToNodesStatistics;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${keyValue.storeDirectory}")
	private Path storeDirectory;
	
	@Autowired
	private KeyValueDAOFileSystem keyValueDAOFileSystem;
	
	@Autowired
	private RandomKeySpliterator randomKeySpliterator;
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	private ResponseEntity<KeyValue> getKeyResponse;
	
	@Value("${bucketsSizeCache.refresh.rate}")
	private long bucketsSizeRefreshRate;
	
	@After
	public void clearKeyValueStorage() {
		bucketsToNodesStatistics.getBucketsInCurrentNode().forEach(this::cleanBucket);
	}
	
	@Given("^that the bucket for key \"([^\"]*)\" is stored on current node$")
	public void that_the_bucket_for_key_is_stored_on_current_node(String key) {
		assertTrue(format("The key %s is not stored on current node", key), bucketsToNodesStatistics.isBucketOnCurrentNode(key));
	}
	@When("^I call the PUT API \"([^\"]*)\" with the JSON (\\{[^\\}]*\\})$")
	public void i_call_the_PUT_API_with_the_JSON(String url, String json) throws Throwable {
		URI uriPutJson = nodeProperties.getCurrentNodeAddress().getURI(url, null);
		KeyValue keyValue = objectMapper.readValue(json, KeyValue.class);
		restTemplate.put(uriPutJson, keyValue);
		logger.debug("Key Value {} send to PUT URL {} ", keyValue, url);
	}

	@Then("^I should see the value \"([^\"]*)\" stored on disk under the key \"([^\"]*)\"$")
	public void i_should_see_the_value_stored_on_disk_under_the_key(String expectedValue, String key) throws IOException {
		int bucket = bucketsToNodesStatistics.bucket(key);
		logger.debug("The key {} should be in the bucket {}", key, bucket);
		Path keyFilePath = Paths.get(storeDirectory.toString(), valueOf(bucket), key);
		
		assertTrue(format("The key %s was not found on disk in location %s", key, keyFilePath.toString()), keyFilePath.toFile().exists());
		
		String actualValue = new String(Files.readAllBytes(keyFilePath)).trim();
		
		assertEquals("Value from disk doesn't match", expectedValue, actualValue);
		
	}
	
	@Given("^I already have stored the value \"([^\"]*)\" for the key \"([^\"]*)\" on the current node$")
	public void i_already_have_stored_the_value_for_the_key_on_the_current_node(String value, String key) {
		keyValueDAOFileSystem.saveOrUpdate(new KeyValue(key, value));
	}

	@When("^I call the GET API \"([^\"]*)\"$")
	public void i_call_the_GET_API(String url) throws URISyntaxException {
		URI uriGetJson = nodeProperties.getCurrentNodeAddress().getURI(url, null);
		getKeyResponse = restTemplate.getForEntity(uriGetJson, KeyValue.class);
	}

	@Then("^I should receive the KeyValue pair (\\{[^\\}]*\\})$")
	public void i_should_receive_the_KeyValue_pair(String expectedKeyValue) throws JsonParseException, JsonMappingException, IOException {
		assertTrue("Invalid response from Node when try to get the key value ", getKeyResponse.getStatusCode().is2xxSuccessful()); 
		
		KeyValue expected = objectMapper.readValue(expectedKeyValue, KeyValue.class);
		assertEquals("Cannot read KeyValue with REST API", expected, getKeyResponse.getBody());
	}
	
	@Given("^I don't have the key \"([^\"]*)\" stored on current node$")
	public void i_don_t_have_the_key_stored_on_current_node(String key) {
		keyValueDAOFileSystem.delete(key);
	}
	
	@When("^I call the GET API \"([^\"]*)\" I should receive the (\\d+) HTTP status code$")
	public void i_call_the_GET_API_I_should_receive_the_HTTP_status_code(String url, int statusCode) throws URISyntaxException {
		URI uriGetJson = nodeProperties.getCurrentNodeAddress().getURI(url, null);
		HttpStatus actualHttpStatus = null;
		try {
			getKeyResponse = restTemplate.getForEntity(uriGetJson, KeyValue.class);
			actualHttpStatus = getKeyResponse.getStatusCode();
		} catch (HttpClientErrorException ex) {
			actualHttpStatus = ex.getStatusCode();
		}
		assertEquals("Invalid status code if key doesn't exists", HttpStatus.valueOf(statusCode), actualHttpStatus);
	}
	
	@When("^I call the GET DELETE \"([^\"]*)\"$")
	public void i_call_the_GET_DELETE(String url) throws Throwable {
		URI uriDeleteJson = nodeProperties.getCurrentNodeAddress().getURI(url, null);
		restTemplate.delete(uriDeleteJson);
	}

	@Then("^the key \"([^\"]*)\" is no longer stored on disk$")
	public void the_key_is_no_longer_stored_on_disk(String key) throws Throwable {
		Optional<KeyValue> load = keyValueDAOFileSystem.load(key);
		assertFalse("The key was not remove from disk", load.isPresent());
	}
	
	@Given("^that (\\d+) distinct pairs of key value are stored on disk$")
	public void that_distinct_pairs_of_key_value_are_stored_on_disk(int size) throws InterruptedException {
		StreamSupport.stream(randomKeySpliterator, false)
			.limit(size)
			.forEach(key -> keyValueDAOFileSystem.saveOrUpdate(new KeyValue(key, key)));
		
		TimeUnit.MILLISECONDS.sleep(2 * bucketsSizeRefreshRate);
	}

	@When("^I call the GET API \"([^\"]*)\" I should receive the value (\\d+)$")
	public void i_call_the_GET_API_I_should_receive_the_value(String url, int expectedSize) throws URISyntaxException {
		URI uriGetJson = nodeProperties.getCurrentNodeAddress().getURI(url, null);
		ResponseEntity<Integer> response = restTemplate.getForEntity(uriGetJson, Integer.class);
		Assert.assertEquals("The size is not correct", Integer.valueOf(expectedSize), response.getBody()); 
	}
	
	private void cleanBucket(int bucket) {
		Path bucketPath = Paths.get(storeDirectory.toString(), valueOf(bucket));
		if (bucketPath.toFile().exists()) {
			try {
				Files.list(bucketPath).forEach(this::deleteIfExists);
			} catch (IOException e) {
				logger.error("Cannot list path {}", bucketPath, e);
			} 
		}
	}
	
	private void deleteIfExists(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			logger.error("Cannot remove path {}", path, e);
		}
	}
}
