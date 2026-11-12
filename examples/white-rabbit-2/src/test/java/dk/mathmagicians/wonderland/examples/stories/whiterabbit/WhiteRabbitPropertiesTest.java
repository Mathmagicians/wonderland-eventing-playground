package dk.mathmagicians.wonderland.examples.stories.whiterabbit;

import static dk.mathmagicians.wonderland.eventing.domain.EventFixtures.TARTS;
import static dk.mathmagicians.wonderland.eventing.domain.EventFixtures.TOP_HAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class WhiteRabbitPropertiesTest {

    private static final String FOREVER = "";

    @Test
    void holdsTheRegionTheThingsAndHowLongItRuns() {
        var settings = new WhiteRabbitProperties("APAC", List.of(TOP_HAT, TARTS), "30");

        assertThat(settings.region()).isEqualTo("APAC");
        assertThat(settings.wants()).containsExactly(TOP_HAT, TARTS);
        assertThat(settings.playsFor()).hasValue(Duration.ofSeconds(30));
    }

    @Test
    void runsInThePlatformsRegionWhenNoneIsGiven() {
        assertThat(new WhiteRabbitProperties(null, List.of(TOP_HAT), "30").region()).isNull();
    }

    @Test
    void runsUntilStoppedWhenTheTtlIsLeftEmpty() {
        assertThat(new WhiteRabbitProperties(null, List.of(TOP_HAT), FOREVER).playsFor()).isEmpty();
    }

    @Test
    void rejectsABlankRegion() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new WhiteRabbitProperties(" ", List.of(TOP_HAT), FOREVER))
                .withMessageContaining("white-rabbit.region");
    }

    @Test
    void requiresAtLeastOneThing() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new WhiteRabbitProperties(null, List.of(), FOREVER))
                .withMessageContaining("white-rabbit.wants");
    }

    @Test
    void rejectsABlankThing() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new WhiteRabbitProperties(null, List.of(TOP_HAT, " "), FOREVER))
                .withMessageContaining("white-rabbit.wants")
                .withMessageContaining(TOP_HAT);
    }

    @Test
    void rejectsAThingNamedTwice() {
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> new WhiteRabbitProperties(null, List.of(TOP_HAT, TOP_HAT), FOREVER))
                .withMessageContaining(TOP_HAT + " twice");
    }

    @Test
    void rejectsATtlOfZero() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new WhiteRabbitProperties(null, List.of(TOP_HAT), "0"))
                .withMessageContaining("white-rabbit.ttl");
    }
}
