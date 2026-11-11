package dk.mathmagicians.playground.architecture.discovery.fixture.application;

import dk.mathmagicians.playground.architecture.discovery.fixture.domain.Circle;
import dk.mathmagicians.playground.architecture.discovery.fixture.domain.Square;
import dk.mathmagicians.playground.architecture.discovery.fixture.domain.Thing;
import java.util.Set;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

/// A story of the fixture: a driving port that says what it subscribes to and what it publishes,
/// `subscribesTo()` and `publishes()` naming the classes, and publishes through the other driving
/// port, `Publish`.
@PrimaryPort
public record Squarer(Publish publish) {

    public Set<Class<?>> subscribesTo() {
        return Set.of(Circle.class);
    }

    public Set<Class<?>> publishes() {
        return Set.of(Square.class);
    }

    public void on(Circle circle) {
        publish.publish(new Thing("square of " + circle.radius()));
    }
}
