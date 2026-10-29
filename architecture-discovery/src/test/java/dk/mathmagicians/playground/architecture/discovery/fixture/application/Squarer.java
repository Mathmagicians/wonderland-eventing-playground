package dk.mathmagicians.playground.architecture.discovery.fixture.application;

import dk.mathmagicians.playground.architecture.discovery.fixture.domain.Circle;
import dk.mathmagicians.playground.architecture.discovery.fixture.domain.Square;
import dk.mathmagicians.playground.architecture.discovery.fixture.domain.Thing;
import java.util.Set;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

/// A story of the fixture: a driving port that subscribes to circles, `subscribesTo()` naming the
/// class, and publishes squares through the other driving port, `Publish`.
@PrimaryPort
public record Squarer(Publish publish) {

    public Set<Class<?>> subscribesTo() {
        return Set.of(Circle.class);
    }

    public void on(Circle circle) {
        publish.publish(new Thing(new Square(circle.radius()).toString()));
    }
}
