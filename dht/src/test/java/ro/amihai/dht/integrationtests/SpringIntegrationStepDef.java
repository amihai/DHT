package ro.amihai.dht.integrationtests;

import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;

import ro.amihai.dht.DhtApplication;

@ContextConfiguration( classes = DhtApplication.class, loader=SpringBootContextLoader.class)
@EnableScheduling
public class SpringIntegrationStepDef {

}
