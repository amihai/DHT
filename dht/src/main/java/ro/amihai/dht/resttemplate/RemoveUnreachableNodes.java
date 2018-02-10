package ro.amihai.dht.resttemplate;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import ro.amihai.dht.bucketstonodes.BucketsToNodes;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.balancer.BucketsBalancer;

@Component
public class RemoveUnreachableNodes implements ClientHttpRequestInterceptor {

	private Logger logger = LoggerFactory.getLogger(BucketsBalancer.class);
	
	@Min(1)
	@Value("${removeUnreachableNodes.maxNoOfFailures}")
	private int maxNoOfFailures;
	
	
	@Value("${removeUnreachableNodes.timeToLiveMillis}")
	private long timeToLiveMillis;
	
	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	
	private Map<NodeAddress, AtomicInteger> unreachableNodes;
	
	@PostConstruct
	private void init() {
		unreachableNodes = new PassiveExpiringMap<>(timeToLiveMillis);
	}
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		request.getURI();
		try {
			return execution.execute(request, body);
		} catch (IOException e) {
			NodeAddress nodeAddressToBeRemoved = nodeAddress(request);
			unreachableNodes.putIfAbsent(nodeAddressToBeRemoved, new AtomicInteger(0));
			int noOfFailures = unreachableNodes.get(nodeAddressToBeRemoved).incrementAndGet();
			if (noOfFailures >= maxNoOfFailures) {
				logger.info("Remove Unreachable node {} from Buckets To Nodes mapping after {} failures ", nodeAddressToBeRemoved, noOfFailures);
				bucketsToNodes.remove(nodeAddressToBeRemoved);
			}
			throw e;
		}
	}
	
	private NodeAddress nodeAddress(HttpRequest request) {
		return new NodeAddress(request.getURI().getHost(), request.getURI().getPort());
	}

}
