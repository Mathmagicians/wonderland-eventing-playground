package dk.mathmagicians.wonderland.examples.stories.whiterabbit;

import dk.mathmagicians.wonderland.eventing.application.Story;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/// The settings of the White Rabbit story, `white-rabbit.*`: the region whose offers it hears and
/// orders in, `--white-rabbit.region`, the platform's `region` unless given; the things it wants,
/// `--white-rabbit.wants="P-POCK,P-TOPH"`, one product id per thing as the wire carries it, the
/// Pocket Watch by default; and how long it runs, `--white-rabbit.ttl` seconds, five minutes by
/// default, left empty for until stopped. Every value has a default, so the bundle binds at every
/// start whatever the story; a wrong value fails startup with the entry in the message.
@ConfigurationProperties("white-rabbit")
public record WhiteRabbitProperties(
        @Nullable String region,
        @DefaultValue("P-POCK") List<String> wants,
        @DefaultValue("300") String ttl) {

    public WhiteRabbitProperties {
        if (region != null && region.isBlank()) {
            throw new IllegalArgumentException(
                    NoTimeToHaggle.NAME
                            + ".region cannot be blank; leave it out for the platform's");
        }
        if (wants == null || wants.isEmpty()) {
            throw new IllegalArgumentException(
                    NoTimeToHaggle.NAME
                            + ".wants is required: at least one thing by its product id");
        }
        var seen = new TreeSet<String>();
        for (var thing : wants) {
            if (thing.isBlank()) {
                throw new IllegalArgumentException(
                        NoTimeToHaggle.NAME + ".wants names a blank thing in " + wants);
            }
            if (!seen.add(thing)) {
                throw new IllegalArgumentException(
                        NoTimeToHaggle.NAME + ".wants names " + thing + " twice");
            }
        }
        Story.ttl(NoTimeToHaggle.NAME, ttl);
    }

    /// How long the White Rabbit runs, empty for until stopped.
    public Optional<Duration> playsFor() {
        return Story.ttl(NoTimeToHaggle.NAME, ttl);
    }
}
