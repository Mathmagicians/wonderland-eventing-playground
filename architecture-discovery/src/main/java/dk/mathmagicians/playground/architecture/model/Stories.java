package dk.mathmagicians.playground.architecture.model;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/// The stories of a platform: every module with a `PrimaryPort` type that implements `Story`, each
/// as its Story, in the hexagon's order. The module's name and package are the story's; its
/// `PrimaryPort` types are the primary ports, each with what it implements; its `SecondaryPort`
/// types the secondary ports, each with the types whose arrows implement it; the driving port's
/// `subscribes` and `publishes` arrows are what the story subscribes to and publishes, the
/// application port it uses what it publishes through; the modules its arrows reach are what it
/// depends on; its other stereotyped types are its types. The wiring is read from the module's
/// Spring annotations: the `AutoConfiguration` type is the configuration, the
/// `ConfigurationProperties` types it enables are the settings with their prefix, its `Bean`
/// methods the beans with what they build and the parameters they build from. The features are the
/// `.feature` files under the directories given: a behaviour feature belongs to the story whose
/// build it lives in, the project named as the story or the one story of a client's build; one that
/// lives elsewhere says which story it drives, `@story:load`; an architecture feature is nobody's.
/// What the platform does not carry stays absent: `through` when the port uses no application port
/// or more than one, the features when no directory is given.
public final class Stories {

    static final String STORY = "Story";
    static final String PRIMARY_PORT = "PrimaryPort";
    static final String SECONDARY_PORT = "SecondaryPort";
    static final String AUTO_CONFIGURATION = "AutoConfiguration";
    static final String ENABLE_CONFIGURATION_PROPERTIES = "EnableConfigurationProperties";
    static final String CONFIGURATION_PROPERTIES = "ConfigurationProperties";
    static final String BEAN = "Bean";
    static final String FEATURE = ".feature";

    /// The tag of an architecture feature, which belongs to no story's behaviour.
    static final String ARCHITECTURE = "@architecture-discovery";

    /// The tag a feature living outside its story's build carries, `@story:load`.
    static final String STORY_TAG = "@story:";

    /// A wiring entry as the writer prints it: the annotation's name, and its value in parentheses
    /// when it carries one.
    private static final Pattern WIRING = Pattern.compile("(\\w+)(?:\\((.*)\\))?");

    /// A tag line of a feature file: `@load @slow`, above its `Feature:` or a `Scenario:` line.
    private static final Pattern TAGS = Pattern.compile("^\\s*(@\\S+\\s*)+$");

    private Stories() {}

    /// The stories without their features.
    public static List<Story> of(Platform platform) {
        return platform.getModuleList().stream()
                .flatMap(
                        module ->
                                implementing(platform, module).stream()
                                        .map(port -> story(platform, module, port)))
                .toList();
    }

    /// The stories with their features under the directory.
    public static List<Story> of(Platform platform, Path features) {
        return of(platform, List.of(features));
    }

    /// The stories with their features under the directories: a feature tagged with a story's name
    /// is that story's; an untagged behaviour feature is the one story's when there is one.
    public static List<Story> of(Platform platform, List<Path> directories) {
        var stories = of(platform);
        var names = stories.stream().map(TextProtoWriter::fileName).toList();
        var files = featureFiles(directories);
        return stories.stream()
                .map(
                        story ->
                                story.toBuilder()
                                        .addAllFeature(
                                                features(
                                                        files,
                                                        TextProtoWriter.fileName(story),
                                                        names))
                                        .build())
                .toList();
    }

    /// The arrow from a PrimaryPort of the module to Story, when the module is a story.
    private static Optional<Arrow> implementing(Platform platform, Module module) {
        return platform.getArrowList().stream()
                .filter(arrow -> arrow.getRelation() == Relation.IMPLEMENTS)
                .filter(arrow -> arrow.getFrom().getModule().equals(module.getName()))
                .filter(arrow -> arrow.getTo().getType().equals(STORY))
                .filter(arrow -> hasStereotype(module, arrow.getFrom().getType(), PRIMARY_PORT))
                .findFirst();
    }

    private static Story story(Platform platform, Module module, Arrow implementing) {
        var port = implementing.getFrom();
        var story =
                Story.newBuilder().setName(module.getName()).setBasePackage(module.getPackage());
        for (var type : module.getTypeList()) {
            if (type.getStereotypeList().contains(PRIMARY_PORT)) {
                story.addPrimaryPort(port(platform, module, type));
            } else if (type.getStereotypeList().contains(SECONDARY_PORT)) {
                story.addSecondaryPort(port(platform, module, type));
            } else if (type.getStereotypeCount() > 0) {
                story.addType(
                        Type.newBuilder()
                                .setName(type.getName())
                                .addAllStereotype(type.getStereotypeList()));
            }
        }
        var fromPort =
                platform.getArrowList().stream()
                        .filter(arrow -> arrow.getFrom().equals(port))
                        .toList();
        var through = through(platform, fromPort);
        for (var arrow : fromPort) {
            switch (arrow.getRelation()) {
                case SUBSCRIBES -> story.addSubscribesBuilder().setPayload(arrow.getTo().getType());
                case PUBLISHES -> {
                    var publishes = story.addPublishesBuilder().setPayload(arrow.getTo().getType());
                    through.ifPresent(publishes::setThrough);
                }
                default -> {}
            }
        }
        platform.getArrowList().stream()
                .filter(arrow -> arrow.getFrom().getModule().equals(module.getName()))
                .map(arrow -> arrow.getTo().getModule())
                .filter(name -> !name.equals(module.getName()))
                .distinct()
                .forEach(story::addDependsOn);
        wiring(module).ifPresent(story::setWiring);
        return story.build();
    }

    /// A port: its name, what it implements, the target of its implements arrow out of the module,
    /// and what implements it, the sources of the implements arrows into it.
    private static Port port(Platform platform, Module module, Type type) {
        var end = End.newBuilder().setModule(module.getName()).setType(type.getName()).build();
        var port = Port.newBuilder().setName(type.getName());
        platform.getArrowList().stream()
                .filter(arrow -> arrow.getRelation() == Relation.IMPLEMENTS)
                .filter(arrow -> arrow.getFrom().equals(end))
                .map(arrow -> arrow.getTo().getType())
                .findFirst()
                .ifPresent(port::setImplements);
        platform.getArrowList().stream()
                .filter(arrow -> arrow.getRelation() == Relation.IMPLEMENTS)
                .filter(arrow -> arrow.getTo().equals(end))
                .map(arrow -> arrow.getFrom().getType())
                .forEach(port::addImplementedBy);
        return port.build();
    }

    /// The one PrimaryPort the port uses, `PublishMessage`; nothing when there is none or several.
    private static Optional<String> through(Platform platform, List<Arrow> fromPort) {
        var used =
                fromPort.stream()
                        .filter(arrow -> arrow.getRelation() == Relation.USES)
                        .map(Arrow::getTo)
                        .filter(to -> hasStereotype(platform, to, PRIMARY_PORT))
                        .map(End::getType)
                        .distinct()
                        .toList();
        return used.size() == 1 ? Optional.of(used.getFirst()) : Optional.empty();
    }

    private static boolean hasStereotype(Platform platform, End end, String stereotype) {
        return platform.getModuleList().stream()
                .filter(module -> module.getName().equals(end.getModule()))
                .anyMatch(module -> hasStereotype(module, end.getType(), stereotype));
    }

    private static boolean hasStereotype(Module module, String type, String stereotype) {
        return module.getTypeList().stream()
                .filter(candidate -> candidate.getName().equals(type))
                .anyMatch(candidate -> candidate.getStereotypeList().contains(stereotype));
    }

    /// The module's wiring, when it has an auto-configuration: the settings it enables, with the
    /// prefix each binds, and its bean methods, the entries named `<configuration>.<method>`.
    private static Optional<Wiring> wiring(Module module) {
        return module.getTypeList().stream()
                .filter(type -> values(type, AUTO_CONFIGURATION).findAny().isPresent())
                .findFirst()
                .map(
                        configuration -> {
                            var wiring =
                                    Wiring.newBuilder().setConfiguration(configuration.getName());
                            values(configuration, ENABLE_CONFIGURATION_PROPERTIES)
                                    .flatMap(value -> Stream.of(value.split(",\\s*")))
                                    .forEach(
                                            settings ->
                                                    wiring.addSettings(settings(module, settings)));
                            module.getTypeList().stream()
                                    .filter(
                                            type ->
                                                    type.getName()
                                                            .startsWith(
                                                                    configuration.getName() + "."))
                                    .forEach(
                                            method ->
                                                    values(method, BEAN)
                                                            .findFirst()
                                                            .ifPresent(
                                                                    builds ->
                                                                            wiring.addBean(
                                                                                    Bean
                                                                                            .newBuilder()
                                                                                            .setName(
                                                                                                    method(
                                                                                                            method))
                                                                                            .setBuilds(
                                                                                                    builds)
                                                                                            .addAllFrom(
                                                                                                    method
                                                                                                            .getParameterList()))));
                            return wiring.build();
                        });
    }

    private static Settings settings(Module module, String type) {
        var settings = Settings.newBuilder().setType(type);
        module.getTypeList().stream()
                .filter(candidate -> candidate.getName().equals(type))
                .flatMap(candidate -> values(candidate, CONFIGURATION_PROPERTIES))
                .findFirst()
                .ifPresent(settings::setPrefix);
        return settings.build();
    }

    /// The values of the type's wiring entries with the annotation given, an empty string for an
    /// entry without one.
    private static Stream<String> values(Type type, String annotation) {
        return type.getWiringList().stream()
                .map(WIRING::matcher)
                .filter(Matcher::matches)
                .filter(matcher -> matcher.group(1).equals(annotation))
                .map(matcher -> matcher.group(2) == null ? "" : matcher.group(2));
    }

    /// The method's name from its entry, `purse` from `PurseStory.purse`.
    private static String method(Type entry) {
        return entry.getName().substring(entry.getName().lastIndexOf('.') + 1);
    }

    /// The `.feature` files under the directories that are there.
    private static List<Path> featureFiles(List<Path> directories) {
        return directories.stream()
                .filter(Files::isDirectory)
                .flatMap(
                        directory -> {
                            try (Stream<Path> files = Files.list(directory)) {
                                return files
                                        .filter(
                                                file ->
                                                        file.getFileName()
                                                                .toString()
                                                                .endsWith(FEATURE))
                                        .toList()
                                        .stream();
                            } catch (IOException e) {
                                throw new UncheckedIOException("cannot list " + directory, e);
                            }
                        })
                .toList();
    }

    /// The story's features by name, sorted. An architecture feature is nobody's. A feature that
    /// says which story it drives, `@story:load`, is that story's. Any other belongs to the story
    /// whose build it lives in: the project named as the story, `stories/tea-party`, or the one
    /// story of a client's build.
    private static List<String> features(List<Path> files, String story, List<String> stories) {
        var sole = stories.size() == 1;
        return files.stream()
                .filter(
                        file -> {
                            var tags = tags(file);
                            if (tags.contains(ARCHITECTURE)) {
                                return false;
                            }
                            var said =
                                    tags.stream()
                                            .filter(tag -> tag.startsWith(STORY_TAG))
                                            .map(tag -> tag.substring(STORY_TAG.length()))
                                            .toList();
                            if (!said.isEmpty()) {
                                return said.contains(story);
                            }
                            return project(file).equals(story) || sole;
                        })
                .map(file -> file.getFileName().toString())
                .sorted()
                .distinct()
                .toList();
    }

    /// The name of the project a feature lives in, the directory above its
    /// `src/test/resources/features`; empty when the file is not under such a path.
    private static String project(Path file) {
        var project = file.toAbsolutePath();
        for (var i = 0; i < 5 && project != null; i++) {
            project = project.getParent();
        }
        return project == null || project.getFileName() == null
                ? ""
                : project.getFileName().toString();
    }

    /// The tags on the file's tag lines, `@load`, `@slow`.
    private static List<String> tags(Path file) {
        try {
            return Files.readAllLines(file).stream()
                    .filter(line -> TAGS.matcher(line).matches())
                    .flatMap(line -> Stream.of(line.strip().split("\\s+")))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + file, e);
        }
    }
}
