package dk.mathmagicians.wonderland.examples;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/// The wiring test of the distribution: the platform with the White Rabbit on the classpath,
/// playing it. The local profile reads nothing, and the ttl left empty keeps the runner from
/// waiting, so the context comes up and goes down with the test.
@SpringBootTest(properties = "white-rabbit.ttl=")
class WhiteRabbitTests {

    @Test
    void contextLoads() {}
}
