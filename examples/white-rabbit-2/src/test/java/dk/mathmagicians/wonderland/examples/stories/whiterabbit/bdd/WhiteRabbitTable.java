package dk.mathmagicians.wonderland.examples.stories.whiterabbit.bdd;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

import dk.mathmagicians.wonderland.eventing.bdd.Region;
import dk.mathmagicians.wonderland.eventing.bdd.StoryContainer;
import dk.mathmagicians.wonderland.examples.stories.whiterabbit.NoTimeToHaggle;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/// Test driver of the White Rabbit's outcome: the story as a container in the scenario's region,
/// wanting one thing, started before the offers; and what it ordered, read from its log, one line
/// per order as `NoTimeToHaggle` phrases it.
class WhiteRabbitTable {

    private static final String RUNS_FOR = "120";

    /// The order line as a pattern: each `{}` of the message a word, the customer first, then the
    /// thing, the seller, and the price as the log prints a double.
    private static final Pattern ORDERED = line(NoTimeToHaggle.ORDERED);

    private final StoryContainer story;
    private final Region region;

    /// The order lines a step has accounted for, by where they start in the log.
    private final Set<Integer> accountedFor = new HashSet<>();

    WhiteRabbitTable(StoryContainer story, Region region) {
        this.story = story;
        this.region = region;
    }

    /// Starts the White Rabbit wanting the thing, in the scenario's region, so it listens before
    /// any offer is on the stream.
    void wants(String thing) {
        var settings = NoTimeToHaggle.NAME + ".";
        story.start(
                NoTimeToHaggle.NAME,
                Map.of(
                        settings + "wants",
                        thing,
                        settings + "region",
                        region.current(),
                        settings + "ttl",
                        RUNS_FOR));
    }

    void assertOrdered(String customer, String thing, String seller, BigDecimal price) {
        var line =
                lines().stream()
                        .filter(order -> order.group(1).equals(customer))
                        .filter(order -> order.group(2).equals(thing))
                        .filter(order -> order.group(3).equals(seller))
                        .filter(order -> new BigDecimal(order.group(4)).compareTo(price) == 0)
                        .findFirst();
        assertThat(line)
                .as(
                        "a line saying %s ordered %s from %s at %s coins",
                        customer, thing, seller, price)
                .isPresent();
        accountedFor.add(line.get().start());
    }

    /// Every order line is one a step accounted for.
    void assertNothingElseOrdered() {
        var others =
                lines().stream()
                        .filter(order -> !accountedFor.contains(order.start()))
                        .map(MatchResult::group)
                        .toList();
        assertThat(others).as("orders beyond what the scenario said").isEmpty();
    }

    private List<MatchResult> lines() {
        return ORDERED.matcher(story.logs()).results().toList();
    }

    /// The pattern of an SLF4J message: its text as it is, each `{}` a word.
    private static Pattern line(String message) {
        return Pattern.compile(
                Stream.of(message.split("\\{}", -1))
                        .map(Pattern::quote)
                        .collect(joining("(\\S+)")));
    }
}
