package ro.amihai.dht.integrationtests.balancer;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/resources/features/bucketsBalancer.feature"} ,
		strict = true)
public class BucketsBalancerTest {

}
