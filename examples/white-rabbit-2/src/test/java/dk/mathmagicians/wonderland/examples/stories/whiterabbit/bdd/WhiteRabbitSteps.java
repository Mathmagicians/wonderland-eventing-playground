package dk.mathmagicians.wonderland.examples.stories.whiterabbit.bdd;

import dk.mathmagicians.wonderland.eventing.bdd.Region;
import dk.mathmagicians.wonderland.eventing.bdd.StoryContainer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.math.BigDecimal;

/// Steps of "The White Rabbit has no time to haggle", glue to `WhiteRabbitTable`: what the White
/// Rabbit wants, and what it ordered. The offers on the stream, the reading, and the transport are
/// the platform's steps. The characters and the things are their ids on the wire, see
/// `ParameterTypes.wonderlander` and `ParameterTypes.thing`.
public class WhiteRabbitSteps {

    private final WhiteRabbitTable table;

    public WhiteRabbitSteps(StoryContainer story, Region region) {
        this.table = new WhiteRabbitTable(story, region);
    }

    @Given("the White Rabbit wants a {thing}")
    public void theWhiteRabbitWantsA(String thing) {
        table.wants(thing);
    }

    @Then("the {wonderlander} ordered a {thing} from {wonderlander} at {bigdecimal} coins")
    public void orderedAThingFromAtCoins(
            String customer, String thing, String seller, BigDecimal price) {
        table.assertOrdered(customer, thing, seller, price);
    }

    @Then("nothing else was ordered")
    public void nothingElseWasOrdered() {
        table.assertNothingElseOrdered();
    }
}
