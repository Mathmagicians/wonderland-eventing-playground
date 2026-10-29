package dk.mathmagicians.playground.architecture.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/// A specification written by hand in the compact layout against the model the build writes, with
/// the layout the parser allows either way.
class ModelCheckTest {

    private static final String SPECIFIED =
            """
            # proto-file: hexagon.proto
            # proto-message: Story

            name: "Purse Story"
            primary_port { name: "KnowWhatIsLeft"  implements: "Story" }
            subscribes { payload: "Transaction" }
            depends_on: ["Domain"]
            wiring { configuration: "PurseStory"  bean { name: "purse"  builds: "KnowWhatIsLeft" } }
            """;

    private static final String BUILT =
            """
            # proto-file: hexagon.proto
            # proto-message: Story

            name: "Purse Story"
            primary_port {
              name: "KnowWhatIsLeft"
              implements: "Story"
            }
            subscribes {
              payload: "Transaction"
            }
            depends_on: "Domain"
            wiring {
              configuration: "PurseStory"
              bean {
                name: "purse"
                builds: "KnowWhatIsLeft"
              }
            }
            """;

    @TempDir Path specifications;
    @TempDir Path models;

    private void specify(String file, String text) throws IOException {
        Files.writeString(specifications.resolve(file), text);
    }

    private void build(String file, String text) throws IOException {
        Files.writeString(models.resolve(file), text);
    }

    /// The layouts differ, the messages do not.
    @Test
    void findsNothingWhenTheBuildSaysWhatWasSpecified() throws IOException {
        specify("purse.textproto", SPECIFIED);
        build("purse.textproto", BUILT);

        var report = ModelCheck.compare(specifications, models);

        assertThat(report.isClean()).isTrue();
        assertThat(report.unspecified()).isEmpty();
    }

    /// One value apart: the finding names the file and the line each side has alone, in the
    /// writer's layout.
    @Test
    void reportsTheLinesEachSideHasAlone() throws IOException {
        specify("purse.textproto", SPECIFIED);
        build(
                "purse.textproto",
                BUILT.replace("depends_on: \"Domain\"", "depends_on: \"Platform\""));

        var report = ModelCheck.compare(specifications, models);

        assertThat(report.findings()).hasSize(1);
        var finding = report.findings().getFirst();
        assertThat(finding.file()).isEqualTo("purse.textproto");
        assertThat(finding.specifiedNotBuilt()).containsExactly("depends_on: [\"Domain\"]");
        assertThat(finding.builtNotSpecified()).containsExactly("depends_on: [\"Platform\"]");
        assertThat(finding.text())
                .startsWith("purse.textproto: the specification and the build differ\n")
                .contains("  specified, not built:\n    depends_on: [\"Domain\"]\n")
                .contains("  built, not specified:\n    depends_on: [\"Platform\"]\n");
    }

    /// A built model nobody specified is listed; a specification nothing was built for is a finding
    /// with every line specified and nothing built.
    @Test
    void listsTheUnspecifiedAndFailsTheUnbuilt() throws IOException {
        specify("purse.textproto", SPECIFIED);
        build("load.textproto", BUILT.replace("Purse Story", "Load Story"));

        var report = ModelCheck.compare(specifications, models);

        assertThat(report.unspecified()).containsExactly("load.textproto");
        assertThat(report.findings()).hasSize(1);
        assertThat(report.findings().getFirst().builtNotSpecified()).isEmpty();
        assertThat(report.findings().getFirst().specifiedNotBuilt())
                .contains("name: \"Purse Story\"");
    }

    /// No specification directory: nothing specified, every model unspecified, clean.
    @Test
    void isCleanWithoutASpecificationDirectory() throws IOException {
        build("platform.textproto", "# proto-message: Platform\nname: \"Wonderland\"\n");

        var report = ModelCheck.compare(specifications.resolve("none"), models);

        assertThat(report.isClean()).isTrue();
        assertThat(report.unspecified()).containsExactly("platform.textproto");
    }

    @Test
    void rejectsAFileNamingNoMessage() throws IOException {
        specify("purse.textproto", "name: \"Purse Story\"\n");

        assertThatThrownBy(() -> ModelCheck.compare(specifications, models))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("names no message");
    }
}
