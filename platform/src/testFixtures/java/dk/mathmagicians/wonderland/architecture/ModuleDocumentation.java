package dk.mathmagicians.wonderland.architecture;

import dk.mathmagicians.playground.architecture.discovery.ForestDocumenter;
import dk.mathmagicians.playground.architecture.discovery.HexagonDocumenter;
import dk.mathmagicians.playground.architecture.discovery.StreamsDocumenter;
import dk.mathmagicians.playground.architecture.model.ModelWriter;
import dk.mathmagicians.playground.architecture.model.TextProtoWriter;
import dk.mathmagicians.wonderland.EventingPlatform;
import java.io.File;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.modulith.docs.Documenter.CanvasOptions;
import org.springframework.modulith.docs.Documenter.DiagramOptions;
import org.springframework.modulith.docs.Documenter.DiagramOptions.DiagramStyle;

/// The application modules of a distribution, the packages marked `@Module` under the platform's
/// root, verified and documented: UML component diagrams of the modules and their dependencies as
/// PlantUML, a canvas per module, and the hexagon of the modules with their stereotyped types,
/// written under the folder the Gradle task names in `architecture.docs`. The verification runs
/// with the unit tests; the writer carries the `GENERATE` tag and runs from `make arch-gen`, which
/// renders the diagrams. A distribution extends this, nothing more.
public abstract class ModuleDocumentation {

    public static final String GENERATE = "architecture-gen";

    private static final ApplicationModules MODULES =
            ApplicationModules.of(EventingPlatform.class, new Production());

    private static Path docs() {
        return Path.of(System.getProperty("architecture.docs", "docs/generated/architecture"));
    }

    @Test
    void modulesDependOnEachOtherAsDeclared() {
        MODULES.verify();
    }

    @Test
    @Tag(GENERATE)
    void writesTheDocumentation() {
        var plantuml = docs().resolve("plantuml");
        new Documenter(MODULES, Documenter.Options.defaults().withOutputFolder(plantuml.toString()))
                .writeDocumentation(
                        DiagramOptions.defaults().withStyle(DiagramStyle.UML),
                        CanvasOptions.defaults());
        var platform = new ModelWriter(MODULES).toPlatform();
        var features =
                Stream.of(
                                System.getProperty(
                                                "architecture.features",
                                                "src/test/resources/features")
                                        .split(File.pathSeparator))
                        .filter(directory -> !directory.isBlank())
                        .map(Path::of)
                        .toList();
        new TextProtoWriter(platform, features).writeTo(docs().resolve("model"));
        new HexagonDocumenter(platform).writeTo(plantuml.resolve("hexagon.puml"));
        new ForestDocumenter(platform).writeTo(plantuml.resolve("forest.puml"));
        new StreamsDocumenter(platform).writeTo(plantuml.resolve("streams.puml"));
    }
}
