package ro.amihai.dht.node;

import static java.util.Optional.ofNullable;

import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="node")
public class NodeProperties {

	private int noOfBuckets;
	
	@NestedConfigurationProperty
	private NodeAddress nodeAddress = new NodeAddress();

	private String[] initializeFromNodes;
	
	public int getNoOfBuckets() {
		return noOfBuckets;
	}

	public void setNoOfBuckets(int noOfBuckets) {
		this.noOfBuckets = noOfBuckets;
	}

	public NodeAddress getNodeAddress() {
		return nodeAddress;
	}

	public void setNodeAddress(NodeAddress nodeAddress) {
		this.nodeAddress = nodeAddress;
	}

	public Optional<String[]> getInitializeFromNodes() {
		return ofNullable(initializeFromNodes);
	}

	public void setInitializeFromNodes(String[] initializeFromNodes) {
		this.initializeFromNodes = initializeFromNodes;
	}
	
	
}
