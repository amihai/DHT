package ro.amihai.dht.tests.util;

import static java.util.Collections.emptyList;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ro.amihai.dht.bucketstonodes.BucketsToNodes;
import ro.amihai.dht.keyvaluestore.size.BucketSize;
import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.node.NodeProperties;

@Component
public class MockNodeOperations {

	private static final String BUCKET_DETAIL = "(.*)buckets/(\\d*)";

	private static final String BUCKETS_TO_NODES = "(.*)/bucketsToNodes";

	private static final String BUCKETS_SIZE = "(.*)/buckets/size";

	private Logger logger = LoggerFactory.getLogger(MockNodeOperations.class);
	
	private static final int MOCK_NODES_STARTING_PORT = 7000;
	
	@Lazy
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private NodeProperties nodeProperties;
	
	@Autowired
	private BucketsToNodes bucketsToNodes;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public NodeAddress startMockNode(int nodeNumber) {
		int port = nodeNumber + MOCK_NODES_STARTING_PORT;
		
		logger.info("Adding Mock Node with port {}", port);
		
		NodeAddress nodeAddress = new NodeAddress("localhost", port);
		return nodeAddress;
	}
	
	public void setMockNodeExpectations(List<NodeAddress> mockNodes) throws JsonProcessingException {
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		
		server.expect(manyTimes(), request -> {}) //Match All requests
			.andRespond(this::createResponse);
	}
	
	private ClientHttpResponse createResponse(ClientHttpRequest request) throws IOException {
		String url = request.getURI().toString();
		String body = "";
		if (url.matches(BUCKETS_SIZE)) {
			body = objectMapper.writeValueAsString(dummyBucketsSize());
		} else if (url.matches(BUCKETS_TO_NODES)) {
			body = objectMapper.writeValueAsString(bucketsToNodes.getBucketsToNodes());
		} else if (url.matches(BUCKET_DETAIL)) {
			body = objectMapper.writeValueAsString(emptyList());
		}
		return MockRestResponseCreators.withSuccess(body, MediaType.APPLICATION_JSON).createResponse(request);
	}
	
	private Map<Integer, BucketSize> dummyBucketsSize() {
		return IntStream.range(0, nodeProperties.getNoOfBuckets())
				.boxed()
				.collect(Collectors.toMap(Function.identity(), 
						bucket -> new BucketSize(bucket, 0, 1)));
	}
	
}
