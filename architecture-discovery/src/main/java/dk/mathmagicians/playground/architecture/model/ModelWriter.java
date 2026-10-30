package dk.mathmagicians.playground.architecture.model;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.ReferencedClassObject;
import com.tngtech.archunit.core.domain.properties.HasAnnotations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.jmolecules.stereotype.api.Stereotype;
import org.jmolecules.stereotype.api.StereotypeFactory;
import org.jmolecules.stereotype.catalog.StereotypeCatalog;
import org.jmolecules.stereotype.catalog.StereotypeDefinition;
import org.jmolecules.stereotype.catalog.support.CatalogSource;
import org.jmolecules.stereotype.catalog.support.JsonPathStereotypeCatalog;
import org.jmolecules.stereotype.reflection.ArchUnitStereotypeFactory;
import org.jmolecules.stereotype.reflection.ReflectionStereotypeFactory;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModuleDependency;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.DependencyDepth;
import org.springframework.modulith.core.DependencyType;

/// The hexagon of an application read from its classes into the model, once. Spring Modulith's
/// application modules sit in their rings by the jMolecules stereotypes of their types, their own
/// or their package's, in the hexagon's order: the driving adapters, the application with the
/// domain after it, the driven adapters, the adapters both sides share. A module carries its
/// package's jMolecules annotations as stereotypes, `Module`, `PrimaryAdapter`, and its Spring ones
/// as wiring. A type with a role or a Spring annotation is an entry: its role as its stereotype,
/// `PrimaryPort`, its Spring annotations with their value as its wiring, `Profile(cli)`; a method
/// or constructor with one is an entry of its own, `PurseStory.purse` with `Bean(KnowWhatIsLeft)`
/// and the types of its parameters; the permitted types of a sealed type follow it. The arrows:
/// from an adapter type into the application and the shared adapters, between the application's
/// types and the domain's, and from a permitted type to its sealed type, `uses` where one holds the
/// other, `implements` where one is of the other's kind, `depends on` for any other reference.
/// Everything is sorted, so the same code gives the same model.
public final class ModelWriter {

    /// The group of the jMolecules hexagonal stereotypes, as their catalog names it.
    static final String HEXAGONAL = "architecture.hexagonal";

    private static final String PRIMARY_ADAPTER = HEXAGONAL + ".PrimaryAdapter";
    private static final String SECONDARY_ADAPTER = HEXAGONAL + ".SecondaryAdapter";
    private static final String ADAPTER = HEXAGONAL + ".Adapter";
    private static final String PACKAGE_INFO = "package-info";

    /// The annotations that are architecture: Spring's wiring on a type, its methods, its
    /// constructors, or its package; jMolecules' on a package, since on a type they are the roles.
    private static final String SPRING = "org.springframework.";

    private static final String JMOLECULES = "org.jmolecules.";

    /// The bean method, whose stereotype names what it builds.
    private static final String BEAN = "org.springframework.context.annotation.Bean";

    /// The role of a story's driving port, and the method on it that names the payloads it
    /// subscribes to, Wonderland's `Story.subscribesTo()`: the class objects it references.
    private static final String PRIMARY_PORT = "PrimaryPort";

    private static final String SUBSCRIBES_TO = "subscribesTo";

    /// Its twin, the method that names the payloads the port publishes, `Story.publishes()`.
    private static final String PUBLISHES = "publishes";

    private final ApplicationModules modules;
    private final StereotypeCatalog catalog;
    private final StereotypeFactory<JavaPackage, JavaClass, JavaMethod> stereotypes;

    /// Stereotypes from every catalog on the class path, the `META-INF/jmolecules-stereotypes.json`
    /// files.
    public ModelWriter(ApplicationModules modules) {
        this(
                modules,
                new JsonPathStereotypeCatalog(
                        CatalogSource.ofClassLoader(ModelWriter.class.getClassLoader())));
    }

    ModelWriter(ApplicationModules modules, JsonPathStereotypeCatalog catalog) {
        this.modules = modules;
        this.catalog = catalog;
        this.stereotypes = new ArchUnitStereotypeFactory(new ReflectionStereotypeFactory(catalog));
    }

    public Platform toPlatform() {
        var rings = new EnumMap<Ring, List<ApplicationModule>>(Ring.class);
        modules.stream()
                .sorted(Comparator.comparing(ApplicationModule::getDisplayName))
                .forEach(
                        module ->
                                rings.computeIfAbsent(ring(module), _ -> new ArrayList<>())
                                        .add(module));
        var platform = Platform.newBuilder().setName(systemName());
        for (var ring : ordered(rings)) {
            for (var module : rings.getOrDefault(ring, List.of())) {
                platform.addModule(module(module, ring));
            }
        }
        arrows(platform, rings);
        return platform.build();
    }

    /// The rings in the hexagon's order; the domain follows the first application module, inside
    /// which it is drawn.
    private static List<Ring> ordered(Map<Ring, List<ApplicationModule>> rings) {
        return List.of(Ring.DRIVING, Ring.APPLICATION, Ring.DOMAIN, Ring.DRIVEN, Ring.SHARED);
    }

    /// Modulith's system name, `@Modulithic(systemName)`, or the last segment of the root package.
    private String systemName() {
        return modules.getSystemName()
                .orElseGet(
                        () -> {
                            var root = modules.getRootPackages().getFirst().getName();
                            return root.substring(root.lastIndexOf('.') + 1);
                        });
    }

    /// The module: its name, its package, its ring, its package's annotations, and its entries, the
    /// hexagonal roles first by role, then the other roles by role, then the types with Spring
    /// annotations alone, each type followed by its annotated methods and, when it is sealed, by
    /// its permitted types.
    private Module module(ApplicationModule module, Ring ring) {
        var builder =
                Module.newBuilder()
                        .setName(module.getDisplayName())
                        .setPackage(module.getBasePackage().getName())
                        .setRing(ring);
        module.getBasePackage().stream()
                .filter(type -> type.getSimpleName().equals(PACKAGE_INFO))
                .findFirst()
                .ifPresent(
                        info -> {
                            stereotypes(info).sorted().forEach(builder::addStereotype);
                            wiring(info).sorted().forEach(builder::addWiring);
                        });
        var hexagonal = new TreeMap<String, List<JavaClass>>();
        var others = new TreeMap<String, List<JavaClass>>();
        var wired = new ArrayList<JavaClass>();
        var permitted = permittedTypes(module);
        types(module)
                .filter(type -> !permitted.contains(type.getName()))
                .forEach(
                        type -> {
                            var roles = roles(type);
                            if (roles.isEmpty()) {
                                if (isAnnotated(type)) {
                                    wired.add(type);
                                }
                                return;
                            }
                            var group = isHexagonal(type) ? hexagonal : others;
                            group.computeIfAbsent(roles.getFirst(), _ -> new ArrayList<>())
                                    .add(type);
                        });
        wired.sort(
                Comparator.comparing((JavaClass type) -> !hasBeans(type))
                        .thenComparing(JavaClass::getName));
        Stream.of(
                        hexagonal.values().stream().flatMap(List::stream),
                        others.values().stream().flatMap(List::stream),
                        wired.stream())
                .flatMap(types -> types)
                .forEach(type -> entries(builder, type));
        return builder.build();
    }

    /// The type's entry, its annotated methods' and constructors' entries, and, for a sealed type,
    /// the entries of the permitted types in its package that have a role or an annotation.
    private void entries(Module.Builder module, JavaClass type) {
        module.addType(
                Type.newBuilder()
                        .setName(type.getSimpleName())
                        .addAllStereotype(roles(type))
                        .addAllWiring(wiring(type).sorted().toList()));
        type.getCodeUnits().stream()
                .sorted(Comparator.comparing(JavaCodeUnit::getFullName))
                .forEach(
                        unit -> {
                            var wiring =
                                    unit.getAnnotations().stream()
                                            .filter(ModelWriter::isSpring)
                                            .map(annotation -> stereotype(annotation, unit))
                                            .sorted()
                                            .toList();
                            if (!wiring.isEmpty()) {
                                module.addType(
                                        Type.newBuilder()
                                                .setName(
                                                        type.getSimpleName()
                                                                + "."
                                                                + member(unit, type))
                                                .addAllWiring(wiring)
                                                .addAllParameter(
                                                        unit.getRawParameterTypes().stream()
                                                                .map(JavaClass::getSimpleName)
                                                                .toList()));
                            }
                        });
        permitted(type)
                .filter(member -> !roles(member).isEmpty() || isAnnotated(member))
                .forEach(member -> entries(module, member));
    }

    /// The types a sealed type permits in its own package, sorted by name.
    private static Stream<JavaClass> permitted(JavaClass type) {
        return type.getPermittedSubclasses().orElse(Set.of()).stream()
                .filter(member -> member.getPackageName().equals(type.getPackageName()))
                .sorted(Comparator.comparing(JavaClass::getName));
    }

    /// The arrows, sorted, one per pair of types, the strongest relation when there are several.
    /// From each adapter type into the application and shared types it depends on; a dependency on
    /// the domain from an adapter is not drawn, the domain sits inside the application and the
    /// nesting says it. From each application or domain entry to the application and domain entries
    /// it refers to. From each permitted type to its sealed type.
    private void arrows(Platform.Builder platform, Map<Ring, List<ApplicationModule>> rings) {
        var core = rings.getOrDefault(Ring.APPLICATION, List.of());
        var domain = rings.getOrDefault(Ring.DOMAIN, List.of());
        var shared = rings.getOrDefault(Ring.SHARED, List.of());
        var lines = new TreeMap<String, Relation>();
        for (var ring : List.of(Ring.DRIVING, Ring.DRIVEN, Ring.SHARED)) {
            for (var module : rings.getOrDefault(ring, List.of())) {
                module.getDependencies(modules, DependencyDepth.IMMEDIATE).stream()
                        .filter(
                                dependency ->
                                        core.contains(dependency.getTargetModule())
                                                || shared.contains(dependency.getTargetModule()))
                        .forEach(
                                dependency ->
                                        lines.merge(
                                                key(
                                                        module,
                                                        dependency.getSourceType(),
                                                        dependency.getTargetModule(),
                                                        dependency.getTargetType()),
                                                relation(dependency),
                                                ModelWriter::strongest));
            }
        }
        var inside = Stream.concat(core.stream(), domain.stream()).toList();
        var entries =
                inside.stream()
                        .flatMap(ModelWriter::types)
                        .filter(type -> !roles(type).isEmpty() || isAnnotated(type))
                        .map(JavaClass::getName)
                        .collect(toCollection(TreeSet::new));
        for (var module : inside) {
            types(module)
                    .filter(source -> entries.contains(source.getName()))
                    .forEach(
                            source ->
                                    source.getDirectDependenciesFromSelf().stream()
                                            .map(
                                                    dependency ->
                                                            topLevel(dependency.getTargetClass()))
                                            .filter(
                                                    target ->
                                                            !target.equals(source)
                                                                    && entries.contains(
                                                                            target.getName()))
                                            .forEach(
                                                    target ->
                                                            lines.merge(
                                                                    key(
                                                                            module,
                                                                            source,
                                                                            owner(inside, target),
                                                                            target),
                                                                    relation(source, target),
                                                                    ModelWriter::strongest)));
        }
        var payloads =
                domain.stream()
                        .flatMap(module -> permittedTypes(module).stream())
                        .collect(toCollection(TreeSet::new));
        for (var module : core) {
            types(module)
                    .filter(port -> roles(port).contains(PRIMARY_PORT))
                    .forEach(
                            port -> {
                                var subscribed = declared(port, SUBSCRIBES_TO, payloads);
                                subscribed.forEach(
                                        payload ->
                                                lines.put(
                                                        key(
                                                                module,
                                                                port,
                                                                owner(inside, payload),
                                                                payload),
                                                        Relation.SUBSCRIBES));
                                declared(port, PUBLISHES, payloads)
                                        .forEach(
                                                payload ->
                                                        lines.put(
                                                                key(
                                                                        module,
                                                                        port,
                                                                        owner(inside, payload),
                                                                        payload),
                                                                Relation.PUBLISHES));
                                if (publishesThroughAPort(port)) {
                                    dependencies(port)
                                            .filter(target -> payloads.contains(target.getName()))
                                            .filter(target -> !subscribed.contains(target))
                                            .forEach(
                                                    payload ->
                                                            lines.put(
                                                                    key(
                                                                            module,
                                                                            port,
                                                                            owner(inside, payload),
                                                                            payload),
                                                                    Relation.PUBLISHES));
                                }
                            });
        }
        lines.forEach(
                (key, relation) -> {
                    var parts = key.split("\t", -1);
                    platform.addArrowBuilder()
                            .setFrom(End.newBuilder().setModule(parts[0]).setType(parts[1]))
                            .setTo(End.newBuilder().setModule(parts[2]).setType(parts[3]))
                            .setRelation(relation);
                });
    }

    /// The payloads a story's port declares in the method named, `subscribesTo()` or `publishes()`:
    /// the payload class objects it references, `Set.of(Transaction.class)`.
    private static Set<JavaClass> declared(JavaClass port, String method, Set<String> payloads) {
        return port.getMethods().stream()
                .filter(candidate -> candidate.getName().equals(method))
                .flatMap(candidate -> candidate.getReferencedClassObjects().stream())
                .map(ReferencedClassObject::getRawType)
                .filter(type -> payloads.contains(type.getName()))
                .collect(
                        toCollection(
                                () -> new TreeSet<>(Comparator.comparing(JavaClass::getName))));
    }

    /// Whether the port publishes: it holds another driving port, `PublishMessage`, in a field. The
    /// payloads it then depends on and does not subscribe to are what it publishes. The contract it
    /// implements, `Story`, a driving port too, is not what it publishes through.
    private boolean publishesThroughAPort(JavaClass port) {
        return port.getFields().stream()
                .map(field -> topLevel(field.getRawType()))
                .filter(target -> !target.equals(port))
                .anyMatch(target -> roles(target).contains(PRIMARY_PORT));
    }

    /// The top-level types the type depends on.
    private static Stream<JavaClass> dependencies(JavaClass type) {
        return type.getDirectDependenciesFromSelf().stream()
                .map(dependency -> topLevel(dependency.getTargetClass()))
                .distinct();
    }

    /// The module among the given that holds the type.
    private static ApplicationModule owner(List<ApplicationModule> modules, JavaClass type) {
        return modules.stream()
                .filter(module -> module.getBasePackage().getName().equals(type.getPackageName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(type.getName() + " is in no module"));
    }

    private static String key(
            ApplicationModule from, JavaClass source, ApplicationModule to, JavaClass target) {
        return String.join(
                "\t",
                from.getDisplayName(),
                topLevel(source).getSimpleName(),
                to.getDisplayName(),
                topLevel(target).getSimpleName());
    }

    /// The top-level type a nested one belongs to, `Recipe` inside `GenerateLoad`, whose entry an
    /// arrow ends on.
    private static JavaClass topLevel(JavaClass type) {
        var outer = type;
        while (outer.getEnclosingClass().isPresent()) {
            outer = outer.getEnclosingClass().get();
        }
        return outer;
    }

    /// The relation that says the most, in the enum's order: uses, implements, depends on.
    private static Relation strongest(Relation one, Relation other) {
        return one.getNumber() <= other.getNumber() ? one : other;
    }

    private static Relation relation(ApplicationModuleDependency dependency) {
        if (dependency.getDependencyType() == DependencyType.USES_COMPONENT) {
            return Relation.USES;
        }
        return dependency.getSourceType().isAssignableTo(dependency.getTargetType().getName())
                ? Relation.IMPLEMENTS
                : Relation.DEPENDS_ON;
    }

    /// Between two types of the hexagon: `implements` when the source is one of the target's kind,
    /// `uses` when it holds one in a field, a record component, `depends on` for a reference in a
    /// signature or a body.
    private static Relation relation(JavaClass source, JavaClass target) {
        if (source.isAssignableTo(target.getName())) {
            return Relation.IMPLEMENTS;
        }
        return source.getFields().stream().anyMatch(field -> field.getRawType().equals(target))
                ? Relation.USES
                : Relation.DEPENDS_ON;
    }

    /// Which ring a module belongs to, by the hexagonal stereotypes of its types, their own and
    /// their package's. A qualified adapter carries the unqualified `Adapter` stereotype as well,
    /// so the qualified ones decide first; the unqualified one alone is a shared adapter.
    private Ring ring(ApplicationModule module) {
        var identifiers =
                types(module)
                        .flatMap(
                                type ->
                                        Stream.concat(
                                                ownStereotypes(type), packageStereotypes(type)))
                        .map(Stereotype::getIdentifier)
                        .collect(toCollection(TreeSet::new));
        if (identifiers.contains(PRIMARY_ADAPTER)) {
            return Ring.DRIVING;
        }
        if (identifiers.contains(SECONDARY_ADAPTER)) {
            return Ring.DRIVEN;
        }
        if (identifiers.contains(ADAPTER)) {
            return Ring.SHARED;
        }
        if (identifiers.stream().anyMatch(identifier -> identifier.startsWith(HEXAGONAL + "."))) {
            return Ring.APPLICATION;
        }
        return Ring.DOMAIN;
    }

    /// The module's types, sorted by name, without the package's own class and without inner
    /// classes.
    private static Stream<JavaClass> types(ApplicationModule module) {
        return module.getBasePackage().stream()
                .filter(type -> !type.getSimpleName().equals(PACKAGE_INFO))
                .filter(type -> !type.isInnerClass() && !type.isAnonymousClass())
                .sorted(Comparator.comparing(JavaClass::getName));
    }

    /// The names of the types a sealed type of the module permits, which follow their type.
    private static Set<String> permittedTypes(ApplicationModule module) {
        return types(module)
                .filter(JavaClass::isSealed)
                .flatMap(ModelWriter::permitted)
                .map(JavaClass::getName)
                .collect(toCollection(TreeSet::new));
    }

    /// The type's role, the catalog's first by priority, as a list of one or none: its own
    /// stereotype, `PrimaryAdapter` before the `Adapter` it implies, or its package's hexagonal one
    /// when it declares none, `Reader` in a `@PrimaryAdapter` package.
    private List<String> roles(JavaClass type) {
        return ownStereotypes(type)
                .sorted()
                .findFirst()
                .or(() -> packageStereotypes(type).sorted().findFirst())
                .map(ModelWriter::role)
                .stream()
                .toList();
    }

    /// Whether the type's first role is a hexagonal one, which places it before the others.
    private boolean isHexagonal(JavaClass type) {
        return ownStereotypes(type)
                .sorted()
                .findFirst()
                .or(() -> packageStereotypes(type).sorted().findFirst())
                .filter(stereotype -> stereotype.belongsToGroup(HEXAGONAL))
                .isPresent();
    }

    /// Whether the type declares a bean method: the wiring class, listed before the settings it
    /// binds.
    private static boolean hasBeans(JavaClass type) {
        return type.getCodeUnits().stream()
                .flatMap(unit -> unit.getAnnotations().stream())
                .anyMatch(annotation -> annotation.getRawType().getName().equals(BEAN));
    }

    /// Whether the type, one of its methods, or one of its constructors carries a Spring
    /// annotation.
    private static boolean isAnnotated(JavaClass type) {
        return wiring(type).findAny().isPresent()
                || type.getCodeUnits().stream()
                        .flatMap(unit -> unit.getAnnotations().stream())
                        .anyMatch(ModelWriter::isSpring);
    }

    /// The stereotypes declared on the type itself. The factory hands a type its package's
    /// stereotypes as well, `@Module` on a `package-info` reaches every type in the package, and
    /// those are the package's role, not the type's. A stereotype is the type's own when one of the
    /// annotations the catalog assigns it by is on the type, directly or as a meta-annotation.
    private Stream<Stereotype> ownStereotypes(JavaClass type) {
        return stereotypes.fromType(type).stream()
                .filter(stereotype -> declaredOn(type, stereotype));
    }

    /// The hexagonal stereotypes declared on the type's own package, `@Adapter` on a
    /// `package-info`: the role of every top-level type in the package, as the architecture rule
    /// reads it. A parent package's do not reach down, and a nested type keeps to its top-level
    /// type's entry.
    private Stream<Stereotype> packageStereotypes(JavaClass type) {
        if (!type.isTopLevelClass()) {
            return Stream.empty();
        }
        var pkg = type.getPackage();
        return stereotypes.fromPackage(pkg).stream()
                .filter(stereotype -> stereotype.belongsToGroup(HEXAGONAL))
                .filter(stereotype -> declaredOn(pkg, stereotype));
    }

    private boolean declaredOn(HasAnnotations<?> target, Stereotype stereotype) {
        return catalog.getDefinition(stereotype).getAssignments().stream()
                .map(StereotypeDefinition.Assignment::getTarget)
                .map(assigned -> assigned.startsWith("@") ? assigned.substring(1) : assigned)
                .anyMatch(target::isMetaAnnotatedWith);
    }

    /// The last segment of the identifier: `PrimaryPort` from `architecture.hexagonal.PrimaryPort`.
    private static String role(Stereotype stereotype) {
        var identifier = stereotype.getIdentifier();
        return identifier.substring(identifier.lastIndexOf('.') + 1);
    }

    /// The jMolecules annotations on the target, a package, by their names: `Module`, `Adapter`.
    private static Stream<String> stereotypes(HasAnnotations<?> target) {
        return target.getAnnotations().stream()
                .filter(annotation -> isFrom(annotation, JMOLECULES))
                .map(annotation -> annotation.getRawType().getSimpleName());
    }

    /// The Spring annotations on the target, each with its value: `Component`, `Profile(cli)`.
    private static Stream<String> wiring(HasAnnotations<?> target) {
        return target.getAnnotations().stream()
                .filter(ModelWriter::isSpring)
                .map(ModelWriter::stereotype);
    }

    private static boolean isSpring(JavaAnnotation<?> annotation) {
        return isFrom(annotation, SPRING);
    }

    private static boolean isFrom(JavaAnnotation<?> annotation, String pkg) {
        return annotation.getRawType().getName().startsWith(pkg);
    }

    /// The annotation as wiring, `Profile(cli)`: its name, and in parentheses its value, its
    /// prefix, or its name attribute, the first that is set and not blank.
    private static String stereotype(JavaAnnotation<?> annotation) {
        var name = annotation.getRawType().getSimpleName();
        return Stream.of("value", "prefix", "name")
                .map(annotation::get)
                .flatMap(Optional::stream)
                .map(ModelWriter::valueText)
                .filter(value -> !value.isBlank())
                .findFirst()
                .map(value -> name + "(" + value + ")")
                .orElse(name);
    }

    /// A method's or a constructor's annotation: a bean's with what it builds,
    /// `Bean(KnowWhatIsLeft)`, the rest as on a type.
    private static String stereotype(JavaAnnotation<?> annotation, JavaCodeUnit unit) {
        if (annotation.getRawType().getName().equals(BEAN) && unit instanceof JavaMethod method) {
            return "Bean(" + method.getRawReturnType().getSimpleName() + ")";
        }
        return stereotype(annotation);
    }

    /// The member's name in an entry, a constructor named as its type.
    private static String member(JavaCodeUnit unit, JavaClass type) {
        return unit instanceof JavaConstructor ? type.getSimpleName() : unit.getName();
    }

    /// An annotation value as text: a string as is, a class by its simple name, an enum constant by
    /// its name, several joined by commas.
    private static String valueText(Object value) {
        return switch (value) {
            case JavaClass type -> type.getSimpleName();
            case JavaEnumConstant constant -> constant.name();
            case Object[] values ->
                    Arrays.stream(values).map(ModelWriter::valueText).collect(joining(", "));
            default -> String.valueOf(value);
        };
    }
}
