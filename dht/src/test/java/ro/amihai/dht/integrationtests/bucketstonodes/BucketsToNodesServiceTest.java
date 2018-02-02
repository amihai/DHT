package ro.amihai.dht.integrationtests.bucketstonodes;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/resources/features/bucketsToNodesService.feature"} ,
		strict = true)
public class BucketsToNodesServiceTest {
}
