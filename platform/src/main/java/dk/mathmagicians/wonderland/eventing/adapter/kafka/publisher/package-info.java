/// Driven adapter: Confluent Cloud, where envelopes go, for the test and prod profiles.
@SecondaryAdapter
@Module(name = "Kafka Publisher")
package dk.mathmagicians.wonderland.eventing.adapter.kafka.publisher;

import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.ddd.annotation.Module;
