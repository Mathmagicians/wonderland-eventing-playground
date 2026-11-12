package dk.mathmagicians.playground.architecture.discovery;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantuml.preproc.Defines;

/// The `.puml` sources of a directory as SVG, one file per diagram. PlantUML reads
/// `hexagon.style.puml`, the look, from the class path as its config before each diagram, and lays
/// out with smetana, its built-in engine, so Graphviz is not needed.
public final class DiagramRenderer {

    private static final String STYLE = "/hexagon.style.puml";
    private static final String LAYOUT = "!pragma layout smetana";
    private static final String SOURCE = ".puml";

    private DiagramRenderer() {}

    /// `DiagramRenderer <puml directory> <svg directory>`: renders headless, prints each SVG
    /// written, and exits with 1 when a source directory is missing or a diagram cannot be drawn.
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("usage: DiagramRenderer <puml directory> <svg directory>");
            System.exit(2);
        }
        System.setProperty("java.awt.headless", "true");
        try {
            render(Path.of(args[0]), Path.of(args[1])).forEach(System.out::println);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /// Every `.puml` directly under the sources rendered into the directory, created when missing;
    /// the SVG files written, sorted.
    public static List<Path> render(Path sources, Path rendered) {
        if (!Files.isDirectory(sources)) {
            throw new IllegalArgumentException(
                    "no directory of .puml sources at " + sources.toAbsolutePath());
        }
        var config = config();
        var written = new ArrayList<Path>();
        for (var source : pumls(sources)) {
            written.addAll(render(source, rendered, config));
        }
        return written.stream().sorted().toList();
    }

    private static List<Path> render(Path source, Path rendered, List<String> config) {
        try {
            var reader =
                    new SourceFileReader(
                            false,
                            Defines.createWithFileName(source.toFile()),
                            source.toFile(),
                            rendered.toFile(),
                            config,
                            StandardCharsets.UTF_8.name(),
                            new FileFormatOption(FileFormat.SVG));
            var images = reader.getGeneratedImages();
            if (reader.hasError() || images.isEmpty()) {
                throw new IllegalStateException("PlantUML cannot draw " + source + reason(images));
            }
            return images.stream().map(image -> image.getPngFile().toPath()).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("cannot render " + source, e);
        }
    }

    /// The line PlantUML stopped at, when it names one.
    private static String reason(List<GeneratedImage> images) {
        return images.stream()
                .filter(image -> image.lineErrorRaw() >= 0)
                .findFirst()
                .map(image -> ": line " + (image.lineErrorRaw() + 1))
                .orElse("");
    }

    /// What PlantUML reads before each diagram: the style's lines, then the layout engine.
    private static List<String> config() {
        try (var style = DiagramRenderer.class.getResourceAsStream(STYLE)) {
            if (style == null) {
                throw new IllegalStateException(STYLE + " is not on the class path");
            }
            var lines =
                    new ArrayList<>(
                            new String(style.readAllBytes(), StandardCharsets.UTF_8)
                                    .lines()
                                    .toList());
            lines.add(LAYOUT);
            return List.copyOf(lines);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + STYLE, e);
        }
    }

    private static List<Path> pumls(Path sources) {
        try (Stream<Path> files = Files.list(sources)) {
            return files.filter(file -> file.getFileName().toString().endsWith(SOURCE))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("cannot list " + sources, e);
        }
    }
}
