package dk.mathmagicians.playground.architecture.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.importer.ImportOption;
import dk.mathmagicians.playground.architecture.discovery.HexagonDocumenter.Options;
import dk.mathmagicians.playground.architecture.discovery.fixture.FixtureApplication;
import dk.mathmagicians.playground.architecture.model.ModelWriter;
import dk.mathmagicians.playground.architecture.model.Platform;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/// Over the model of the fixture application: five modules, `cli` with a driving adapter,
/// `application` with two ports, its settings, and its wiring, `domain` with value objects, a
/// sealed one among them, and a factory, `kafka` with a driven adapter, `wire` with a shared
/// adapter, the stereotype on its package. The fixture lives among the tests, which Modulith leaves
/// out by default, so the import option includes them.
class HexagonDocumenterTest {

    private static final ApplicationModules FIXTURE =
            ApplicationModules.of(
                    FixtureApplication.PACKAGE, ImportOption.Predefined.DO_NOT_INCLUDE_JARS);

    private static final Platform PLATFORM = new ModelWriter(FIXTURE).toPlatform();

    /// A line of styling: a skinparam, a style block, a colour, a font size, a bold.
    private static final Pattern STYLING =
            Pattern.compile("skinparam|<style>|#[0-9A-Fa-f]{6}|#line:|<size:|<b>");

    private final String uml = new HexagonDocumenter(PLATFORM).toPlantUml();

    /// The text is content: the elements, their stereotypes, their nesting, the arrows. The look is
    /// `hexagon.style.puml`'s.
    @Test
    void carriesNoStyling() {
        assertThat(uml.lines()).noneMatch(line -> STYLING.matcher(line).find());
    }

    /// Every module's cards sit inside a padding rectangle, so its slanted sides clear them; the
    /// rectangle has no look of its own in the text.
    @Test
    void padsEveryModule() {
        assertThat(uml)
                .contains("rectangle \" \" as m_Cli_in {")
                .contains("rectangle \" \" as m_Domain_in {");
    }

    /// A card in a module: the type's name, its alias, its role as the stereotype, and nothing
    /// after it.
    private static String card(String module, String type, String role) {
        return "card \"" + type + "\" as m_" + module + "_" + type + " <<" + role + ">>\n";
    }

    /// A module's hexagon: its name above its package, the last two segments, no stereotype.
    private static String hexagon(String pkg, String name) {
        return "hexagon \"" + name + "\\n" + pkg + "\" as m_" + name + " {";
    }

    /// Module names are Modulith's display names, the last package segment capitalised. A column is
    /// a package with the ring's name as its title; a module is a hexagon.
    @Test
    void placesModulesByTheStereotypesOfTheirTypes() {
        assertThat(uml)
                .contains("package \"DRIVING ADAPTERS\" as driving {")
                .contains(hexagon("fixture/cli", "Cli"))
                .contains("package \"APPLICATION\" as core {")
                .contains(hexagon("fixture/application", "Application"))
                .contains(hexagon("fixture/domain", "Domain"))
                .contains("package \"DRIVEN ADAPTERS\" as driven {")
                .contains(hexagon("fixture/kafka", "Kafka"));
    }

    @Test
    void nestsTheDomainInsideTheApplication() {
        var application = uml.indexOf(hexagon("fixture/application", "Application"));
        var domain = uml.indexOf(hexagon("fixture/domain", "Domain"));
        var driven = uml.indexOf("package \"DRIVEN ADAPTERS\"");

        assertThat(application).isLessThan(domain);
        assertThat(domain).isLessThan(driven);
    }

    /// An empty anchor above each column, in a row, each column hung from its own, and the packages
    /// chained: what keeps the driving adapters left and the driven ones right.
    @Test
    void ordersTheColumnsLeftToRight() {
        assertThat(uml)
                .contains("label \" \" as t_driving")
                .contains("t_driving -[hidden]right-> t_core")
                .contains("t_core -[hidden]right-> t_driven")
                .contains("driving -[hidden]right-> core")
                .contains("core -[hidden]right-> driven")
                .contains("t_driving -[hidden]down-> m_Cli_Runner")
                .contains("t_core -[hidden]down-> m_Application_Squarer")
                .contains("t_driven -[hidden]down-> m_Kafka_KafkaPublisher");
    }

    /// `Codec` has no stereotype of its own; `@Adapter` on its package gives it the role. Its
    /// module is the row of shared adapters, written after the three columns and hung from the
    /// cards along the domain's bottom, and the adapters of both sides point into it without rank,
    /// so they keep their height.
    @Test
    void drawsTheSharedAdaptersInARowBelowTheApplication() {
        assertThat(uml)
                .contains("package \"SHARED ADAPTERS\" as shared {")
                .contains(hexagon("fixture/wire", "Wire"))
                .contains(card("Wire", "Codec", "Adapter"))
                .contains("m_Domain_Square -[hidden]down-> m_Wire_Codec")
                .contains("m_Cli_Runner .[norank].> m_Wire_Codec\n")
                .contains("m_Kafka_KafkaPublisher .[norank].> m_Wire_Codec\n");
        assertThat(uml.indexOf("package \"SHARED ADAPTERS\""))
                .isGreaterThan(uml.indexOf("package \"DRIVEN ADAPTERS\""));
    }

    @Test
    void listsHexagonalTypesWithTheirRole() {
        assertThat(uml)
                .contains(card("Cli", "Runner", "PrimaryAdapter"))
                .contains(card("Application", "Publish", "PrimaryPort"))
                .contains(card("Application", "Publisher", "SecondaryPort"))
                .contains(card("Kafka", "KafkaPublisher", "SecondaryAdapter"));
    }

    /// The model carries `Runner`'s `@Component` and `@Profile`, `Wiring`'s auto-configuration and
    /// bean, `Settings`' properties; the picture shows the roles alone, and a type with no role has
    /// no card.
    @Test
    void drawsRolesOnlyAndLeavesTheWiringToTheModel() {
        assertThat(uml)
                .contains(card("Cli", "Runner", "PrimaryAdapter"))
                .doesNotContain("<<@")
                .doesNotContain("m_Application_Wiring")
                .doesNotContain("m_Application_Settings")
                .doesNotContain("m_Application_Defaults");
    }

    @Test
    void listsEveryOtherStereotypedTypeByDefault() {
        assertThat(uml)
                .contains(card("Domain", "Things", "Factory"))
                .contains(card("Domain", "Item0", "ValueObject"))
                .contains(card("Domain", "Item9", "ValueObject"))
                .contains(card("Domain", "Thing", "ValueObject"))
                .doesNotContain("total)");
    }

    /// Thirteen value objects at the top level, the sealed one counted once with its permitted
    /// types: with a maximum of ten, `Item0` to `Item8` by name, then the ellipsis with the total.
    @Test
    void listsUpToTheMaximumThenTheTotal() {
        var capped =
                new HexagonDocumenter(PLATFORM, Options.defaults().withMaxTypes(10)).toPlantUml();

        assertThat(capped)
                .contains(card("Domain", "Item0", "ValueObject"))
                .contains(card("Domain", "Item8", "ValueObject"))
                .contains("card \"... (13 total)\" as m_Domain_ValueObject_total <<ValueObject>>")
                .doesNotContain(card("Domain", "Item9", "ValueObject"))
                .doesNotContain(card("Domain", "Thing", "ValueObject"))
                .doesNotContain(card("Domain", "Circle", "ValueObject"));
    }

    /// `Shape` permits `Circle` and `Square`: their cards come in a row under the domain's grid,
    /// hung from `Shape` alone, each with its arrow up to `Shape`.
    @Test
    void drawsThePermittedTypesUnderTheGridImplementingTheSealedType() {
        assertThat(uml)
                .contains(card("Domain", "Shape", "ValueObject"))
                .contains("m_Domain_Circle .up.|> m_Domain_Shape")
                .contains("m_Domain_Square .up.|> m_Domain_Shape")
                .contains("m_Domain_Circle -[hidden]right-> m_Domain_Square")
                .contains("m_Domain_Shape -[hidden]down-> m_Domain_Circle")
                .doesNotContain("m_Domain_Thing -[hidden]down-> m_Domain_Circle");
    }

    /// The domain's cards near square, fourteen in four columns; the application's two ports in one
    /// row above the domain, each hung to the middle of the domain's first row.
    @Test
    void laysCardsOutInAGrid() {
        assertThat(uml)
                .contains("m_Domain_Item0 -[hidden]right-> m_Domain_Item1")
                .contains("m_Domain_Things -[hidden]down-> m_Domain_Item3")
                .contains("m_Application_Publish -[hidden]right-> m_Application_Squarer")
                .contains("m_Application_Squarer -[hidden]right-> m_Application_Publisher")
                .contains("m_Application_Publish -[hidden]down-> m_Domain_Item0")
                .contains("m_Application_Publisher -[hidden]down-> m_Domain_Item0");
    }

    /// `Runner`, a component, holds a `Publish`, injected, so Modulith reads it as a use;
    /// `KafkaPublisher` implements `Publisher`, the port; `Publish` holds a `Publisher` and takes a
    /// `Thing`. Both ends have cards, so the arrows join the cards, without a label but for what a
    /// port subscribes to and publishes, which say so: the legend draws the other relations, in a
    /// row hung below the driven adapters and the shared adapters, and the text legend names the
    /// system. `Runner` holds a `Defaults` too, which has no role and no card: that arrow ends on
    /// the Application hexagon. `Wiring` has no card, so its arrows are not drawn.
    @Test
    void drawsTheArrowsWithTheLegend() {
        assertThat(uml)
                .contains("m_Cli_Runner -right-> m_Application_Publish\n")
                .contains("m_Cli_Runner -right-> m_Application\n")
                .contains("m_Kafka_KafkaPublisher .left.|> m_Application_Publisher\n")
                .contains("m_Application_Publish -right-> m_Application_Publisher\n")
                .contains("m_Application_Publish ..> m_Domain_Thing\n")
                .contains("m_Application_Squarer ..> m_Domain_Circle : subscribes\n")
                .contains("m_Application_Squarer ..> m_Domain_Square : publishes\n")
                .doesNotContain("m_Application_Wiring")
                .contains("frame \"legend\" as legend {")
                .contains("l_implements_from .right.|> l_implements_to : implements")
                .contains("l_uses_to -[hidden]right-> l_implements_from")
                .contains("l_implements_to -[hidden]right-> l_depends_on_from")
                .doesNotContain("speaks")
                .doesNotContain("-[dotted]->")
                .contains("m_Wire_Codec -[hidden]down-> l_uses_from")
                .doesNotContain("m_Kafka_KafkaPublisher -[hidden]down-> l_uses_from")
                .doesNotContain("m_Domain_Square -[hidden]down-> l_uses_from")
                .contains("legend right\n  fixture\nendlegend");
    }

    @Test
    void writesTheSameTextTwice() {
        assertThat(new HexagonDocumenter(PLATFORM).toPlantUml()).isEqualTo(uml);
    }
}
