package dk.mathmagicians.playground.architecture.discovery.fixture.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

/// The application's settings, bound from `fixture.*`: a type with a wiring annotation and no
/// stereotype, a card with the annotation and its prefix.
@ConfigurationProperties("fixture")
public record Settings(String region) {}
