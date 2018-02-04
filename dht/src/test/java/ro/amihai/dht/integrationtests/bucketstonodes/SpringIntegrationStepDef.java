package ro.amihai.dht.integrationtests.bucketstonodes;

import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;

import ro.amihai.dht.DhtApplication;

@ContextConfiguration( classes = DhtApplication.class, loader=SpringBootContextLoader.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@EnableScheduling
public class SpringIntegrationStepDef {

}
