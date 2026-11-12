package dk.mathmagicians.playground.architecture.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/// Over `ForestDocumenterTest`'s Wonderland: the purse subscribes to transactions, the White Rabbit
/// subscribes to offers and publishes orders.
class StreamsDocumenterTest {

    private final String uml =
            new StreamsDocumenter(ForestDocumenterTest.wonderland()).toPlantUml();

    @Test
    void carriesNoStyling() {
        assertThat(uml.lines())
                .noneMatch(line -> ForestDocumenterTest.STYLING.matcher(line).find());
    }

    /// A queue per payload a story publishes or subscribes to, sorted; a hexagon per story, an
    /// empty label inside it so its shape is drawn; the arrows into and out of the queues saying
    /// which, read left to right as the messages flow.
    @Test
    void drawsAQueuePerPayloadAndTheStoriesAroundThem() {
        assertThat(uml)
                .contains("left to right direction\n")
                .contains("queue \"Offer\" as q_Offer\n")
                .contains("queue \"Order\" as q_Order\n")
                .contains("queue \"Transaction\" as q_Transaction\n")
                .contains(
                        "hexagon \"Purse Story\" as s_Purse_Story <<story>> {\n"
                                + "  label \" \" as s_Purse_Story_in\n}\n")
                .contains("hexagon \"White Rabbit Story\" as s_White_Rabbit_Story <<story>> {\n")
                .contains("q_Transaction --> s_Purse_Story_in : subscribes\n")
                .contains("q_Offer --> s_White_Rabbit_Story_in : subscribes\n")
                .contains("s_White_Rabbit_Story_in --> q_Order : publishes\n")
                .doesNotContain("platform");
        assertThat(uml.indexOf("queue \"Offer\"")).isLessThan(uml.indexOf("queue \"Order\""));
    }

    @Test
    void writesTheSameTextTwice() {
        assertThat(new StreamsDocumenter(ForestDocumenterTest.wonderland()).toPlantUml())
                .isEqualTo(uml);
    }
}
