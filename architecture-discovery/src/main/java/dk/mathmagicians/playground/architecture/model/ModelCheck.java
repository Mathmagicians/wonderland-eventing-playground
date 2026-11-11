package dk.mathmagicians.playground.architecture.model;

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/// The model as specified against the model as built. For every `.textproto` under the
/// specification directory, the file of the same name under the model directory; both are parsed as
/// the message their header names, `Story` or `Platform`, and compared as messages, so the layout
/// of either does not count, only what it says. A difference is reported as the lines of the
/// canonical text one side has and the other has not. A built model without a specification is
/// listed, not a failure: it is not specified yet. A specification without a built model is a
/// failure: what was specified is not built.
public final class ModelCheck {

    /// What a comparison found: the file, the lines specified and not built, the lines built and
    /// not specified.
    public record Finding(
            String file, List<String> specifiedNotBuilt, List<String> builtNotSpecified) {

        /// The finding as the report prints it.
        public String text() {
            var out = new StringBuilder(file + ": the specification and the build differ\n");
            if (!specifiedNotBuilt.isEmpty()) {
                out.append("  specified, not built:\n");
                specifiedNotBuilt.forEach(line -> out.append("    ").append(line).append('\n'));
            }
            if (!builtNotSpecified.isEmpty()) {
                out.append("  built, not specified:\n");
                builtNotSpecified.forEach(line -> out.append("    ").append(line).append('\n'));
            }
            return out.toString();
        }
    }

    /// The outcome over a directory: the findings, and the built models nobody specified.
    public record Report(List<Finding> findings, List<String> unspecified) {

        public boolean isClean() {
            return findings.isEmpty();
        }
    }

    private static final String EXTENSION = TextProtoWriter.EXTENSION;
    private static final Pattern MESSAGE =
            Pattern.compile("^# proto-message: (\\w+)$", Pattern.MULTILINE);

    private ModelCheck() {}

    /// `ModelCheck <specification directory> <model directory>`: prints the report and exits with 1
    /// when the specification and the build differ.
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("usage: ModelCheck <specification directory> <model directory>");
            System.exit(2);
        }
        var report = compare(Path.of(args[0]), Path.of(args[1]));
        report.findings().forEach(finding -> System.out.print(finding.text()));
        if (!report.unspecified().isEmpty()) {
            System.out.println(
                    "built, not specified under "
                            + args[0]
                            + ": "
                            + String.join(", ", report.unspecified()));
        }
        if (report.isClean()) {
            System.out.println("the model as built is the model as specified under " + args[0]);
        } else {
            System.exit(1);
        }
    }

    public static Report compare(Path specifications, Path models) {
        var specified = textProtos(specifications);
        var built = textProtos(models);
        var findings = new ArrayList<Finding>();
        for (var file : specified) {
            var specification = specifications.resolve(file);
            var model = models.resolve(file);
            if (!Files.exists(model)) {
                findings.add(new Finding(file, lines(parse(specification)), List.of()));
                continue;
            }
            compare(file, parse(specification), parse(model)).ifPresent(findings::add);
        }
        var unspecified = built.stream().filter(file -> !specified.contains(file)).toList();
        return new Report(findings, unspecified);
    }

    /// Nothing when the two say the same; else the lines of the canonical text each has alone.
    static java.util.Optional<Finding> compare(String file, Message specified, Message built) {
        if (specified.equals(built)) {
            return java.util.Optional.empty();
        }
        var specifiedLines = lines(specified);
        var builtLines = lines(built);
        return java.util.Optional.of(
                new Finding(
                        file,
                        specifiedLines.stream().filter(line -> !builtLines.contains(line)).toList(),
                        builtLines.stream()
                                .filter(line -> !specifiedLines.contains(line))
                                .toList()));
    }

    /// The file parsed as the message its header names, comments and layout as the text format
    /// allows them.
    static Message parse(Path file) {
        String text;
        try {
            text = Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + file, e);
        }
        var header = MESSAGE.matcher(text);
        if (!header.find()) {
            throw new IllegalArgumentException(
                    file + " names no message: '# proto-message: Story' or 'Platform'");
        }
        var builder =
                switch (header.group(1)) {
                    case "Story" -> Story.newBuilder();
                    case "Platform" -> Platform.newBuilder();
                    default ->
                            throw new IllegalArgumentException(
                                    file
                                            + " names the message "
                                            + header.group(1)
                                            + ", not Story or Platform");
                };
        try {
            TextFormat.merge(text, builder);
        } catch (TextFormat.ParseException e) {
            throw new IllegalArgumentException(file + " does not parse: " + e.getMessage(), e);
        }
        return builder.build();
    }

    /// The message in the writer's layout, its lines trimmed, so a finding reads as the files do.
    static List<String> lines(Message message) {
        return TextProtoWriter.text(message)
                .lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .toList();
    }

    /// The `.textproto` file names under the directory, sorted; none when the directory is not
    /// there.
    private static Set<String> textProtos(Path directory) {
        if (!Files.isDirectory(directory)) {
            return Set.of();
        }
        try (Stream<Path> files = Files.list(directory)) {
            return files.map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(EXTENSION))
                    .sorted()
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        } catch (IOException e) {
            throw new UncheckedIOException("cannot list " + directory, e);
        }
    }
}
