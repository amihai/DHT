package ro.amihai.dht.integrationtests.bucketstonodes;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import ro.amihai.dht.bucketstonodes.BucketsToNodes;
import ro.amihai.dht.bucketstonodes.BucketsToNodesService;
import ro.amihai.dht.integrationtests.SpringIntegrationStepDef;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties= {"server.port=8001"})
public class BucketsToNodesServiceStepsDef extends SpringIntegrationStepDef {

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private BucketsToNodesService bucketsToNodesService;
	
	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	private Map<Integer, Set<NodeAddress>> bucketsToNodesReceived;
	private Map<Integer, Set<NodeAddress>> bucketsToNodesToBeAdded;
	private NodeAddress nodeAddressToBeRemoved;
	
	@Given("^that a Node is started$")
	public void that_a_Node_is_started() throws Throwable {
		assertNotNull("Node is not started", nodeProperties);
	}

	@When("^I call the REST API GET \"([^\"]*)\" of the Node$")
	public void i_call_the_REST_API_GET_of_the_Node(String bucketsToNodesServicePath) throws Throwable {
		bucketsToNodesReceived = restTemplate.getForObject( nodeProperties.getCurrentNodeAddress().getURI(bucketsToNodesServicePath, null), Map.class);
	}

	@Then("^I receive a JSON with the Buckets To Nodes mapping$")
	public void i_receive_a_JSON_with_the_Buckets_To_Nodes_mapping() throws Throwable {
		assertEquals("The number of Buckets from the map is invalid", nodeProperties.getNoOfBuckets(), bucketsToNodesReceived.size());
	}

	@When("^I call the REST API POST \"([^\"]*)\" of the Node with a JSON with new mappings$")
	public void i_call_the_REST_API_POST_of_the_Node_with_a_JSON_with_new_mappings(String addMappingsPath) throws Throwable {
		bucketsToNodesToBeAdded = ThreadLocalRandom.current().ints(0, nodeProperties.getNoOfBuckets())
			.distinct()
			.boxed()
			.limit(10)
			.collect(toMap(identity(), this::dummyNodeAddresses));
		
		ResponseEntity<Map> response = restTemplate.postForEntity(nodeProperties.getCurrentNodeAddress().getURI(addMappingsPath, null), bucketsToNodesToBeAdded, Map.class);
		assertTrue("Adding of new mapping failed", response.getStatusCode().is2xxSuccessful());
		
		bucketsToNodesReceived = response.getBody();
	}

	
	@Then("^the new mappings are added to the Buckets To Node if they are not already there$")
	public void the_new_mappings_are_added_to_the_Buckets_To_Node_if_they_are_not_already_there() throws Throwable {
		bucketsToNodesToBeAdded.entrySet().forEach( entry -> {
			assertTrue("Some of the values were not added to the mapping" , bucketsToNodesService.getBucketsToNodes().get(entry.getKey()).containsAll(entry.getValue()));
		});
	}

	@Then("^the Buckets To Nodes mapping is returned as JSON$")
	public void the_Buckets_To_Nodes_mapping_is_returned_as_JSON() throws Throwable {
		assertEquals("The number of Buckets from the map is invalid", nodeProperties.getNoOfBuckets(), bucketsToNodesReceived.size());
	}
	
	@Given("^the Mapping To Nodes contains in bucket \"([^\"]*)\" Node with host \"([^\"]*)\" and port \"([^\"]*)\"$")
	public void the_Mapping_To_Nodes_contains_in_bucket_Node_with_host_and_port(String bucket, String host, String port) throws Throwable {
		bucketsToNodes.add(Integer.valueOf(bucket) , new NodeAddress(host, Integer.parseInt(port)));
	}

	@When("^I call the REST API DELETE \"([^\"]*)\"$")
	public void i_call_the_REST_API_DELETE(String removeMappingPath) throws Throwable {
		restTemplate.delete((nodeProperties.getCurrentNodeAddress().getURI(removeMappingPath, null)));
	}

	@Then("^the mapping is removed from the Buckets To Nodes$")
	public void the_mappings_are_removed_from_the_Buckets_To_Nodes_if_they_are_there() throws Throwable {
		Assert.assertFalse("The mapping was not removed", bucketsToNodesService.getBucketsToNodes().get(1).contains(nodeAddressToBeRemoved));
	}

	@Then("^the new Buckets To Nodes mapping is stored on disk for recovery$")
	public void the_new_Buckets_To_Nodes_mapping_is_stored_on_disk_for_recovery() throws Throwable {
	    Integer noOfBucketsMappings = (int) Files.list(Paths.get("target", "node_8001/bucketsToNodes")).count();
	    
	    assertEquals("Buckets TO Nodes mapping is not stored on disk", Integer.valueOf(nodeProperties.getNoOfBuckets()), noOfBucketsMappings);
	}

	private Set<NodeAddress> dummyNodeAddresses(int bucket) {
		return ThreadLocalRandom.current().ints(5, 0, 255)
			.mapToObj(this::dummyNodeAddresse)
			.collect(Collectors.toSet());
	}
	private NodeAddress dummyNodeAddresse(int i) {
		NodeAddress nodeAddress = new NodeAddress();
		nodeAddress.setHost("192.168.2." + i);
		nodeAddress.setPort(ThreadLocalRandom.current().nextInt(1026, 65536));
		return nodeAddress;
	}
}
