package dk.mathmagicians.playground.architecture.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.google.protobuf.TextFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/// Over platforms written inline in the text format: the application with `Story` and
/// `PublishMessage`, the domain with two payloads, and the modules each test adds.
class StoriesTest {

    private static final String CORE =
            """
            module { name: "Application" ring: APPLICATION
              type { name: "Story" stereotype: "PrimaryPort" }
              type { name: "PublishMessage" stereotype: "PrimaryPort" }
              type { name: "Publisher" stereotype: "SecondaryPort" } }
            module { name: "Domain" ring: DOMAIN
              type { name: "Offer" stereotype: "ValueObject" }
              type { name: "Order" stereotype: "ValueObject" } }
            """;

    private static final String PURSE =
            """
            module { name: "Purse Story" ring: APPLICATION package: "stories.purse" stereotype: "Module"
              type { name: "KnowWhatIsLeft" stereotype: "PrimaryPort" }
              type { name: "Opening" stereotype: "ValueObject" }
              type { name: "PurseStory" wiring: "AutoConfiguration"
                                        wiring: "EnableConfigurationProperties(PurseProperties)" }
              type { name: "PurseStory.purse" wiring: "Bean(KnowWhatIsLeft)" parameter: "PurseProperties" }
              type { name: "PurseProperties" wiring: "ConfigurationProperties(purse)" } }
            arrow { from { module: "Purse Story" type: "KnowWhatIsLeft" }
                    to { module: "Application" type: "Story" } relation: IMPLEMENTS }
            arrow { from { module: "Purse Story" type: "KnowWhatIsLeft" }
                    to { module: "Domain" type: "Offer" } relation: SUBSCRIBES }
            arrow { from { module: "Purse Story" type: "KnowWhatIsLeft" }
                    to { module: "Domain" type: "Order" } relation: DEPENDS_ON }
            """;

    private static final String USES_PUBLISH_MESSAGE =
            """
            arrow { from { module: "White Rabbit Story" type: "NoTimeToHaggle" }
                    to { module: "Application" type: "PublishMessage" } relation: USES }
            """;

    private static final String RABBIT =
            """
            module { name: "White Rabbit Story" ring: APPLICATION
              type { name: "NoTimeToHaggle" stereotype: "PrimaryPort" }
              type { name: "Haggling" stereotype: "SecondaryPort" }
              type { name: "ConsoleHaggling" stereotype: "SecondaryAdapter" } }
            arrow { from { module: "White Rabbit Story" type: "NoTimeToHaggle" }
                    to { module: "Application" type: "Story" } relation: IMPLEMENTS }
            arrow { from { module: "White Rabbit Story" type: "NoTimeToHaggle" }
                    to { module: "Domain" type: "Order" } relation: PUBLISHES }
            arrow { from { module: "White Rabbit Story" type: "ConsoleHaggling" }
                    to { module: "White Rabbit Story" type: "Haggling" } relation: IMPLEMENTS }
            """
                    + USES_PUBLISH_MESSAGE;

    private static Platform platform(String... parts) {
        var builder = Platform.newBuilder();
        try {
            TextFormat.merge(String.join("", parts), builder);
        } catch (TextFormat.ParseException e) {
            throw new IllegalArgumentException(e);
        }
        return builder.build();
    }

    private static Story purse() {
        return Stories.of(platform(CORE, PURSE)).getFirst();
    }

    private static Story rabbit() {
        return Stories.of(platform(CORE, RABBIT)).getFirst();
    }

    /// A module whose PrimaryPort implements Story. The application's own ports, and a port that
    /// implements nothing, are no story.
    @Test
    void findsTheModulesWhosePrimaryPortImplementsStory() {
        assertThat(Stories.of(platform(CORE, PURSE, RABBIT)))
                .extracting(Story::getName)
                .containsExactly("Purse Story", "White Rabbit Story");
        assertThat(Stories.of(platform(CORE))).isEmpty();
    }

    @Test
    void namesTheStoryAndItsPackageAsTheModule() {
        assertThat(purse().getName()).isEqualTo("Purse Story");
        assertThat(purse().getBasePackage()).isEqualTo("stories.purse");
    }

    /// A primary port with what it implements; a secondary port with what implements it.
    @Test
    void listsThePortsOfBothKinds() {
        assertThat(purse().getPrimaryPortList())
                .extracting(Port::getName, Port::getImplements)
                .containsExactly(tuple("KnowWhatIsLeft", "Story"));
        assertThat(purse().getSecondaryPortList()).isEmpty();
        assertThat(rabbit().getSecondaryPortList())
                .extracting(Port::getName, Port::getImplementedByList)
                .containsExactly(tuple("Haggling", List.of("ConsoleHaggling")));
    }

    @Test
    void listsWhatThePortSubscribesTo() {
        assertThat(purse().getSubscribesList())
                .extracting(Subscribes::getPayload)
                .containsExactly("Offer");
        assertThat(purse().getPublishesList()).isEmpty();
    }

    /// What the port publishes, through the application port it uses.
    @Test
    void listsWhatThePortPublishesThroughThePortItUses() {
        assertThat(rabbit().getPublishesList())
                .extracting(Publishes::getPayload, Publishes::getThrough)
                .containsExactly(tuple("Order", "PublishMessage"));
    }

    /// Without one application port used, `through` is absent, never guessed.
    @Test
    void leavesThroughAbsentWithoutOneUsedPort() {
        var rabbit =
                Stories.of(platform(CORE, RABBIT.replace(USES_PUBLISH_MESSAGE, ""))).getFirst();

        assertThat(rabbit.getPublishes(0).getThrough()).isEmpty();
    }

    /// The modules the story's arrows reach, each once, in the arrows' order, the story's own left
    /// out.
    @Test
    void listsTheModulesTheStoryReaches() {
        assertThat(purse().getDependsOnList()).containsExactly("Application", "Domain");
        assertThat(rabbit().getDependsOnList()).containsExactly("Application", "Domain");
    }

    /// The story's other stereotyped types, the ports and the wiring left out.
    @Test
    void listsTheOtherStereotypedTypes() {
        assertThat(purse().getTypeList())
                .extracting(Type::getName, Type::getStereotypeList)
                .containsExactly(tuple("Opening", List.of("ValueObject")));
    }

    /// The configuration, the settings it enables with their prefix, its beans with what they build
    /// and build from; a story without an auto-configuration has no wiring.
    @Test
    void readsTheWiringFromTheSpringAnnotations() {
        var wiring = purse().getWiring();

        assertThat(wiring.getConfiguration()).isEqualTo("PurseStory");
        assertThat(wiring.getSettingsList())
                .extracting(Settings::getType, Settings::getPrefix)
                .containsExactly(tuple("PurseProperties", "purse"));
        assertThat(wiring.getBeanList())
                .extracting(Bean::getName, Bean::getBuilds, Bean::getFromList)
                .containsExactly(tuple("purse", "KnowWhatIsLeft", List.of("PurseProperties")));
        assertThat(rabbit().hasWiring()).isFalse();
    }

    /// One story in the build: every behaviour feature is its, the architecture feature is not; a
    /// missing directory gives none.
    @Test
    void givesTheOneStoryEveryBehaviourFeature(@TempDir Path features) throws IOException {
        Files.writeString(
                features.resolve("alices_purse_knows_what_is_left.feature"),
                "Feature: Alice's purse knows what is left\n");
        Files.writeString(
                features.resolve("the_purse_has_hexagonal_architecture.feature"),
                "@architecture-discovery\nFeature: The purse has a hexagonal architecture\n");

        assertThat(Stories.of(platform(CORE, PURSE), features).getFirst().getFeatureList())
                .containsExactly("alices_purse_knows_what_is_left.feature");
        assertThat(
                        Stories.of(platform(CORE, PURSE), features.resolve("none"))
                                .getFirst()
                                .getFeatureList())
                .isEmpty();
        assertThat(purse().getFeatureList()).isEmpty();
    }

    /// Several stories in the build: a feature belongs to the story whose project it lives in,
    /// `white-rabbit/src/test/resources/features`; one living elsewhere says whose it is,
    /// `@story:purse`; one that does neither stays nobody's.
    @Test
    void givesAFeatureToTheStoryOfItsBuildOrTheOneItNames(@TempDir Path build) throws IOException {
        var platform =
                Files.createDirectories(build.resolve("platform/src/test/resources/features"));
        var rabbit =
                Files.createDirectories(build.resolve("white-rabbit/src/test/resources/features"));
        Files.writeString(
                platform.resolve("alices_purse_knows_what_is_left.feature"),
                "@story:purse\nFeature: Alice's purse knows what is left\n");
        Files.writeString(platform.resolve("nobodys.feature"), "Feature: Nobody's\n");
        Files.writeString(
                rabbit.resolve("the_white_rabbit_has_no_time_to_haggle.feature"),
                "@slow\nFeature: The White Rabbit has no time to haggle\n");

        var stories = Stories.of(platform(CORE, PURSE, RABBIT), List.of(platform, rabbit));

        assertThat(stories.get(0).getFeatureList())
                .containsExactly("alices_purse_knows_what_is_left.feature");
        assertThat(stories.get(1).getFeatureList())
                .containsExactly("the_white_rabbit_has_no_time_to_haggle.feature");
    }
}
