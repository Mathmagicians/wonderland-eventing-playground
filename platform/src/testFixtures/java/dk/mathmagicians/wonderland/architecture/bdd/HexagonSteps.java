package dk.mathmagicians.wonderland.architecture.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import dk.mathmagicians.playground.architecture.model.Bean;
import dk.mathmagicians.playground.architecture.model.Port;
import dk.mathmagicians.playground.architecture.model.Publishes;
import dk.mathmagicians.playground.architecture.model.Settings;
import dk.mathmagicians.playground.architecture.model.Subscribes;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/// The hexagon's steps, the library's words: the architecture the build wrote, a module, its ports,
/// its wiring, what it depends on, and that nothing else is stereotyped or wired. A client's
/// feature states the facts on these; the platform's `WonderlandSteps` add what a story subscribes
/// to and publishes.
public class HexagonSteps {

    private static final String AUTO_CONFIGURATION_IMPORTS =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    private final ArchitectureModel model;
    private URI feature = URI.create("");

    public HexagonSteps(ArchitectureModel model) {
        this.model = model;
    }

    /// The feature the scenario runs in, on the class path or in a file, for the annotations it
    /// names.
    @Before
    public void rememberTheFeature(Scenario scenario) {
        feature = scenario.getUri();
    }

    @Given("the architecture written by the build")
    public void theArchitectureWrittenByTheBuild() {
        model.read();
    }

    @Given("the module {string}, @Module on the package {word}")
    public void theModule(String name, String pkg) {
        var module = model.module(name);
        assertThat(module.getBasePackage()).as("the package of %s", name).endsWith("." + pkg);
        assertThat(model.platformModule().getStereotypeList())
                .as("the stereotypes on the package of %s", name)
                .contains("Module");
    }

    @Then("its @PrimaryPort is {word}, which implements {word}")
    public void itsPrimaryPortIs(String port, String contract) {
        assertThat(model.primaryPort(port))
                .as("the primary port %s", port)
                .map(Port::getImplements)
                .contains(contract);
    }

    @Then("its @AutoConfiguration is {word}, listed in META-INF\\/spring")
    public void itsAutoConfigurationIs(String type) {
        assertThat(model.wiring().getConfiguration()).isEqualTo(type);
        var listed = model.module().getBasePackage() + "." + type;
        assertThat(autoConfigurationImports())
                .as("the auto-configurations listed on the class path")
                .contains(listed);
    }

    @Then(
            "{word} has @EnableConfigurationProperties for {word}, the @ConfigurationProperties {string}")
    public void hasEnableConfigurationPropertiesFor(String type, String properties, String prefix) {
        assertThat(model.wiring().getConfiguration()).isEqualTo(type);
        assertThat(model.wiring().getSettingsList())
                .as("the settings %s enables", type)
                .extracting(Settings::getType, Settings::getPrefix)
                .contains(org.assertj.core.groups.Tuple.tuple(properties, prefix));
    }

    @Then("its @Bean {word} builds {word} from {word}")
    public void itsBeanBuilds(String method, String built, String from) {
        var bean =
                model.wiring().getBeanList().stream()
                        .filter(it -> it.getName().equals(method))
                        .findFirst();
        assertThat(bean).as("the bean %s", method).map(Bean::getBuilds).contains(built);
        assertThat(bean.orElseThrow().getFromList())
                .as("what the bean %s builds from", method)
                .contains(from);
    }

    @Then("the module depends on {word} and {word} only")
    public void theModuleDependsOnOnly(String one, String other) {
        var dependsOn =
                model.module().getDependsOnList().stream().map(String::toLowerCase).toList();
        assertThat(dependsOn).containsExactlyInAnyOrder(one.toLowerCase(), other.toLowerCase());
    }

    /// A payload is driven when a scenario of the story's features, in the last `bdd` run, names
    /// its stream or the payload itself: `reads the stream of transactions`, `offers a Top Hat`.
    /// The story's features are the ones it lists, else every feature of the run, the build's own.
    @Then("every payload it subscribes to or publishes is driven by a scenario of its feature")
    public void everyPayloadIsDrivenByAScenario() {
        var module = model.module();
        var payloads =
                Stream.concat(
                                module.getSubscribesList().stream().map(Subscribes::getPayload),
                                module.getPublishesList().stream().map(Publishes::getPayload))
                        .toList();
        assertThat(payloads)
                .as("what %s subscribes to or publishes", module.getName())
                .isNotEmpty();
        var reported = model.namesReportedFor(module.getFeatureList());
        assertThat(reported)
                .as("the last bdd run's scenarios of %s", module.getName())
                .isNotEmpty();
        assertThat(payloads)
                .allSatisfy(
                        payload ->
                                assertThat(reported)
                                        .as("a scenario driving %s", payload)
                                        .anyMatch(name -> name.contains(payload.toLowerCase())));
    }

    @Then("the module carries no Hexagonal Architecture stereotypes this feature does not state")
    public void theModuleCarriesNoStereotypeThisFeatureDoesNotState() {
        var stated = ArchitectureModel.annotationsNamedIn(feature).toList();
        var module = model.platformModule();
        var carried =
                Stream.concat(
                                module.getStereotypeList().stream(),
                                module.getTypeList().stream()
                                        .flatMap(type -> type.getStereotypeList().stream()))
                        .map(stereotype -> "@" + stereotype)
                        .distinct()
                        .toList();
        assertThat(carried)
                .as("the stereotypes the build found, against those %s states", feature)
                .isSubsetOf(stated);
    }

    @Then("the module carries no Spring wiring this feature does not state")
    public void theModuleCarriesNoWiringThisFeatureDoesNotState() {
        var stated = ArchitectureModel.annotationsNamedIn(feature).toList();
        var module = model.platformModule();
        var carried =
                Stream.concat(
                                module.getWiringList().stream(),
                                module.getTypeList().stream()
                                        .flatMap(type -> type.getWiringList().stream()))
                        .map(wiring -> "@" + wiring.replaceFirst("\\(.*\\)$", ""))
                        .distinct()
                        .toList();
        assertThat(carried)
                .as("the wiring the build found, against what %s states", feature)
                .isSubsetOf(stated);
    }

    /// Every auto-configuration listed on the class path, by its full name.
    private static List<String> autoConfigurationImports() {
        try {
            return Collections.list(
                            HexagonSteps.class
                                    .getClassLoader()
                                    .getResources(AUTO_CONFIGURATION_IMPORTS))
                    .stream()
                    .flatMap(HexagonSteps::lines)
                    .map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Stream<String> lines(URL resource) {
        try (var in = resource.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList().stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
