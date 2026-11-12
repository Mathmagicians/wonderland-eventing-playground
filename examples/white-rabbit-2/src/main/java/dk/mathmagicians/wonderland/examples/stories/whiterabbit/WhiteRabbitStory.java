package dk.mathmagicians.wonderland.examples.stories.whiterabbit;

import dk.mathmagicians.wonderland.eventing.application.Publishing;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/// Joins a distribution as every story does, an auto-configuration listed in `META-INF/spring`: the
/// settings bound, the White Rabbit wired from what the platform offers.
@AutoConfiguration
@EnableConfigurationProperties(WhiteRabbitProperties.class)
class WhiteRabbitStory {

    private final Publishing publishing;

    /// What a story publishes with, the platform's, taken here and not by the bean method: the
    /// hexagon states the White Rabbit is built from its settings.
    WhiteRabbitStory(Publishing publishing) {
        this.publishing = publishing;
    }

    @Bean
    NoTimeToHaggle whiteRabbit(WhiteRabbitProperties whiteRabbit) {
        return new NoTimeToHaggle(
                whiteRabbit.wants(),
                publishing.from(whiteRabbit.region()),
                whiteRabbit.playsFor().orElse(null));
    }
}
