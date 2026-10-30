package dk.mathmagicians.playground.architecture.discovery;

import dk.mathmagicians.playground.architecture.model.Platform;
import dk.mathmagicians.playground.architecture.model.Publishes;
import dk.mathmagicians.playground.architecture.model.Stories;
import dk.mathmagicians.playground.architecture.model.Subscribes;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.stream.Stream;

/// Level 2, the streams: the stories with what they publish and subscribe to, as PlantUML, content
/// alone, the look `hexagon.style.puml`'s. Every payload a story publishes or subscribes to is a
/// queue, the topic it travels on, sorted by name; every story a hexagon, in the model's order; an
/// arrow from a story into a queue says `publishes`, one out of a queue into a story `subscribes`.
/// A story that neither publishes nor subscribes stands alone. The same model gives the same text.
public final class StreamsDocumenter {

    private static final String PUBLISHES = "publishes";
    private static final String SUBSCRIBES = "subscribes";

    private final Platform platform;

    public StreamsDocumenter(Platform platform) {
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
        var stories = Stories.of(platform);
        var payloads = new TreeSet<String>();
        for (var story : stories) {
            story.getSubscribesList().stream().map(Subscribes::getPayload).forEach(payloads::add);
            story.getPublishesList().stream().map(Publishes::getPayload).forEach(payloads::add);
        }
        var out = new ArrayList<String>();
        out.add("@startuml");
        out.add(
                "' the streams from the model: what each story publishes and subscribes to, a queue per"
                        + " payload");
        out.add("");
        payloads.forEach(payload -> out.add("queue \"" + payload + "\" as " + queue(payload)));
        for (var story : stories) {
            out.add("hexagon \"" + story.getName() + "\" as " + ForestDocumenter.alias(story));
        }
        out.add("");
        for (var story : stories) {
            var alias = ForestDocumenter.alias(story);
            Stream.concat(
                            story.getSubscribesList().stream()
                                    .map(Subscribes::getPayload)
                                    .map(
                                            payload ->
                                                    queue(payload)
                                                            + " --> "
                                                            + alias
                                                            + " : "
                                                            + SUBSCRIBES),
                            story.getPublishesList().stream()
                                    .map(Publishes::getPayload)
                                    .map(
                                            payload ->
                                                    alias
                                                            + " --> "
                                                            + queue(payload)
                                                            + " : "
                                                            + PUBLISHES))
                    .forEach(out::add);
        }
        out.add("@enduml");
        return String.join("\n", out) + "\n";
    }

    private static String queue(String payload) {
        return "q_" + payload.replaceAll("[^A-Za-z0-9]", "_");
    }
}
