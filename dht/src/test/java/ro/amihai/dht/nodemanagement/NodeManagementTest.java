package ro.amihai.dht.nodemanagement;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import jdk.nashorn.internal.ir.annotations.Ignore;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/resources/features/nodeManagement.feature" },
		strict = true,
		tags = "~@Ignore")
public class NodeManagementTest {

}
