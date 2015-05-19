  
    


import org.junit.Assert;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class StepDefinitions {

    private static final String NAME = "Jitendra";
    private static final String NOT = "not";

    private Person person;
    private boolean isWeekend;

    @Given("^There is a person$")
    public void There_is_a_person() throws Throwable {
        this.person = new Person(NAME);
    }

    @And("^it is (.*)weekend$")
    public void it_is_weekend(String isOrIsNotWeekend) throws Throwable {
        this.isWeekend = !(NOT.equals(isOrIsNotWeekend));
        System.out.println("Check for NPE:"+ NOT.equals(null));
    }

    @When("^the alarm rings$")
    public void the_alarm_rings() throws Throwable {
        
    }

    @Then("^the person should (.*)get up and go to work$")
    public void the_person_should_get_up_and_go_to_work(String isOrIsNotWeekend) throws Throwable {
        final String expectedMessage;
        if (NOT.equals(isOrIsNotWeekend)){
            expectedMessage = NAME + " goes to work!";
        }
        else {
            expectedMessage = NAME + " does not go to work!";
        }

        String actualMessage = this.person.goToWork(this.isWeekend);
        Assert.assertEquals(expectedMessage, actualMessage);
    }
}