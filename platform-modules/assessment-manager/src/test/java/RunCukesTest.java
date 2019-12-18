import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions (
		monochrome = false,
		format = {"pretty", "html:target/cucumber",
                "json:target_json/cucumber.json",
                "junit:target_junit/cucumber.xml"}
)
public class RunCukesTest {
	
}
