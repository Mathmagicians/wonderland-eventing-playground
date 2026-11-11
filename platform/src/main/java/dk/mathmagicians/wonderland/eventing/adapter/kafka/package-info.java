/// The wire both Kafka adapters share: the topic per payload type and the envelope's headers. Not a
/// module of its own; the publisher and the consumer are, in the packages below.
@Adapter
package dk.mathmagicians.wonderland.eventing.adapter.kafka;

import org.jmolecules.architecture.hexagonal.Adapter;
