package dk.mathmagicians.playground.architecture.discovery.fixture.application;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/// The application wired: an auto-configuration binding the settings and building the port. Every
/// annotation on the class and its methods is a stereotype of its card, a bean's with what it
/// builds.
@AutoConfiguration
@EnableConfigurationProperties(Settings.class)
class Wiring {

    @Bean
    @Primary
    Publish publish(Publisher publisher) {
        return new Publish(publisher);
    }
}
