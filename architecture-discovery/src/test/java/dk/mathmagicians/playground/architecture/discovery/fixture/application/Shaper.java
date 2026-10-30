package dk.mathmagicians.playground.architecture.discovery.fixture.application;

import dk.mathmagicians.playground.architecture.discovery.fixture.domain.Circle;
import dk.mathmagicians.playground.architecture.discovery.fixture.domain.Thing;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

/// A story of the fixture that declares nothing: it holds the other driving port, `Publish`, and
/// builds circles, so the circles are what it publishes, read from its reach.
@PrimaryPort
public record Shaper(Publish publish) {

    public void on(Thing thing) {
        publish.publish(new Thing(new Circle(thing.name().length()).toString()));
    }
}
