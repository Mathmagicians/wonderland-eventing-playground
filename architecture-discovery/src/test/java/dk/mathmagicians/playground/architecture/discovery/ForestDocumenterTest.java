package dk.mathmagicians.playground.architecture.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.TextFormat;
import dk.mathmagicians.playground.architecture.model.Platform;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/// Over a platform written inline: Wonderland with two stories, the purse and the White Rabbit.
class ForestDocumenterTest {

    static final String WONDERLAND =
            """
            name: "Wonderland"
            module { name: "Application" ring: APPLICATION
              type { name: "Story" stereotype: "PrimaryPort" }
              type { name: "PublishMessage" stereotype: "PrimaryPort" } }
            module { name: "Domain" ring: DOMAIN
              type { name: "Offer" stereotype: "ValueObject" }
              type { name: "Order" stereotype: "ValueObject" }
              type { name: "Transaction" stereotype: "ValueObject" } }
            module { name: "Purse Story" ring: APPLICATION package: "dk.mathmagicians.wonderland.examples.stories.purse"
              type { name: "KnowWhatIsLeft" stereotype: "PrimaryPort" } }
            module { name: "White Rabbit Story" ring: APPLICATION package: "dk.mathmagicians.wonderland.stories.whiterabbit"
              type { name: "NoTimeToHaggle" stereotype: "PrimaryPort" } }
            arrow { from { module: "Purse Story" type: "KnowWhatIsLeft" }
                    to { module: "Application" type: "Story" } relation: IMPLEMENTS }
            arrow { from { module: "Purse Story" type: "KnowWhatIsLeft" }
                    to { module: "Domain" type: "Transaction" } relation: SUBSCRIBES }
            arrow { from { module: "White Rabbit Story" type: "NoTimeToHaggle" }
                    to { module: "Application" type: "Story" } relation: IMPLEMENTS }
            arrow { from { module: "White Rabbit Story" type: "NoTimeToHaggle" }
                    to { module: "Application" type: "PublishMessage" } relation: USES }
            arrow { from { module: "White Rabbit Story" type: "NoTimeToHaggle" }
                    to { module: "Domain" type: "Offer" } relation: SUBSCRIBES }
            arrow { from { module: "White Rabbit Story" type: "NoTimeToHaggle" }
                    to { module: "Domain" type: "Order" } relation: PUBLISHES }
            """;

    /// A line of styling: a skinparam, a style block, a colour, a font size, a bold.
    static final Pattern STYLING =
            Pattern.compile("skinparam|<style>|#[0-9A-Fa-f]{6}|#line:|<size:|<b>");

    static Platform wonderland() {
        var builder = Platform.newBuilder();
        try {
            TextFormat.merge(WONDERLAND, builder);
        } catch (TextFormat.ParseException e) {
            throw new IllegalArgumentException(e);
        }
        return builder.build();
    }

    private final String uml = new ForestDocumenter(wonderland()).toPlantUml();

    @Test
    void carriesNoStyling() {
        assertThat(uml.lines()).noneMatch(line -> STYLING.matcher(line).find());
    }

    /// The platform a hexagon named as the system, every story a hexagon with its package, each
    /// playing on the platform.
    @Test
    void drawsThePlatformAndTheStoriesThatPlayOnIt() {
        assertThat(uml)
                .startsWith("@startuml\n")
                .contains("hexagon \"Wonderland\\nplatform\" as platform\n")
                .contains("hexagon \"Purse Story\\nstories/purse\" as s_Purse_Story\n")
                .contains(
                        "hexagon \"White Rabbit Story\\nstories/whiterabbit\" as s_White_Rabbit_Story\n")
                .contains("s_Purse_Story --> platform : plays on\n")
                .contains("s_White_Rabbit_Story --> platform : plays on\n")
                .endsWith("@enduml\n");
        assertThat(uml).doesNotContain("Application").doesNotContain("Domain");
    }

    @Test
    void writesTheSameTextTwice() {
        assertThat(new ForestDocumenter(wonderland()).toPlantUml()).isEqualTo(uml);
    }
}
