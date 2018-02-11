package ro.amihai.dht.integrationtests.balancer;

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.fasterxml.jackson.core.JsonProcessingException;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import ro.amihai.dht.bucketstonodes.BucketsToNodes;
import ro.amihai.dht.bucketstonodes.observer.AllNodes;
import ro.amihai.dht.bucketstonodes.observer.BucketsInCurrentNode;
import ro.amihai.dht.bucketstonodes.observer.NodesToBuckets;
import ro.amihai.dht.integrationtests.SpringIntegrationStepDef;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;
import ro.amihai.dht.node.balancer.BucketsBalancer;
import ro.amihai.dht.tests.util.MockNodeOperations;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties= {"server.port=8004"})
public class BucketsBalancerStepsDef extends SpringIntegrationStepDef {

	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	@Autowired
	private BucketsInCurrentNode bucketsInCurrentNode;
	
	@Autowired
	private NodesToBuckets nodesToBuckets;
	
	@Autowired
	private BucketsBalancer bucketsBalancer;
	
	@Autowired
	private MockNodeOperations mockNodeOperations;
	
	@Autowired
	private AllNodes allNodes;
	
	@Min(1)
	@Max(64)
	@Value("${bucketsToNodes.balancing.replicationFactor}")
	private int replicationFactor;
	
	private List<NodeAddress> mockNodes;
	
	@Before
	public void cleanOldBucketsToNodesMapping() {
		allNodes.getAllNodes().forEach(bucketsToNodes::remove);
	}
	
	@Given("^that (\\d+) more nodes are started$")
	public void that_more_nodes_are_started(int noOfNodes) throws JsonProcessingException {
	    mockNodes = IntStream.rangeClosed(1, noOfNodes)
	    	.boxed()
	    	.map(mockNodeOperations::startMockNode)
	    	.collect(Collectors.toList());
	    
	    mockNodeOperations.setMockNodeExpectations(mockNodes);
	    
	    
	    IntStream.range(0, nodeProperties.getNoOfBuckets())
    		.boxed()
    		.forEach(bucket -> {
    			mockNodes.forEach(node -> bucketsToNodes.add(bucket, node));
    		});
	}

	@Given("^and the Buckets \"([^\"]*)\" are only on one Node each$")
	public void and_the_Buckets_are_only_on_one_Node_each(String buckets) {
		Set<NodeAddress> mockNodesExceptFirst = mockNodes.subList(1, mockNodes.size())
						.stream().collect(Collectors.toSet());
		
		bucketsToNodes.removeAll(
				streamOfBuckets(buckets)
					.collect(toMap(
						identity(), 
						bucket -> mockNodesExceptFirst))
				);
	}

	@When("^the Bucket Balancer is running$")
	public void the_Bucket_Balancer_is_running() throws InterruptedException {
		bucketsBalancer.balanceBuckets();
	}

	@Then("^the node \"([^\"]*)\" are copied on the Current Node$")
	public void the_node_are_copied_on_the_Current_Node(String buckets) {
		Optional<Integer> bucketNotCopied = streamOfBuckets(buckets)
			.filter(bucket -> ! bucketsInCurrentNode.isBucketOnCurrentNode(bucket))
			.findFirst();
		
		bucketNotCopied.ifPresent(bucket -> fail("Bucket " + bucket + " was not copied on current node"));
	}

	@Given("^all (\\d+) Nodes are storing all the Buckets$")
	public void all_Nodes_are_storing_all_the_Buckets(int noOfNodes) throws Throwable {
		Optional<NodeAddress> invalidNode = mockNodes.stream()
			.filter(this::nodeIsMissingBuckets)
			.findFirst();
		
		invalidNode.ifPresent(node -> fail("Node " + node + " is not storing all the Buckets"));
	}

	@Given("^the Current Node has no Bucket$")
	public void the_Current_Node_has_no_Bucket() throws Throwable {
		bucketsToNodes.remove(nodeProperties.getCurrentNodeAddress());
	}

	@Given("^the replication factor is (\\d+)$")
	public void the_replication_factor_is(int expectedReplicationFactor) throws Throwable {
		assertEquals("Expected replication factor is different", expectedReplicationFactor, replicationFactor);
	}

	@Given("^the total number of Buckets is (\\d+)$")
	public void the_total_number_of_Buckets_is(int expectedNumberOfBuckets) throws Throwable {
		assertEquals("Expected Number of Buckets is different", expectedNumberOfBuckets, nodeProperties.getNoOfBuckets());
	}

	@Then("^the Current Node is transferring (\\d+) Buckets from the rest of the Nodes$")
	public void the_Current_Node_is_transferring_Buckets_from_the_rest_of_the_Nodes(int expectedBucketsInCurrentNode) throws Throwable {
		assertEquals("Expected Number of Buckets is different", expectedBucketsInCurrentNode , bucketsInCurrentNode.getBucketsInCurrentNode().size());
	}
	
	private Stream<Integer> streamOfBuckets(String buckets) {
		return stream(buckets.split(","))
		.map(String::trim)
		.map(Integer::valueOf);
	}

	private boolean nodeIsMissingBuckets(NodeAddress node) {
		return nodesToBuckets.getNodesToBuckets().get(node).size() != nodeProperties.getNoOfBuckets();
	}
	
}
