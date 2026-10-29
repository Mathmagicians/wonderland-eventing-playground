package dk.mathmagicians.playground.architecture.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.TextFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/// The layout the specifications are written in, over a story and a platform built inline.
class TextProtoWriterTest {

    private static final Story PURSE =
            Story.newBuilder()
                    .setName("Purse Story")
                    .setBasePackage("dk.mathmagicians.wonderland.examples.stories.purse")
                    .addFeature("alices_purse_knows_what_is_left.feature")
                    .addPrimaryPort(
                            Port.newBuilder().setName("KnowWhatIsLeft").setImplements("Story"))
                    .addSubscribes(Subscribes.newBuilder().setPayload("Transaction"))
                    .addDependsOn("Application")
                    .addDependsOn("Domain")
                    .setWiring(
                            Wiring.newBuilder()
                                    .setConfiguration("PurseStory")
                                    .addSettings(
                                            Settings.newBuilder()
                                                    .setType("PurseProperties")
                                                    .setPrefix("purse"))
                                    .addBean(
                                            Bean.newBuilder()
                                                    .setName("purse")
                                                    .setBuilds("KnowWhatIsLeft")
                                                    .addFrom("PurseProperties")))
                    .build();

    private static final String PURSE_TEXT =
            """
            # proto-file: hexagon.proto
            # proto-message: Story

            name: "Purse Story"
            base_package: "dk.mathmagicians.wonderland.examples.stories.purse"
            feature: ["alices_purse_knows_what_is_left.feature"]
            primary_port { name: "KnowWhatIsLeft"  implements: "Story" }
            subscribes { payload: "Transaction" }
            depends_on: ["Application", "Domain"]
            wiring {
              configuration: "PurseStory"
              settings { type: "PurseProperties"  prefix: "purse" }
              bean { name: "purse"  builds: "KnowWhatIsLeft"  from: ["PurseProperties"] }
            }
            """;

    private static final String PLATFORM =
            """
            name: "Wonderland"
            module { name: "Application" ring: APPLICATION
              type { name: "Story" stereotype: "PrimaryPort" } }
            module { name: "Purse Story" ring: APPLICATION package: "stories.purse" stereotype: "Module"
              type { name: "KnowWhatIsLeft" stereotype: "PrimaryPort" }
              type { name: "PurseStory" wiring: "AutoConfiguration" } }
            arrow { from { module: "Purse Story" type: "KnowWhatIsLeft" }
                    to { module: "Application" type: "Story" } relation: IMPLEMENTS }
            """;

    private static Platform platform() {
        var builder = Platform.newBuilder();
        try {
            TextFormat.merge(PLATFORM, builder);
        } catch (TextFormat.ParseException e) {
            throw new IllegalArgumentException(e);
        }
        return builder.build();
    }

    /// A scalar per line, a repeated scalar as a list, a message on one line when it fits, two
    /// spaces between its fields, and opened over several lines when it does not, its own messages
    /// on one line each.
    @Test
    void printsTheCompactLayout() {
        assertThat(TextProtoWriter.toText(PURSE)).isEqualTo(PURSE_TEXT);
    }

    /// A module with one type fits on a line, its type inside it, a repeated field a list even with
    /// one value; an arrow's two ends too.
    @Test
    void printsAMessageWithMessagesInsideOnOneLineWhenItFits() {
        assertThat(TextProtoWriter.toText(platform()))
                .contains(
                        "module { name: \"Application\"  ring: APPLICATION  type { name: \"Story\"  stereotype: [\"PrimaryPort\"] } }\n")
                .contains(
                        "arrow { from { module: \"Purse Story\"  type: \"KnowWhatIsLeft\" }  to { module: \"Application\"  type: \"Story\" }  relation: IMPLEMENTS }\n");
    }

    @Test
    void headsTheTextWithTheSchemaAndTheMessage() {
        assertThat(TextProtoWriter.toText(platform()))
                .startsWith(
                        "# proto-file: hexagon.proto\n# proto-message: Platform\n\nname: \"Wonderland\"\n");
    }

    /// A string as the parser reads it, an apostrophe as it is.
    @Test
    void quotesStringsAndKeepsAnApostrophe() {
        var story = Story.newBuilder().setName("Alice's \"Purse\" Story").build();

        assertThat(TextProtoWriter.toText(story))
                .endsWith("name: \"Alice's \\\"Purse\\\" Story\"\n");
    }

    /// The module's name without a trailing "Story", in lower case, its words joined by hyphens.
    @Test
    void namesTheFileAsTheStoryIsNamed() {
        assertThat(TextProtoWriter.fileName(Story.newBuilder().setName("Tea Party Story").build()))
                .isEqualTo("tea-party");
        assertThat(TextProtoWriter.fileName(Story.newBuilder().setName("Purse story").build()))
                .isEqualTo("purse");
        assertThat(TextProtoWriter.fileName(Story.newBuilder().setName("White Rabbit").build()))
                .isEqualTo("white-rabbit");
    }

    @Test
    void writesOneFilePerStoryAndOneForThePlatform(@TempDir Path dir) throws IOException {
        new TextProtoWriter(platform(), List.of(dir.resolve("features")))
                .writeTo(dir.resolve("model"));

        assertThat(dir.resolve("model/platform.textproto")).exists();
        assertThat(Files.readString(dir.resolve("model/purse.textproto")))
                .startsWith(
                        "# proto-file: hexagon.proto\n# proto-message: Story\n\nname: \"Purse Story\"\n")
                .contains("primary_port { name: \"KnowWhatIsLeft\"  implements: \"Story\" }\n")
                .contains("wiring { configuration: \"PurseStory\" }\n");
    }

    @Test
    void writesTextTheParserReadsBack() throws TextFormat.ParseException {
        var read = Platform.newBuilder();
        TextFormat.merge(TextProtoWriter.toText(platform()), read);
        var story = Story.newBuilder();
        TextFormat.merge(PURSE_TEXT, story);

        assertThat(read.build()).isEqualTo(platform());
        assertThat(story.build()).isEqualTo(PURSE);
    }

    @Test
    void writesTheSameTextTwice() {
        assertThat(TextProtoWriter.toText(platform()))
                .isEqualTo(TextProtoWriter.toText(platform()));
    }
}
