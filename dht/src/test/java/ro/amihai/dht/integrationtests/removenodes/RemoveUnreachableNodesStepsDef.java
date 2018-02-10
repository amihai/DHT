package ro.amihai.dht.integrationtests.removenodes;

import static org.junit.Assert.assertNotNull;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import ro.amihai.dht.bucketstonodes.BucketsToNodes;
import ro.amihai.dht.bucketstonodes.observer.AllNodes;
import ro.amihai.dht.integrationtests.SpringIntegrationStepDef;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;
import ro.amihai.dht.node.balancer.BucketsBalancer;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties= {"server.port=8003"})
public class RemoveUnreachableNodesStepsDef extends SpringIntegrationStepDef {
	
	@Value("${removeUnreachableNodes.maxNoOfFailures}")
	private int maxNoOfFailures;
	
	@Autowired
	private BucketsToNodes bucketsToNodes;	
	
	@Autowired
	private AllNodes allNodes;	
	
	@Autowired
	private BucketsBalancer bucketsBalancer;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Given("^that the current node is started$")
	public void that_the_current_node_is_started() throws Throwable {
	    assertNotNull("Current Node is not started", bucketsToNodes);
	}

	@Given("^the unreachable Node with host \"([^\"]*)\" and port \"([^\"]*)\" is add to the Buckets To Nodes Mapping$")
	public void the_unreachable_Node_with_host_and_port_is_add_to_the_Buckets_To_Nodes_Mapping(String host, int port) throws Throwable {
		IntStream.rangeClosed(1, maxNoOfFailures)
			.forEach(bucket -> {
				bucketsToNodes.add(bucket, new NodeAddress(host, port));
				bucketsToNodes.remove(bucket, nodeProperties.getCurrentNodeAddress());
			});
		
	}

	@When("^the Buckets Balancing is running$")
	public void the_Buckets_Balancing_is_running() throws Throwable {
		bucketsBalancer.balanceBuckets();
	}

	@Then("^the Node with host \"([^\"]*)\" and port \"([^\"]*)\" is removed from the Buckets To Nodes Mapping$")
	public void the_Node_with_host_and_port_is_removed_from_the_Buckets_To_Nodes_Mapping(String host, int port) throws Throwable {
		Assert.assertFalse("The Unreachable Node was not removed",  allNodes.getAllNodes().contains(new NodeAddress(host, port)));
	}

}
