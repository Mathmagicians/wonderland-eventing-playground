package dk.mathmagicians.wonderland.examples.stories.whiterabbit;

import dk.mathmagicians.wonderland.eventing.application.PublishMessage;
import dk.mathmagicians.wonderland.eventing.application.Story;
import dk.mathmagicians.wonderland.eventing.domain.Envelope;
import dk.mathmagicians.wonderland.eventing.domain.Offer;
import dk.mathmagicians.wonderland.eventing.domain.Order;
import dk.mathmagicians.wonderland.eventing.domain.Payload;
import dk.mathmagicians.wonderland.eventing.domain.Wonderland;
import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The White Rabbit story, `the_white_rabbit_has_no_time_to_haggle.feature`. It reads the offers of
/// its region with a list of things it wants, and the first offer for one of them is taken as it
/// is: an order for the thing, published through `PublishMessage`, and the thing is off the list,
/// so a cheaper offer later passes by. Its consumer group is the story's name, so the instances of
/// a region share its partitions.
@PrimaryPort
public final class NoTimeToHaggle implements Story {

    public static final String NAME = "white-rabbit";

    /// The customer on every order, the White Rabbit's id on the wire.
    public static final String CUSTOMER = Wonderland.characterId("White Rabbit");

    /// What the White Rabbit says, one line per offer. INFO when it orders: customer, thing,
    /// seller, price, and the offer taken; DEBUG for an offer of a thing it does not want.
    public static final String ORDERED =
            "{} ordered {} from {} at {} coins, no time to haggle; offer {}";

    static final String PASSED_BY = "{} offers {} for {} coins, nothing the White Rabbit wants";
    static final String LANDED = "order {} landed on {}, partition {}, offset {}";

    /// The prefix of an order's id on the wire, the domain's `Order.ID_PREFIX`, which it keeps to
    /// itself.
    static final String ORDER_ID_PREFIX = "ORD";

    private static final Logger log = LoggerFactory.getLogger(NoTimeToHaggle.class);

    private final Set<String> wants = new TreeSet<>();
    private final PublishMessage publishMessage;
    private final @Nullable Duration ttl;

    /// The things the White Rabbit wants, by their product ids on the wire; what it publishes with,
    /// in the region whose offers it hears; and how long it runs, nothing for until stopped.
    public NoTimeToHaggle(
            Collection<String> wants, PublishMessage publishMessage, @Nullable Duration ttl) {
        this.wants.addAll(wants);
        this.publishMessage = publishMessage;
        this.ttl = ttl;
    }

    @Override
    public String name() {
        return NAME;
    }

    /// The region the White Rabbit runs about in, the one it publishes for.
    @Override
    public Optional<String> region() {
        return Optional.of(publishMessage.region());
    }

    @Override
    public Optional<Duration> playsFor() {
        return Optional.ofNullable(ttl);
    }

    @Override
    public Set<Class<? extends Payload>> subscribesTo() {
        return Set.of(Offer.class);
    }

    /// What it publishes, as the hexagon reads it: an order per thing it wants.
    public Set<Class<? extends Payload>> publishes() {
        return Set.of(Order.class);
    }

    @Override
    public void on(Envelope envelope) {
        switch (envelope.payload()) {
            case Offer offer -> consider(offer);
            default -> Story.super.on(envelope);
        }
    }

    /// The first offer for a thing on the list is taken: the thing comes off the list and the order
    /// goes out at the offer's price; any other offer passes by.
    private void consider(Offer offer) {
        if (!wants.remove(offer.productId())) {
            log.debug(PASSED_BY, offer.sellerId(), offer.productId(), offer.price());
            return;
        }
        var order =
                new Order(
                        Payload.id(ORDER_ID_PREFIX, publishMessage.random().get()),
                        CUSTOMER,
                        offer.productId(),
                        publishMessage.clock().instant());
        var receipt = publishMessage.publish(order);
        log.info(ORDERED, CUSTOMER, offer.productId(), offer.sellerId(), offer.price(), offer.id());
        var landed = receipt.join();
        log.debug(LANDED, order.id(), landed.topic(), landed.partition(), landed.offset());
    }

    /// The things still wanted, none once every one of them was ordered.
    public Set<String> wants() {
        return Set.copyOf(wants);
    }
}
