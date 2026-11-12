package dk.mathmagicians.playground.architecture.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/// A `.puml` in a directory against the SVG the renderer writes beside it.
class DiagramRendererTest {

    private static final String DIAGRAM =
            """
            @startuml
            hexagon "Purse Story" as purse
            card "KnowWhatIsLeft" as port
            purse --> port
            @enduml
            """;

    @TempDir Path sources;
    @TempDir Path rendered;

    @Test
    void rendersEveryPumlToSvg() throws IOException {
        Files.writeString(sources.resolve("hexagon.puml"), DIAGRAM);
        Files.writeString(sources.resolve("forest.puml"), DIAGRAM);
        Files.writeString(sources.resolve("all-docs.adoc"), "not a diagram");

        var written = DiagramRenderer.render(sources, rendered);

        assertThat(written)
                .containsExactly(rendered.resolve("forest.svg"), rendered.resolve("hexagon.svg"));
        assertThat(rendered.resolve("hexagon.svg"))
                .content()
                .startsWith("<svg")
                .contains("KnowWhatIsLeft");
    }

    @Test
    void rejectsAMissingSourceDirectory() {
        var missing = sources.resolve("plantuml");

        assertThatThrownBy(() -> DiagramRenderer.render(missing, rendered))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(missing.toString());
    }

    @Test
    void rejectsADiagramPlantUmlCannotDraw() throws IOException {
        Files.writeString(
                sources.resolve("broken.puml"), "@startuml\nthis is no diagram\n@enduml\n");

        assertThatThrownBy(() -> DiagramRenderer.render(sources, rendered))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("broken.puml");
    }
}
