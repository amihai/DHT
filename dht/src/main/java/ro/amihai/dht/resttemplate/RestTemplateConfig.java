package ro.amihai.dht.resttemplate;

import static java.util.Arrays.asList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Autowired
	private RemoveUnreachableNodes removeUnreachableNodes;
	
	@Bean
	public RestTemplate restTemplate() {
	    RestTemplate restTemplate = new RestTemplate();
	    restTemplate.setInterceptors(asList(removeUnreachableNodes));
		return restTemplate;
	}
}
