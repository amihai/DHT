package ro.amihai.dht.integrationtests.removenodes;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/resources/features/removeUnreachableNodes.feature"} ,
		strict = true)
public class RemoveUnreachableNodesTest {

}
