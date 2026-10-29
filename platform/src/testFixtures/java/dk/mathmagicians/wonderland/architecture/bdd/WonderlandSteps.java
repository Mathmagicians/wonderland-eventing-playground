package dk.mathmagicians.wonderland.architecture.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import dk.mathmagicians.playground.architecture.model.Subscribes;
import io.cucumber.java.en.Then;

/// Wonderland's words on top of the hexagon's: what a story's port subscribes to and publishes,
/// `Story.subscribesTo()` and `PublishMessage`, as the DSL carries them.
public class WonderlandSteps {

    private final ArchitectureModel model;

    public WonderlandSteps(ArchitectureModel model) {
        this.model = model;
    }

    @Then("{word} subscribesTo {word}")
    public void subscribesTo(String port, String payload) {
        assertThat(model.primaryPort(port)).as("the primary port %s", port).isPresent();
        assertThat(model.module().getSubscribesList())
                .map(Subscribes::getPayload)
                .contains(payload);
    }

    @Then("{word} publishes nothing")
    public void publishesNothing(String port) {
        assertThat(model.primaryPort(port)).as("the primary port %s", port).isPresent();
        assertThat(model.module().getPublishesList()).isEmpty();
    }

    @Then("{word} publishes {word} through {word}")
    public void publishesThrough(String port, String payload, String through) {
        assertThat(model.primaryPort(port)).as("the primary port %s", port).isPresent();
        assertThat(model.module().getPublishesList())
                .map(it -> it.getPayload() + " through " + it.getThrough())
                .contains(payload + " through " + through);
    }
}
