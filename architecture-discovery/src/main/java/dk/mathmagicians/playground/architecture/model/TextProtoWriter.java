package dk.mathmagicians.playground.architecture.model;

import static java.util.stream.Collectors.joining;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/// The model as text format, in the layout the specifications are written in: two header lines
/// naming the schema and the message, a blank line, then the fields in the schema's order, a field
/// at its default left out. A scalar field takes a line, a repeated one as a list, `depends_on:
/// ["Application", "Domain"]`. A message field takes one line when it fits in the width, its fields
/// two spaces apart and a message inside it the same way, `arrow { from { module: "Cli" type:
/// "Runner" } to { … } relation: USES }`; otherwise its fields follow one per line, indented by
/// two, and the brace closes on a line of its own. The same model gives the same text. Under a
/// directory, the platform is `platform.textproto` and each of its stories is named as `--story`
/// names it: the module's name without a trailing "Story", in lower case, its words joined by
/// hyphens, `tea-party.textproto` from "Tea Party Story".
public final class TextProtoWriter {

    public static final String EXTENSION = ".textproto";
    static final String PROTO_FILE = "hexagon.proto";
    static final String PLATFORM = "platform";

    /// The columns a message field may take on one line: an arrow with its two ends fits.
    static final int WIDTH = 150;

    private static final String INDENT = "  ";
    private static final String BETWEEN = "  ";
    private static final TextFormat.Printer PRINTER = TextFormat.printer();

    private final Platform platform;
    private final List<Path> features;

    /// The platform, and the directories of the features its stories are found in, see `Stories`; a
    /// directory that is not there gives no features.
    public TextProtoWriter(Platform platform, List<Path> features) {
        this.platform = platform;
        this.features = features;
    }

    public static String toText(Message message) {
        return "# proto-file: "
                + PROTO_FILE
                + "\n# proto-message: "
                + message.getDescriptorForType().getName()
                + "\n\n"
                + text(message);
    }

    public static String fileName(Story story) {
        var name = story.getName().strip().replaceFirst("(?i)\\s*\\bstory$", "");
        return String.join("-", name.toLowerCase(Locale.ROOT).split("\\s+"));
    }

    /// Writes the platform and each of its stories into the directory, creating it.
    public void writeTo(Path dir) {
        write(dir.resolve(PLATFORM + EXTENSION), platform);
        Stories.of(platform, features)
                .forEach(story -> write(dir.resolve(fileName(story) + EXTENSION), story));
    }

    private static void write(Path file, Message message) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, toText(message));
        } catch (IOException e) {
            throw new UncheckedIOException("cannot write " + file, e);
        }
    }

    /// The message's fields in this layout, without the header: what the check reports with.
    static String text(Message message) {
        return fields(message, "");
    }

    /// The message's set fields, one per line at the indent.
    private static String fields(Message message, String indent) {
        var out = new StringBuilder();
        for (var entry : message.getAllFields().entrySet()) {
            var field = entry.getKey();
            if (field.getJavaType() != JavaType.MESSAGE) {
                out.append(indent).append(scalars(field, entry.getValue())).append('\n');
                continue;
            }
            for (var value : values(field, entry.getValue())) {
                out.append(indent).append(nested(field, (Message) value, indent)).append('\n');
            }
        }
        return out.toString();
    }

    /// A scalar field, `name: "Purse Story"`; a repeated one as a list, `depends_on: ["A", "B"]`.
    private static String scalars(FieldDescriptor field, Object value) {
        if (!field.isRepeated()) {
            return field.getName() + ": " + scalar(field, value);
        }
        return field.getName()
                + ": ["
                + values(field, value).stream()
                        .map(each -> scalar(field, each))
                        .collect(joining(", "))
                + "]";
    }

    /// A message field on one line when it fits in the width, else opened over several.
    private static String nested(FieldDescriptor field, Message message, String indent) {
        var oneLine = oneLine(message).map(inner -> field.getName() + " { " + inner + " }");
        if (oneLine.isPresent() && indent.length() + oneLine.get().length() <= WIDTH) {
            return oneLine.get();
        }
        return field.getName() + " {\n" + fields(message, indent + INDENT) + indent + "}";
    }

    /// The message's fields on one line, two spaces apart, a message among them the same way;
    /// nothing when one of those cannot be.
    private static Optional<String> oneLine(Message message) {
        var parts = new ArrayList<String>();
        for (var entry : message.getAllFields().entrySet()) {
            var field = entry.getKey();
            if (field.getJavaType() != JavaType.MESSAGE) {
                parts.add(scalars(field, entry.getValue()));
                continue;
            }
            for (var value : values(field, entry.getValue())) {
                var inner = oneLine((Message) value);
                if (inner.isEmpty()) {
                    return Optional.empty();
                }
                parts.add(field.getName() + " { " + inner.get() + " }");
            }
        }
        return Optional.of(String.join(BETWEEN, parts));
    }

    private static List<?> values(FieldDescriptor field, Object value) {
        return field.isRepeated() ? (List<?>) value : List.of(value);
    }

    /// A scalar as the text format writes it, a string in double quotes with a backslash, a double
    /// quote, and a line break escaped and an apostrophe as it is, an enum by its name.
    private static String scalar(FieldDescriptor field, Object value) {
        if (field.getJavaType() == JavaType.STRING) {
            return quoted((String) value);
        }
        var out = new StringBuilder();
        try {
            PRINTER.printFieldValue(field, value, out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toString();
    }

    private static String quoted(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
