package dk.mathmagicians.playground.architecture.discovery;

import dk.mathmagicians.playground.architecture.model.Platform;
import dk.mathmagicians.playground.architecture.model.Stories;
import dk.mathmagicians.playground.architecture.model.Story;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

/// Level 1, the forest: the landscape as PlantUML, content alone, the look `hexagon.style.puml`'s.
/// The platform is one hexagon, named as the model names the system; every story is a hexagon
/// beside it, its name above its package, with an arrow to the platform, `plays on`. The stories
/// come in the model's order, so the same model gives the same text.
public final class ForestDocumenter {

    private static final String PLATFORM = "platform";
    private static final String PLAYS_ON = "plays on";

    private final Platform platform;

    public ForestDocumenter(Platform platform) {
        this.platform = platform;
    }

    /// Writes `toPlantUml()` to the file, creating its directories.
    public void writeTo(Path file) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, toPlantUml());
        } catch (IOException e) {
            throw new UncheckedIOException("cannot write " + file, e);
        }
    }

    public String toPlantUml() {
        var out = new ArrayList<String>();
        out.add("@startuml");
        out.add("' the forest from the model: the platform and the stories that play on it");
        out.add("");
        out.add("hexagon \"" + platform.getName() + "\\n" + PLATFORM + "\" as " + PLATFORM);
        var stories = Stories.of(platform);
        for (var story : stories) {
            out.add(
                    "hexagon \""
                            + story.getName()
                            + "\\n"
                            + packageLine(story)
                            + "\" as "
                            + alias(story));
        }
        out.add("");
        for (var story : stories) {
            out.add(alias(story) + " --> " + PLATFORM + " : " + PLAYS_ON);
        }
        out.add("@enduml");
        return String.join("\n", out) + "\n";
    }

    /// The story's alias, its name without anything PlantUML would read as syntax.
    static String alias(Story story) {
        return "s_" + story.getName().replaceAll("[^A-Za-z0-9]", "_");
    }

    /// The story's package as the title's second line: its last two segments, `stories/purse`.
    static String packageLine(Story story) {
        var segments = story.getBasePackage().split("\\.");
        var from = Math.max(0, segments.length - 2);
        return String.join("/", Arrays.asList(segments).subList(from, segments.length));
    }
}
