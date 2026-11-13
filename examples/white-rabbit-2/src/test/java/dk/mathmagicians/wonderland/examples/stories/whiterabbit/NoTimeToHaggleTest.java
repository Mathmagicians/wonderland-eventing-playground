package dk.mathmagicians.wonderland.examples.stories.whiterabbit;

import static dk.mathmagicians.wonderland.eventing.application.StoryFixtures.publishMessage;
import static dk.mathmagicians.wonderland.eventing.domain.EventFixtures.AT;
import static dk.mathmagicians.wonderland.eventing.domain.EventFixtures.MAD_HATTER;
import static dk.mathmagicians.wonderland.eventing.domain.EventFixtures.MARCH_HARE;
import static dk.mathmagicians.wonderland.eventing.domain.EventFixtures.REGION;
import static dk.mathmagicians.wonderland.eventing.domain.EventFixtures.TARTS;
import static dk.mathmagicians.wonderland.eventing.domain.EventFixtures.TOP_HAT;
import static dk.mathmagicians.wonderland.eventing.domain.EventFixtures.WHITE_RABBIT;
import static dk.mathmagicians.wonderland.eventing.domain.EventFixtures.envelope;
import static dk.mathmagicians.wonderland.eventing.domain.EventFixtures.offer;
import static dk.mathmagicians.wonderland.eventing.domain.EventFixtures.order;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import dk.mathmagicians.wonderland.eventing.domain.Envelope;
import dk.mathmagicians.wonderland.eventing.domain.Offer;
import dk.mathmagicians.wonderland.eventing.domain.Order;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/// The White Rabbit over offers built by hand: an offer is one thing for sale, and what it
/// publishes is what it ordered. Every test gets a rabbit of its own, wanting a top hat, so nothing
/// carries over from one test to the next.
class NoTimeToHaggleTest {

    private static final Duration TTL = Duration.ofSeconds(30);

    /// Envelopes as the White Rabbit publishes them.
    private final List<Envelope> published = new ArrayList<>();

    private final NoTimeToHaggle whiteRabbit =
            new NoTimeToHaggle(Set.of(TOP_HAT), publishMessage(published), TTL);

    private List<Order> ordered() {
        return published.stream().map(envelope -> (Order) envelope.payload()).toList();
    }

    @Test
    void isTheWhiteRabbitStoryListeningToOffers() {
        assertThat(whiteRabbit.name()).isEqualTo("white-rabbit");
        assertThat(whiteRabbit.group()).isEqualTo("white-rabbit");
        assertThat(whiteRabbit.subscribesTo()).containsExactly(Offer.class);
        assertThat(whiteRabbit.publishes()).containsExactly(Order.class);
        assertThat(whiteRabbit.playsFor()).hasValue(TTL);
        assertThat(whiteRabbit.region()).hasValue(REGION);
    }

    @Test
    void ordersAtTheFirstOfferForAThingItWants() {
        whiteRabbit.on(envelope(offer("OFF-1", TOP_HAT, 12, MAD_HATTER)));

        assertThat(published)
                .singleElement()
                .satisfies(
                        envelope -> {
                            assertThat(envelope.region()).isEqualTo(REGION);
                            var order = (Order) envelope.payload();
                            assertThat(order.id()).startsWith("ORD-");
                            assertThat(order.customerId()).isEqualTo(WHITE_RABBIT);
                            assertThat(order.productId()).isEqualTo(TOP_HAT);
                            assertThat(order.createdAt()).isEqualTo(AT);
                        });
    }

    @Test
    void takesTheFirstOfferAndLetsACheaperOnePassBy() {
        whiteRabbit.on(envelope(offer("OFF-1", TOP_HAT, 12, MAD_HATTER)));
        whiteRabbit.on(envelope(offer("OFF-2", TOP_HAT, 7, MARCH_HARE)));

        assertThat(ordered()).hasSize(1);
        assertThat(whiteRabbit.wants()).isEmpty();
    }

    @Test
    void letsAnOfferForAnotherThingPassBy() {
        whiteRabbit.on(envelope(offer("OFF-1", TARTS, 2, MAD_HATTER)));

        assertThat(published).isEmpty();
        assertThat(whiteRabbit.wants()).containsExactly(TOP_HAT);
    }

    @Test
    void ordersEveryThingOnItsListOnce() {
        var greedy = new NoTimeToHaggle(List.of(TOP_HAT, TARTS), publishMessage(published), TTL);

        greedy.on(envelope(offer("OFF-1", TARTS, 2, MAD_HATTER)));
        greedy.on(envelope(offer("OFF-2", TOP_HAT, 12, MARCH_HARE)));
        greedy.on(envelope(offer("OFF-3", TARTS, 1, MARCH_HARE)));

        assertThat(ordered()).extracting(Order::productId).containsExactly(TARTS, TOP_HAT);
        assertThat(greedy.wants()).isEmpty();
    }

    @Test
    void runsUntilStoppedWithoutATtl() {
        assertThat(new NoTimeToHaggle(Set.of(TOP_HAT), publishMessage(published), null).playsFor())
                .isEmpty();
    }

    @Test
    void refusesAPayloadItDoesNotListenTo() {
        var envelope = envelope(order());

        assertThatIllegalStateException()
                .isThrownBy(() -> whiteRabbit.on(envelope))
                .withMessageContaining("white-rabbit");
    }
}
