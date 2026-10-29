package dk.mathmagicians.wonderland.architecture.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.TextFormat;
import dk.mathmagicians.playground.architecture.model.Module;
import dk.mathmagicians.playground.architecture.model.Platform;
import dk.mathmagicians.playground.architecture.model.Port;
import dk.mathmagicians.playground.architecture.model.Story;
import dk.mathmagicians.playground.architecture.model.Wiring;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/// Test driver of the architecture a build wrote: the stories under
/// `docs/generated/architecture/model`, one `.textproto` each, and the platform beside them, read
/// through the DSL's schema, and the module a scenario looks at. The folder is `architecture.docs`,
/// the system property `ModuleDocumentation` writes under, `docs/generated/architecture` by
/// default.
public class ArchitectureModel {

    private static final String MODEL = "model";
    private static final String TEXTPROTO = ".textproto";
    private static final String PLATFORM = "platform" + TEXTPROTO;

    /// The last `bdd` run's report, written by `make bdd`: which scenarios ran, and what they said.
    private static final Path LAST_RUN = Path.of("build", "reports", "cucumber", "cucumber.json");

    /// A feature's start in the report, by its file name, and a name in it, a scenario's or a
    /// step's.
    private static final Pattern REPORTED_FEATURE =
            Pattern.compile("\"uri\"\\s*:\\s*\"[^\"]*?([^\"/]+\\.feature)\"");

    private static final Pattern REPORTED_NAME = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]*)\"");

    /// An annotation as a feature names it, `@PrimaryPort`; a tag, `@architecture`, is lower case
    /// and not one.
    private static final Pattern ANNOTATION = Pattern.compile("@[A-Z]\\w*");

    private final Map<String, Story> stories = new TreeMap<>();
    private Platform platform = Platform.getDefaultInstance();
    private @Nullable Story module;

    /// Reads every story the build wrote, and the platform.
    public void read() {
        var folder =
                Path.of(
                        System.getProperty("architecture.docs", "docs/generated/architecture"),
                        MODEL);
        assertThat(folder).as("the model the build wrote").isDirectory();
        try (var files = Files.list(folder)) {
            files.filter(file -> file.toString().endsWith(TEXTPROTO))
                    .forEach(
                            file -> {
                                if (file.getFileName().toString().equals(PLATFORM)) {
                                    platform = parse(file, Platform.newBuilder()).build();
                                } else {
                                    var story = parse(file, Story.newBuilder()).build();
                                    stories.put(story.getName(), story);
                                }
                            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        assertThat(platform.getName()).as("the platform under %s", folder).isNotEmpty();
    }

    private static <B extends com.google.protobuf.Message.Builder> B parse(Path file, B builder) {
        try {
            TextFormat.merge(Files.readString(file), builder);
            return builder;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// The module a scenario looks at, by the name `@Module` gives it.
    public Story module(String name) {
        assertThat(stories).as("the stories the build wrote").containsKey(name);
        module = stories.get(name);
        return module;
    }

    public Story module() {
        assertThat(module).as("the module of the scenario, named in the Background").isNotNull();
        return module;
    }

    /// The module as the platform carries it: every stereotype and every wiring the build found, on
    /// the module and on its types.
    public Module platformModule() {
        var name = module().getName();
        return platform.getModuleList().stream()
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst()
                .orElseThrow(
                        () ->
                                new AssertionError(
                                        "the platform the build wrote has no module " + name));
    }

    /// The module's primary port by name, when it has it.
    public Optional<Port> primaryPort(String name) {
        return module().getPrimaryPortList().stream()
                .filter(port -> port.getName().equals(name))
                .findFirst();
    }

    /// The module's wiring, its auto-configuration and what it declares.
    public Wiring wiring() {
        assertThat(module().hasWiring()).as("the wiring of %s", module().getName()).isTrue();
        return module().getWiring();
    }

    /// The names the last `bdd` run reported for the features given, scenarios and steps alike, in
    /// lower case: what a scenario said, a stream it read, a thing it placed. No features given
    /// means every feature of the run, the build's own. Fails when no run is there to read.
    public List<String> namesReportedFor(List<String> features) {
        assertThat(LAST_RUN).as("the last bdd run's report, written by make bdd").isRegularFile();
        try {
            var report = Files.readString(LAST_RUN);
            var starts = REPORTED_FEATURE.matcher(report).results().toList();
            var names = new ArrayList<String>();
            for (var i = 0; i < starts.size(); i++) {
                if (!features.isEmpty() && !features.contains(starts.get(i).group(1))) {
                    continue;
                }
                var end = i + 1 < starts.size() ? starts.get(i + 1).start() : report.length();
                REPORTED_NAME
                        .matcher(report.substring(starts.get(i).start(), end))
                        .results()
                        .map(match -> match.group(1).toLowerCase())
                        .forEach(names::add);
            }
            return names;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// The annotations a feature names, `@PrimaryPort`, `@Bean`, in every line of it; the feature
    /// as Cucumber found it, on the class path or in a file.
    public static Stream<String> annotationsNamedIn(URI feature) {
        return ANNOTATION.matcher(text(feature)).results().map(match -> match.group()).distinct();
    }

    private static String text(URI feature) {
        try {
            if ("classpath".equals(feature.getScheme())) {
                var resource = feature.getSchemeSpecificPart().replaceFirst("^/", "");
                try (var in =
                        ArchitectureModel.class.getClassLoader().getResourceAsStream(resource)) {
                    assertThat(in).as("the feature %s on the class path", resource).isNotNull();
                    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
            return Files.readString(Path.of(feature));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
