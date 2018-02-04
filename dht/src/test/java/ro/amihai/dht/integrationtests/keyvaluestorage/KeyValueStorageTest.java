package ro.amihai.dht.integrationtests.keyvaluestorage;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/resources/features/keyValueStorage.feature" },
		strict = true)
public class KeyValueStorageTest {

}
