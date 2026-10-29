package dk.mathmagicians.playground.architecture.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.importer.ImportOption;
import dk.mathmagicians.playground.architecture.discovery.fixture.FixtureApplication;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/// Over the fixture application, the same five modules `HexagonDocumenterTest` draws: the model the
/// writer reads out of the classes.
class ModelWriterTest {

    private static final ApplicationModules FIXTURE =
            ApplicationModules.of(
                    FixtureApplication.PACKAGE, ImportOption.Predefined.DO_NOT_INCLUDE_JARS);

    private final Platform platform = new ModelWriter(FIXTURE).toPlatform();

    private Module module(String name) {
        return platform.getModuleList().stream()
                .filter(module -> module.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private Type type(String module, String name) {
        return module(module).getTypeList().stream()
                .filter(type -> type.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private static Arrow arrow(
            String fromModule, String from, String toModule, String to, Relation relation) {
        return Arrow.newBuilder()
                .setFrom(End.newBuilder().setModule(fromModule).setType(from))
                .setTo(End.newBuilder().setModule(toModule).setType(to))
                .setRelation(relation)
                .build();
    }

    /// The modules in the hexagon's order, each in its ring, named as Modulith names them, with
    /// their full package. No `@Modulithic`, so the system is the root package's last segment.
    @Test
    void placesTheModulesInTheirRingsInTheHexagonsOrder() {
        assertThat(platform.getName()).isEqualTo("fixture");
        assertThat(platform.getModuleList())
                .extracting(Module::getName, Module::getRing)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Cli", Ring.DRIVING),
                        org.assertj.core.groups.Tuple.tuple("Application", Ring.APPLICATION),
                        org.assertj.core.groups.Tuple.tuple("Domain", Ring.DOMAIN),
                        org.assertj.core.groups.Tuple.tuple("Kafka", Ring.DRIVEN),
                        org.assertj.core.groups.Tuple.tuple("Wire", Ring.SHARED));
        assertThat(module("Cli").getPackage()).isEqualTo(FixtureApplication.PACKAGE + ".cli");
    }

    /// `@Module` on the application's package, `@Adapter` on the wire's: the module's stereotypes.
    @Test
    void carriesThePackagesAnnotationsOnTheModule() {
        assertThat(module("Application").getStereotypeList()).containsExactly("Module");
        assertThat(module("Wire").getStereotypeList()).containsExactly("Adapter");
        assertThat(module("Application").getWiringList()).isEmpty();
        assertThat(module("Cli").getStereotypeList()).isEmpty();
    }

    /// A type's role is its stereotype, its Spring annotations with their values its wiring, the
    /// two apart; `Codec` has its package's role; `Defaults` carries nothing and has no entry.
    @Test
    void keepsATypesStereotypesAndWiringApart() {
        assertThat(type("Cli", "Runner").getStereotypeList()).containsExactly("PrimaryAdapter");
        assertThat(type("Cli", "Runner").getWiringList())
                .containsExactly("Component", "Profile(cli)");
        assertThat(type("Application", "Publish").getStereotypeList())
                .containsExactly("PrimaryPort");
        assertThat(type("Wire", "Codec").getStereotypeList()).containsExactly("Adapter");
        assertThat(type("Application", "Settings").getStereotypeList()).isEmpty();
        assertThat(type("Application", "Settings").getWiringList())
                .containsExactly("ConfigurationProperties(fixture)");
        assertThat(module("Application").getTypeList())
                .extracting(Type::getName)
                .doesNotContain("Defaults");
    }

    /// A method with an annotation is an entry of its own after its type, a bean's naming what it
    /// builds and the parameters it builds from; the ports come first, then the wiring, the class
    /// with the beans before the settings it binds.
    @Test
    void listsAnAnnotatedMethodAfterItsType() {
        assertThat(type("Application", "Wiring").getWiringList())
                .containsExactly("AutoConfiguration", "EnableConfigurationProperties(Settings)");
        assertThat(type("Application", "Wiring.publish").getWiringList())
                .containsExactly("Bean(Publish)", "Primary");
        assertThat(type("Application", "Wiring.publish").getParameterList())
                .containsExactly("Publisher");
        assertThat(module("Application").getTypeList())
                .extracting(Type::getName)
                .containsExactly(
                        "Publish", "Squarer", "Publisher", "Wiring", "Wiring.publish", "Settings");
    }

    /// `Shape` is sealed: `Circle` and `Square` follow it in the domain's list, and each implements
    /// it.
    @Test
    void listsThePermittedTypesAfterTheirSealedType() {
        var names = module("Domain").getTypeList().stream().map(Type::getName).toList();

        assertThat(names).containsSubsequence("Shape", "Circle", "Square");
        assertThat(platform.getArrowList())
                .contains(
                        arrow("Domain", "Circle", "Domain", "Shape", Relation.IMPLEMENTS),
                        arrow("Domain", "Square", "Domain", "Shape", Relation.IMPLEMENTS));
    }

    /// `Runner`, a component, has `Publish` injected, a use; its `Defaults` has no entry and the
    /// arrow still names it; `KafkaPublisher` implements the port; `Publish` holds a `Publisher`
    /// and takes a `Thing`; the wiring reaches the port it builds.
    @Test
    void drawsTheArrowsWithTheirRelation() {
        assertThat(platform.getArrowList())
                .contains(
                        arrow("Cli", "Runner", "Application", "Publish", Relation.USES),
                        arrow("Cli", "Runner", "Application", "Defaults", Relation.USES),
                        arrow("Cli", "Runner", "Wire", "Codec", Relation.DEPENDS_ON),
                        arrow(
                                "Kafka",
                                "KafkaPublisher",
                                "Application",
                                "Publisher",
                                Relation.IMPLEMENTS),
                        arrow("Application", "Publish", "Application", "Publisher", Relation.USES),
                        arrow("Application", "Publish", "Domain", "Thing", Relation.DEPENDS_ON),
                        arrow(
                                "Application",
                                "Wiring",
                                "Application",
                                "Publish",
                                Relation.DEPENDS_ON));
        assertThat(platform.getArrowList())
                .extracting(arrow -> List.of(arrow.getFrom().getType(), arrow.getTo().getType()))
                .doesNotContain(List.of("Runner", "Thing"));
    }

    /// `Squarer`, a driving port, names `Circle.class` in `subscribesTo()` and publishes through
    /// `Publish`, so the squares it builds are what it publishes; `Publish` publishes nothing, it
    /// reaches no other driving port.
    @Test
    void drawsWhatAPortSubscribesToAndPublishes() {
        assertThat(platform.getArrowList())
                .contains(
                        arrow("Application", "Squarer", "Domain", "Circle", Relation.SUBSCRIBES),
                        arrow("Application", "Squarer", "Domain", "Square", Relation.PUBLISHES),
                        arrow("Application", "Squarer", "Application", "Publish", Relation.USES));
        assertThat(platform.getArrowList())
                .filteredOn(arrow -> arrow.getRelation() == Relation.PUBLISHES)
                .extracting(arrow -> arrow.getFrom().getType())
                .containsOnly("Squarer");
    }

    @Test
    void writesTheSameModelTwice() {
        assertThat(new ModelWriter(FIXTURE).toPlatform()).isEqualTo(platform);
    }
}
