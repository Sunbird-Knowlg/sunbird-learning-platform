import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions (
		monochrome = false,
		format = {"pretty"}
)
public class RunCukesTest {

}
