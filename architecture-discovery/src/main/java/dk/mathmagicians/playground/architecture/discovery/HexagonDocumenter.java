package dk.mathmagicians.playground.architecture.discovery;

import static java.util.stream.Collectors.toCollection;

import dk.mathmagicians.playground.architecture.model.Arrow;
import dk.mathmagicians.playground.architecture.model.End;
import dk.mathmagicians.playground.architecture.model.Module;
import dk.mathmagicians.playground.architecture.model.Platform;
import dk.mathmagicians.playground.architecture.model.Relation;
import dk.mathmagicians.playground.architecture.model.Ring;
import dk.mathmagicians.playground.architecture.model.Type;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/// The hexagon of a model as PlantUML, a view of what `ModelWriter` read, content alone: the look
/// is `hexagon.style.puml`, the resource PlantUML reads as its config before the diagram. Three
/// columns, driving adapters, application, driven adapters, each a package holding the modules of
/// its ring, and below the application a row of the adapters both sides share. Every module is a
/// hexagon; the domain modules nest inside the first application module, whose own types sit in a
/// row above them. Inside a module, every type with a role is a card with the role as its
/// stereotype, in a grid; a type with wiring annotations alone, an auto-configuration, a properties
/// record, stays in the model and off the picture. The permitted types of a sealed type sit in a
/// row under the grid, each implementing it. Types of a non-hexagonal role are listed up to the
/// options' maximum, then an ellipsis line carries the total. A module carries its ring as its
/// stereotype and a card its role, what the style file colours them by; the legend names every role
/// with a card. The arrows are the model's, from a card to a card, or to the module when the target
/// has none, drawn as the legend says: `uses` solid, `implements` dashed with the hollow head,
/// `depends on` dashed. Three kinds stay in the model and off the picture: a dependency on the
/// domain, which every ring may reach, the arrows inside the domain, and what a story subscribes to
/// and publishes, which the streams draw. The options pick the direction: left to right, the
/// default, the layout engine places the rings by the arrows; top to bottom, hidden arrows hold the
/// columns and the grids. Everything follows the model's order, so the same model gives the same
/// text.
public final class HexagonDocumenter {

    /// The way the picture reads. Left to right, the layout engine places the rings by the arrows;
    /// top to bottom, hidden arrows hold three columns and the shared row in place.
    public enum Direction {
        LEFT_TO_RIGHT("lr"),
        TOP_TO_BOTTOM("tb");

        private final String word;

        Direction(String word) {
            this.word = word;
        }

        /// The direction a setting names, `lr` or `tb`.
        public static Direction of(String word) {
            return Arrays.stream(values())
                    .filter(direction -> direction.word.equalsIgnoreCase(word))
                    .findFirst()
                    .orElseThrow(
                            () ->
                                    new IllegalArgumentException(
                                            "direction must be lr or tb, was " + word));
        }
    }

    /// Choices for the picture: how many types of one role a box lists before the ellipsis line,
    /// unlimited by default, and the direction it reads, left to right by default.
    public record Options(int maxTypes, Direction direction) {

        public static Options defaults() {
            return new Options(Integer.MAX_VALUE, Direction.LEFT_TO_RIGHT);
        }

        public Options withMaxTypes(int maxTypes) {
            if (maxTypes < 1) {
                throw new IllegalArgumentException("maxTypes must be positive, was " + maxTypes);
            }
            return new Options(maxTypes, direction);
        }

        public Options withDirection(Direction direction) {
            return new Options(maxTypes, direction);
        }
    }

    private static final String INDENT = "  ";

    /// The element each thing is drawn as: a ring, a module, a type, the legend. The style file
    /// keys the look on them.
    private static final String RING = "package";

    private static final String MODULE = "hexagon";
    private static final String TYPE = "card";
    private static final String LEGEND = "frame";

    /// The space between a module's slanted sides and its cards: a rectangle around them, which the
    /// style file draws without a line.
    private static final String PADDING = "rectangle";

    /// The permitted types of a sealed type in one line, two when there are more than this.
    private static final int FAMILY_COLUMNS = 4;

    /// The columns and the row, each with its title and the way an arrow from a module in it points
    /// into the hexagon; the shared row is the one target of arrows from other adapters, drawn into
    /// it without rank, so the adapter stays at its height.
    private record Column(Ring ring, String label, String direction) {}

    private static final Column DRIVING = new Column(Ring.DRIVING, "DRIVING ADAPTERS", "right");
    private static final Column CORE = new Column(Ring.APPLICATION, "APPLICATION", "");
    private static final Column DRIVEN = new Column(Ring.DRIVEN, "DRIVEN ADAPTERS", "left");
    private static final Column SHARED = new Column(Ring.SHARED, "SHARED ADAPTERS", "up");
    private static final String INBOUND_SHARED = "[norank]";

    /// The line each relation is drawn with; no direction gives the plain arrow.
    private static String line(Relation relation, String direction) {
        return switch (relation) {
            case USES -> direction.isEmpty() ? "-->" : "-" + direction + "->";
            case IMPLEMENTS -> direction.isEmpty() ? "..|>" : "." + direction + ".|>";
            default -> direction.isEmpty() ? "..>" : "." + direction + ".>";
        };
    }

    /// An implements arrow written from the type implemented, so the implementer lands after it.
    private static final String IMPLEMENTED_BY = "<|..";

    /// The legend's samples, the three relations the lines alone say.
    private static final List<Relation> LEGEND_RELATIONS =
            List.of(Relation.USES, Relation.IMPLEMENTS, Relation.DEPENDS_ON);

    private static String word(Relation relation) {
        return relation.name().toLowerCase().replace('_', ' ');
    }

    /// A cell of a grid: the alias at its left edge and at its right edge, one and the same for a
    /// card, the two ends of a sample arrow in the legend; the alias at its top, the middle of a
    /// grid's first row, where the cell hangs from what sits above; and the cards along its bottom
    /// edge, the card itself, or the last row of a grid, where what hangs below attaches.
    record Cell(String left, String right, String top, List<String> bottom) {

        static Cell of(String alias) {
            return new Cell(alias, alias, alias, List.of(alias));
        }

        boolean isBox() {
            return !bottom.equals(List.of(left));
        }
    }

    /// A type's entry in a module: its card, and the cards of its permitted types when it is
    /// sealed.
    record Entry(Cell card, List<Cell> family) {}

    private final Platform platform;
    private final Options options;

    /// The alias of every card, by module name and type name, for the arrows.
    private final Map<String, String> cards = new TreeMap<>();

    /// The permitted types of each sealed type, by module and type name: the implements arrows
    /// within one module.
    private final Map<String, List<String>> families = new TreeMap<>();

    /// The roles the cards carry, in the order they are drawn, for the legend.
    private final Set<String> roles = new LinkedHashSet<>();

    public HexagonDocumenter(Platform platform) {
        this(platform, Options.defaults());
    }

    public HexagonDocumenter(Platform platform, Options options) {
        this.platform = platform;
        this.options = options;
        platform.getArrowList().stream()
                .filter(arrow -> arrow.getRelation() == Relation.IMPLEMENTS)
                .filter(arrow -> arrow.getFrom().getModule().equals(arrow.getTo().getModule()))
                .filter(arrow -> !arrow.getFrom().getType().isEmpty())
                .forEach(
                        arrow ->
                                families.computeIfAbsent(key(arrow.getTo()), _ -> new ArrayList<>())
                                        .add(arrow.getFrom().getType()));
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
        cards.clear();
        roles.clear();
        var out = new ArrayList<String>();
        out.add("@startuml");
        out.add(
                "' the hexagon from the model: Spring Modulith modules placed by their jMolecules stereotypes");
        if (isLeftToRight()) {
            out.add("left to right direction");
        }
        out.add("");
        var columns = new LinkedHashMap<Column, Cell>();
        column(out, DRIVING, modules(Ring.DRIVING), List.of())
                .ifPresent(cell -> columns.put(DRIVING, cell));
        column(out, CORE, modules(Ring.APPLICATION), modules(Ring.DOMAIN))
                .ifPresent(cell -> columns.put(CORE, cell));
        column(out, DRIVEN, modules(Ring.DRIVEN), List.of())
                .ifPresent(cell -> columns.put(DRIVEN, cell));
        var shared = column(out, SHARED, modules(Ring.SHARED), List.of());
        order(out, columns);
        var anchors = Optional.ofNullable(columns.get(CORE)).map(Cell::bottom).orElse(List.of());
        shared.ifPresent(
                row -> anchors.forEach(anchor -> hidden(out, "", anchor, "down", row.top())));
        out.add("");
        arrows(out);
        out.add("");
        legend(out, shared.map(Cell::bottom).orElseGet(() -> hangers(anchors)));
        out.add("@enduml");
        return String.join("\n", out) + "\n";
    }

    private boolean isLeftToRight() {
        return options.direction() == Direction.LEFT_TO_RIGHT;
    }

    /// A hidden arrow, what holds the grid top to bottom; left to right the layout engine places
    /// everything by the arrows drawn, and none is written.
    private void hidden(List<String> out, String indent, String from, String direction, String to) {
        if (!isLeftToRight()) {
            out.add(indent + from + " -[hidden]" + direction + "-> " + to);
        }
    }

    private List<Module> modules(Ring ring) {
        return platform.getModuleList().stream()
                .filter(module -> module.getRing() == ring)
                .toList();
    }

    /// Where the legend hangs when there is no shared row: the last driven card and the cards along
    /// the bottom of the application column.
    private List<String> hangers(List<String> anchors) {
        var driven =
                modules(Ring.DRIVEN).stream()
                        .flatMap(
                                module ->
                                        module.getTypeList().stream()
                                                .map(type -> cards.get(key(module, type))))
                        .filter(alias -> alias != null)
                        .toList();
        var hangers = new ArrayList<String>();
        if (!driven.isEmpty()) {
            hangers.add(driven.getLast());
        }
        hangers.addAll(anchors);
        return hangers;
    }

    /// A column: a package with the ring's name as its title, its modules as hexagons in a grid of
    /// one column. The nested modules go inside the first module of the column, or straight into
    /// the column when it has none of its own. Answers the column as a cell, its top where it hangs
    /// from what sits above, the cards along the bottom of every module in it where what hangs
    /// below attaches, since the arrows may float a module above its place in the grid; nothing
    /// when the ring has no module.
    private Optional<Cell> column(
            List<String> out, Column column, List<Module> members, List<Module> nested) {
        if (members.isEmpty() && nested.isEmpty()) {
            return Optional.empty();
        }
        out.add(RING + " \"" + column.label() + "\" as " + alias(column) + " {");
        var cells = new ArrayList<Cell>();
        if (members.isEmpty()) {
            nested.forEach(module -> cells.add(box(out, module, INDENT, List.of())));
        }
        for (var i = 0; i < members.size(); i++) {
            cells.add(box(out, members.get(i), INDENT, i == 0 ? nested : List.of()));
        }
        var cell = grid(out, "", cells, 1);
        out.add("}");
        var bottom = cells.stream().flatMap(member -> member.bottom().stream()).toList();
        return Optional.of(new Cell(cell.left(), cell.right(), cell.top(), bottom));
    }

    /// The columns in their order, left to right: an empty anchor above each, the anchors in a row,
    /// each column hung from its own, and the packages chained the same way. Without it the layout
    /// engine mirrors the columns, or lowers one whose adapters the shared row pulls at.
    private void order(List<String> out, Map<Column, Cell> columns) {
        if (isLeftToRight()) {
            return;
        }
        Column previous = null;
        for (var entry : columns.entrySet()) {
            var column = entry.getKey();
            out.add("label \" \" as " + anchor(column));
            if (previous != null) {
                hidden(out, "", anchor(previous), "right", anchor(column));
                hidden(out, "", alias(previous), "right", alias(column));
            }
            hidden(out, "", anchor(column), "down", entry.getValue().top());
            previous = column;
        }
    }

    private static String alias(Column column) {
        return column.ring() == Ring.APPLICATION ? "core" : column.ring().name().toLowerCase();
    }

    private static String anchor(Column column) {
        return "t_" + alias(column);
    }

    /// One module: its hexagon, its name above its package. Inside, the cards of its types with a
    /// role, grouped by role in the model's order, the hexagonal roles first, each group up to the
    /// options' maximum and an ellipsis line beyond it; then the modules nested inside it, each a
    /// hexagon of its own. The cards sit in a near-square grid, at least as wide as the widest
    /// family; each sealed type's permitted types hang from it in a row of their own below the
    /// grid; the nested modules come last, and above them the cards keep to one row. Answers the
    /// module as a cell, its ends the module's first card, the hexagon itself when it has none.
    private Cell box(List<String> out, Module module, String indent, List<Module> nested) {
        out.add(
                indent
                        + MODULE
                        + " \""
                        + module.getName()
                        + "\\n"
                        + packageLine(module)
                        + "\" as "
                        + alias(module)
                        + " <<"
                        + module.getRing().name().toLowerCase()
                        + ">> {");
        var inner = indent + INDENT;
        out.add(inner + PADDING + " \" \" as " + alias(module) + "_in {");
        var permitted =
                families.entrySet().stream()
                        .filter(entry -> entry.getKey().startsWith(module.getName() + "\t"))
                        .flatMap(entry -> entry.getValue().stream())
                        .collect(toCollection(TreeSet::new));
        var groups = new LinkedHashMap<String, List<Type>>();
        module.getTypeList().stream()
                .filter(type -> !permitted.contains(type.getName()))
                .filter(HexagonDocumenter::hasRole)
                .forEach(
                        type ->
                                groups.computeIfAbsent(role(type), _ -> new ArrayList<>())
                                        .add(type));
        var entries = new ArrayList<Entry>();
        groups.forEach(
                (role, types) -> {
                    var shown =
                            types.size() <= options.maxTypes()
                                    ? types
                                    : types.subList(0, options.maxTypes() - 1);
                    shown.forEach(type -> entries.add(entry(out, inner, module, type)));
                    if (shown.size() < types.size()) {
                        var total =
                                card(
                                        out,
                                        inner,
                                        module,
                                        "... (" + types.size() + " total)",
                                        role,
                                        role + "_total");
                        entries.add(new Entry(Cell.of(total), List.of()));
                    }
                });
        var own = entries.stream().map(Entry::card).toList();
        var withFamily = entries.stream().filter(entry -> !entry.family().isEmpty()).toList();
        var rows = new ArrayList<Cell>();
        if (!own.isEmpty()) {
            var widest =
                    withFamily.stream().mapToInt(entry -> columns(entry.family())).max().orElse(1);
            var top =
                    grid(
                            out,
                            inner,
                            own,
                            nested.isEmpty() ? Math.max(square(own.size()), widest) : own.size());
            var bottom = new ArrayList<String>();
            for (var entry : withFamily) {
                var family = grid(out, inner, entry.family(), columns(entry.family()));
                hidden(out, inner + INDENT, entry.card().left(), "down", family.top());
                bottom.addAll(family.bottom());
            }
            rows.add(
                    withFamily.isEmpty()
                            ? top
                            : new Cell(top.left(), top.right(), top.top(), bottom));
        }
        if (!nested.isEmpty()) {
            var children =
                    nested.stream()
                            .map(child -> box(out, child, inner + INDENT, List.of()))
                            .toList();
            rows.add(grid(out, inner, children, children.size()));
        }
        var cell = rows.isEmpty() ? Cell.of(alias(module)) : grid(out, inner, rows, 1);
        out.add(inner + "}");
        out.add(indent + "}");
        return cell;
    }

    /// A card for the type. For a sealed type, cards for its permitted types as well, its family,
    /// each with its arrow up to the type it implements. Registers every card for the arrows.
    private Entry entry(List<String> out, String indent, Module module, Type type) {
        var alias = card(out, indent, module, type.getName(), role(type), type.getName());
        cards.put(key(module, type), alias);
        var family = new ArrayList<Cell>();
        for (var name : families.getOrDefault(key(module.getName(), type.getName()), List.of())) {
            member(module, name)
                    .ifPresent(
                            member -> {
                                var memberAlias =
                                        card(out, indent, module, name, role(member), name);
                                cards.put(key(module, member), memberAlias);
                                family.add(Cell.of(memberAlias));
                            });
        }
        family.forEach(
                member ->
                        out.add(
                                indent
                                        + INDENT
                                        + member.left()
                                        + " "
                                        + line(Relation.IMPLEMENTS, isLeftToRight() ? "" : "up")
                                        + " "
                                        + alias));
        return new Entry(Cell.of(alias), family);
    }

    private static Optional<Type> member(Module module, String name) {
        return module.getTypeList().stream()
                .filter(type -> type.getName().equals(name))
                .filter(HexagonDocumenter::hasRole)
                .findFirst();
    }

    /// Writes the card, the type's name with its role as the stereotype, and notes the role for the
    /// legend. Answers the card's alias.
    private String card(
            List<String> out,
            String indent,
            Module module,
            String name,
            String role,
            String aliasName) {
        var alias = alias(module) + "_" + aliasName.replaceAll("[^A-Za-z0-9]", "_");
        out.add(indent + INDENT + TYPE + " \"" + name + "\" as " + alias + " <<" + role + ">>");
        roles.add(role);
        return alias;
    }

    /// Whether the type has a role, a stereotype; a type with wiring alone has no card.
    private static boolean hasRole(Type type) {
        return type.getStereotypeCount() > 0;
    }

    private static String role(Type type) {
        return type.getStereotype(0);
    }

    /// Hidden arrows lay the cells out in a grid with the columns given: right between neighbours
    /// in a row, down from the cards along a cell's bottom to the top of the cell below it. Answers
    /// the grid as a cell: its first cell's left, its first row's last right, the top of the middle
    /// cell of its first row, and the cards along its bottom, those of the last row, and in a grid
    /// of several columns those of every box, since a box in a row above may reach lower.
    private Cell grid(List<String> out, String indent, List<Cell> cells, int columns) {
        for (var i = 0; i < cells.size(); i++) {
            if ((i + 1) % columns != 0 && i + 1 < cells.size()) {
                hidden(
                        out,
                        indent + INDENT,
                        cells.get(i).right(),
                        "right",
                        cells.get(i + 1).left());
            }
            if (i + columns < cells.size()) {
                for (var card : cells.get(i).bottom()) {
                    hidden(out, indent + INDENT, card, "down", cells.get(i + columns).top());
                }
            }
        }
        var lastRow = (cells.size() - 1) / columns * columns;
        var bottom = new ArrayList<String>();
        for (var i = 0; i < cells.size(); i++) {
            if (i >= lastRow || columns > 1 && cells.get(i).isBox()) {
                bottom.addAll(cells.get(i).bottom());
            }
        }
        var firstRow = Math.min(columns, cells.size());
        return new Cell(
                cells.getFirst().left(),
                cells.get(firstRow - 1).right(),
                cells.get((firstRow - 1) / 2).top(),
                bottom);
    }

    /// About as many rows as columns, so a box stays near square whatever it holds.
    private static int square(int cells) {
        return Math.max(1, (int) Math.ceil(Math.sqrt(cells)));
    }

    /// A family in one line, two when it is long.
    private static int columns(List<Cell> family) {
        return Math.min(family.size(), FAMILY_COLUMNS);
    }

    /// The model's arrows, in its order, from a card: to the card at the other end, or to the
    /// module when the type there has none. An arrow within a module between a permitted type and
    /// its sealed type is drawn with the family. The direction is the source column's: right from
    /// the driving adapters, left from the driven, into the shared row without rank, sideways
    /// within a module, down otherwise.
    private void arrows(List<String> out) {
        var rings = new TreeMap<String, Ring>();
        platform.getModuleList().forEach(module -> rings.put(module.getName(), module.getRing()));
        for (var arrow : platform.getArrowList()) {
            var from = cards.get(key(arrow.getFrom()));
            if (from == null || isFamily(arrow) || !isDrawn(arrow, rings)) {
                continue;
            }
            var to = cards.getOrDefault(key(arrow.getTo()), alias(arrow.getTo().getModule()));
            if (isLeftToRight()
                    && arrow.getRelation() == Relation.IMPLEMENTS
                    && rings.get(arrow.getFrom().getModule()) == Ring.DRIVEN) {
                out.add(to + " " + IMPLEMENTED_BY + " " + from);
                continue;
            }
            out.add(from + " " + line(arrow.getRelation(), direction(arrow, rings)) + " " + to);
        }
    }

    /// Whether the picture draws the arrow. Every ring may reach the domain, so a dependency on it
    /// and the arrows inside it say nothing a reader needs; what a story subscribes to and
    /// publishes is the streams' to draw.
    private static boolean isDrawn(Arrow arrow, Map<String, Ring> rings) {
        var toDomain = rings.get(arrow.getTo().getModule()) == Ring.DOMAIN;
        var fromDomain = rings.get(arrow.getFrom().getModule()) == Ring.DOMAIN;
        return switch (arrow.getRelation()) {
            case SUBSCRIBES, PUBLISHES -> false;
            case DEPENDS_ON -> !toDomain;
            default -> !(fromDomain && toDomain);
        };
    }

    private boolean isFamily(Arrow arrow) {
        return arrow.getRelation() == Relation.IMPLEMENTS
                && arrow.getFrom().getModule().equals(arrow.getTo().getModule())
                && families.getOrDefault(key(arrow.getTo()), List.of())
                        .contains(arrow.getFrom().getType());
    }

    private String direction(Arrow arrow, Map<String, Ring> rings) {
        if (rings.get(arrow.getTo().getModule()) == Ring.SHARED) {
            return INBOUND_SHARED;
        }
        if (isLeftToRight()) {
            return "";
        }
        return switch (rings.get(arrow.getFrom().getModule())) {
            case DRIVING -> DRIVING.direction();
            case DRIVEN -> DRIVEN.direction();
            case SHARED -> SHARED.direction();
            default -> arrow.getFrom().getModule().equals(arrow.getTo().getModule()) ? "right" : "";
        };
    }

    /// The legend: a frame, one row of horizontal samples, every arrow with its relation written on
    /// it, and under it a row of cards, one per role, each named as its role. Left to right the
    /// samples and the cards are one strip, the only hidden arrows of that picture, since nothing
    /// else joins them. Hidden arrows from the hangers, the cards along the bottom of the shared
    /// row, or the last driven card and the bottom of the application column when there is none,
    /// hang it below everything, to the right. The text legend carries the system's name.
    private void legend(List<String> out, List<String> hangers) {
        out.add(LEGEND + " \"legend\" as legend {");
        var samples = new ArrayList<Cell>();
        for (var relation : LEGEND_RELATIONS) {
            var alias = "l_" + relation.name().toLowerCase();
            var sample = new Cell(alias + "_from", alias + "_to", alias + "_from", List.of());
            out.add(INDENT + "label \" \" as " + sample.left());
            out.add(INDENT + "label \" \" as " + sample.right());
            out.add(
                    INDENT
                            + sample.left()
                            + " "
                            + line(relation, "right")
                            + " "
                            + sample.right()
                            + " : "
                            + word(relation));
            samples.add(sample);
        }
        var named =
                roles.stream()
                        .map(
                                role -> {
                                    var alias = "l_role_" + role.replaceAll("[^A-Za-z0-9]", "_");
                                    out.add(
                                            INDENT + TYPE + " \"" + role + "\" as " + alias + " <<"
                                                    + role + ">>");
                                    return Cell.of(alias);
                                })
                        .toList();
        if (isLeftToRight()) {
            var strip = new ArrayList<Cell>(samples);
            strip.addAll(named);
            for (var i = 0; i + 1 < strip.size(); i++) {
                out.add(INDENT + strip.get(i).right() + " -[hidden]-> " + strip.get(i + 1).left());
            }
        } else {
            grid(out, "", samples, samples.size());
            if (!named.isEmpty()) {
                grid(out, "", named, named.size());
                hidden(out, INDENT, samples.getFirst().left(), "down", named.getFirst().left());
            }
        }
        out.add("}");
        hangers.forEach(hanger -> hidden(out, "", hanger, "down", samples.getFirst().left()));
        out.add("legend right");
        out.add(INDENT + platform.getName());
        out.add("endlegend");
    }

    private static String key(Module module, Type type) {
        return key(module.getName(), type.getName());
    }

    private static String key(End end) {
        return key(end.getModule(), end.getType());
    }

    private static String key(String module, String type) {
        return module + "\t" + type;
    }

    private static String alias(Module module) {
        return alias(module.getName());
    }

    private static String alias(String moduleName) {
        return "m_" + moduleName.replaceAll("[^A-Za-z0-9]", "_");
    }

    /// The module's package as the title's second line: its last two segments, `cli/log`.
    private static String packageLine(Module module) {
        var segments = module.getPackage().split("\\.");
        var from = Math.max(0, segments.length - 2);
        return String.join("/", Arrays.asList(segments).subList(from, segments.length));
    }
}
