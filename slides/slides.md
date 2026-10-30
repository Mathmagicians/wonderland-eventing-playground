---
theme: default
title: Architecture Discoverability and Verification
info: Faglig Fredag, 30 October 2026
layout: cover
---

# Architecture Discoverability and Verification

Agata Przybyszewska, PhD

Senior Enterprise Architect · DSV

Architect, DevSecOps engineer, Coder · MathMagicians

Faglig Fredag · 30 October 2026

---
layout: section
---

# Background

---

# Big enterprise, stale docs
What problem are we trying to solve?

Compliance, audit, governance.

Stop gates - you want it deployed? You need approval. 

<img src="/images/approved-stamp.svg" class="absolute right-16 top-24 w-56" alt="An approved stamp" />

Then ... life goes on[^1] ...

[^1]: The author believes that the thing worse than no documentation, is stale documentation.

---
layout: statement
---

# Must architecture docs always go stale?

---

# So what is Architecture Discoverability?
Three questions

How do we specify it?

How do we keep it in sync, as life goes on?

How do we verify it?


---

# Hexagonal Architecture

This style will be used in our examples.

<img src="/images/hexagonal-architecture.svg" class="mx-auto h-100" alt="The hexagon: primary adapters call the application through primary ports, the application calls secondary adapters through secondary ports, dependencies point inward" />

---

# Meet Wonderland, a toy platform

Built to explore the core ideas

Wonderland plays stories that send messages to each other; CLI, Kafka, and Confluent are the transports, the stories the same on each

One Spring Boot substrate plays every story, `--story=<name>`, one image, one process per story

Platform Engineering is a skill

DX, make it easy - red carpet to do the right thing

Keep it simple, hide away complexity and deep tech stacks

---

# Wonderland, as planned

<img src="/images/wonderland-architecture.svg" class="mx-auto h-100" alt="Wonderland's hexagon as planned: StoryRunner, ConsoleListener, and StoryListener drive Story; the stories publish through PublishMessage to Publisher, which LoggingPublisher and KafkaPublisher implement; Converter, Topics, and EnvelopeHeaders are shared by both sides" />

---

# Wonderland - Tech stack

Why Java? Enterprise

*$\mu$-services*: Java 25, Spring Boot, etc ...

*Runtime:* JRE, Docker + Docker Compose to run it

*Eventing Platforms:* Kafka, Confluent[^1] , Cloudflare, CLI

*Testing:* TDD: JUnit, BDD: Cucumber,  Testcontainers

*IaC:* Terraform, Terraform Cloud

*SDLC:* GitHub Actions, Makefile, Gradle[^2]

*Architecture discovery:* ArchUnit, JMolecules, PlantUML +  our PlantUML plugin

[^1]: expensive

[^2]: ... and the slides you see run from a cosy Docker container with Slidev


---
class: text-xs
---

# Java 25, Spring Boot, Docker
µ-services: Java 25, Spring Boot + Runtime: JRE, Docker + Docker Compose

| | | Run |
|---|---|---|
| **Java 25 + Spring Boot**<br>[EventingPlatform.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/main/java/dk/mathmagicians/wonderland/EventingPlatform.java) | The Spring Boot application is the substrate for the ports and the adapters; a process plays one story | `make run ARGS="--story=tea-party"` |
| **Docker + Compose**<br>[compose.yaml](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/compose.yaml) | Starts the flock from one image: a generator per region and the tea party | `TTL=300 make up` |

---
class: text-xs
---

# Kafka, Confluent, CLI
Eventing Platforms: Kafka, Confluent, Cloudflare, CLI

| | | Run |
|---|---|---|
| **CLI**<br>[ConsoleListener.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/main/java/dk/mathmagicians/wonderland/eventing/adapter/cli/console/ConsoleListener.java) | Without a cluster: a story publishes to the log and hears what is typed on stdin | `make run ARGS="--story=tea-party --tea-party.ttl="` |
| **The kafka universe**<br>[compose.kafka.yaml](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/compose.kafka.yaml) | A broker, a schema registry, and a topic viewer on the machine, with the topics and schemas `iac/` gives Confluent Cloud; what the `bdd` job plays on | `make kafka-universe-up` · `make run-tiny TRANSPORT=kafka` |
| **Kafka on Confluent Cloud**<br>[KafkaPublisher.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/main/java/dk/mathmagicians/wonderland/eventing/adapter/kafka/publisher/KafkaPublisher.java) · [kafka.properties](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/main/resources/kafka.properties) | Records carry CloudEvents headers and a Protobuf payload; the key picks the partition | `make run-tiny TRANSPORT=confluent` |

---
class: text-sm
---

# Development methodology
Testing: TDD: JUnit, BDD: Cucumber, Testcontainers

| | | Run |
|---|---|---|
| **TDD**<br>[TransactionTest.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/test/java/dk/mathmagicians/wonderland/eventing/domain/TransactionTest.java) · [ArchitectureTest.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/app/src/test/java/dk/mathmagicians/wonderland/ArchUnitTest.java) | Unit tests with JUnit 5, AssertJ, and Mockito, one class per production class; the architecture rules are unit tests too | `make test` |
| **BDD**<br>[i_can_generate_events.feature](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/test/resources/features/i_can_generate_events.feature) · [StreamSteps.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/testFixtures/java/dk/mathmagicians/wonderland/eventing/bdd/StreamSteps.java) | Cucumber features and step definitions; each feature plays against the image in Testcontainers, a red feature blocks the build | `make bdd` |
| **Spec Driven**<br>[How we work](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/README.md#how-we-work) · [discovery.md](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/docs/releases/release-0.0.7/discovery.md) | Discovery → Formulation → Automation → Verification; a release plan per folder, the board follows it | `make gh-project-sync` |

---
class: text-sm
---

# The DevSecOps part
IaC: Terraform, Terraform Cloud + SDLC: GitHub Actions, Makefile, Gradle

| | | Run |
|---|---|---|
| **Terraform + Terraform Cloud**<br>[topics.tf](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/iac/topics.tf) | Topics, schemas, and Flink tables, applied on every push to `main` | `make tf-plan` |
| **GitHub Actions**<br>[cicd.yaml](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/.github/workflows/cicd.yaml) | Makefile drives the GitHub workflow, commands can be run locally | `make check` |
| **Makefile**<br>[Makefile](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/Makefile) | All commands work locally, and drive the CICD workflows. | `make help` |
| **Gradle**<br>[platform.gradle](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform.gradle) | `platform.gradle` holds the conventions every build shares; a story's own build applies it too | `make build` |

---
class: text-xs
---

# Architecture discovery kit
Architecture discovery: ArchUnit, jMolecules, Spring Modulith, PlantUML + our own tool, architecture-discovery

| | | Run |
|---|---|---|
| **jMolecules**<br>[Story.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/main/java/dk/mathmagicians/wonderland/eventing/application/Story.java) · [package-info.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/examples/purse/src/main/java/dk/mathmagicians/wonderland/examples/stories/purse/package-info.java) | The stereotypes on the code: `@Module` on a package, `@PrimaryPort`, `@SecondaryPort`, `@PrimaryAdapter`, `@SecondaryAdapter` on the types; the annotation is the spec | |
| **ArchUnit + Spring Modulith**<br>[HexagonalArchitecture.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/HexagonalArchitecture.java) · [ModuleDocumentation.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/ModuleDocumentation.java) | The hexagonal rule and the module boundaries as unit tests, red when the core reaches an adapter; a client's build gets them for free | `make arch-verify` |
| **architecture-discovery, our own tool**<br>[hexagon.proto](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/architecture-discovery/src/main/proto/hexagon.proto) · [ModelWriter.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/model/ModelWriter.java) | Reads the classes once into one model and writes the DSL, `<story>.textproto`; draws the three levels from the model, the forest, the streams, the hexagon | `make arch-gen` |
| **PlantUML**<br>[hexagon.style.puml](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/architecture-discovery/src/main/resources/hexagon.style.puml) | Renders the three drawings to SVG, the content from the model, the look in one style file | `make arch-gen` |
| **The checks**<br>[ModelCheck.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/model/ModelCheck.java) · [HexagonSteps.java](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/bdd/HexagonSteps.java) | The DSL as specified against the DSL as built; the architecture features in Gherkin against the model, on the steps the platform ships | `make arch-check` · `make arch-bdd` |

---
class: text-xs
---

# First iteration
cli, confluent

<div class="grid grid-cols-2 gap-4">
<div>

**cli**: the console stands in for the topics, the log is where a story publishes, stdin is what it subscribes to

```bash
make run ARGS="--story=tea-party --tea-party.ttl="
Offer offer_id: "OFF-1" product_id: "P-TOPH" price: 12.5 seller_id: "MAD_HATTER"
Order id: "ORD-1" customer_id: "ALICE" product_id: "P-TOPH"
```

</div>
<div>

```
12:00:03.117  INFO [main] SettleAtTheTeaParty : tea-party: Se-tea-tled ORD-1 with OFF-1: ALICE pays MAD_HATTER 12.5
12:00:03.121  INFO [main] LoggingPublisher : Published 7c2e-… to log-0 at offset 0, key ALICE, region EMEA: Transaction id: "TX-1" customer_id: "ALICE" seller_id: "MAD_HATTER" product_id: "P-TOPH" price: 12.5
```

</div>
<div>

**confluent**: Confluent Cloud, the topics, schemas, and Flink tables from `iac/`, applied by Terraform Cloud; the keys from `.env*`

```bash
make tf-plan
make run-tiny TRANSPORT=confluent
```

</div>
<div class="flex gap-2 items-start">
  <img src="/images/confluent.png" class="h-40" alt="Confluent Cloud" />
  <img src="/images/tf.png" class="h-40" alt="Terraform Cloud" />
</div>
</div>

---
layout: image
image: /images/hexagon-confluent-cli.svg
backgroundSize: contain
---

<div class="absolute top-4 left-4 text-xs font-mono px-2 py-1 rounded bg-red-600/80 text-white">● REC · cli, confluent</div>

---
class: text-sm
---

# The wiring: a transport is a profile

A process plays `<transport>,<env>`; the adapters of a transport carry it, the properties of a transport connect it

```java
@PrimaryAdapter @Component @Profile("cli")        public final class ConsoleListener ...   // adapter/cli/console
@SecondaryAdapter @Component @Profile("cli")      final class LoggingPublisher ...         // adapter/cli/log
@PrimaryAdapter @Component @Profile("confluent")  public final class StoryListener ...     // adapter/kafka/consumer
@SecondaryAdapter @Component @Profile("confluent") final class KafkaPublisher ...          // adapter/kafka/publisher
```

```properties
# platform.properties: the transport when none is given
spring.profiles.default=cli
# application-confluent.properties: the cluster, the registry, SASL, the serializers; a missing key fails startup
spring.config.import=classpath:kafka.properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS}
```

```bash
make run TRANSPORT=confluent ENV=test    # SPRING_PROFILES_ACTIVE=confluent,test, the keys from .env*
```

---
class: text-sm
---

# Meet BDD
Cucumber · Gherkin · Testcontainers

<div class="absolute top-4 right-4 text-xs font-mono px-2 py-1 rounded bg-green-600/80 text-white">● LIVE · make run-tiny · make bdd</div>

| Feature | Story | Reads | Puts |
|---|---|---|---|
| [I can publish messages](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/test/resources/features/i_can_generate_events.feature) | the generator | | products, offers, orders |
| [At the tea party an order meets an offer](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/stories/tea-party/src/test/resources/features/at_the_tea_party_an_order_meets_an_offer.feature) | the tea party | offers, orders | transactions |
| [Alice's purse knows what is left](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/examples/purse/src/test/resources/features/alices_purse_knows_what_is_left.feature) | the purse | transactions | |

```gherkin
Scenario Outline: An order is settled with an offer for its thing
  Given a tea party in region <Region>
  And Mad Hatter offers a Top Hat for 12.5 coins
  And Alice orders a Top Hat
  When the tea party reads the streams of offers and orders
  Then Alice paid Mad Hatter 12.5 coins for the Top Hat
```

---
layout: statement
---

# Out of trial credits :)

---
class: text-sm
---

# The kafka universe
Apache Kafka · Confluent Schema Registry · Kafbat UI · compose.kafka.yaml

<div class="absolute top-4 right-4 text-xs font-mono px-2 py-1 rounded bg-green-600/80 text-white">● LIVE · make kafka-universe-up · make run-tiny TRANSPORT=kafka · localhost:8082</div>

<img src="/images/kafka-universe-message.png" class="mt-4 mx-auto h-80 rounded shadow" alt="The topic viewer: an offer on test.offers, key EMEA/P-HEDG, its Protobuf value decoded through the registry" />

---
class: text-sm
---

# The very simple refactoring
kafka is the transport, confluent is kafka with credentials · five annotations, one group, the defaults for a broker next door

```java
@Profile("kafka")   // was "confluent": KafkaPublisher, StoryListener, Reader, DeadLetters, Topics
```

```properties
# platform.properties: confluent plays kafka's adapters and settings underneath
spring.profiles.group.confluent=kafka
# kafka.properties: the broker and the registry of make kafka-universe-up, unless the environment names others
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.kafka.properties.schema.registry.url=${SCHEMA_REGISTRY_REST_ENDPOINT:http://localhost:8081}
# application-confluent.properties: what Confluent Cloud adds, SASL and the registry's basic auth
spring.kafka.security.protocol=SASL_SSL
```

```gherkin
@kafka
Feature: The cluster has my topics and schemas

  Scenario Outline: A topic exists with its schema            # plays on kafka and confluent
    Then the topic <Topic> exists
    And the Protobuf schema <Schema> is registered for the topic <Topic>

  @confluent
  Scenario Outline: A Flink table set up with IaC is on the cluster, with its schema    # confluent alone
```

---
class: text-sm
---

# Happy build
cli · kafka · cicd.yaml

```bash
make ci-transports            # ["cli","kafka"]; confluent sits beside it, commented out
make bdd                      # cli: the log and the console
make bdd TRANSPORT=kafka      # the universe up first, the containers on its network
make bdd-matrix               # both, locally what the bdd job plays
```

`cicd.yaml`, job `bdd`: `make bdd-published TRANSPORT=<t>` against the candidate image, one job per entry in `<t>-test`; `cd` publishes what both passed

<img src="/images/green_gh_cicd.png" class="mt-4 rounded" alt="cicd.yaml on a pull request: ci, bdd (cli) and bdd (kafka), cd green, tag skipped">

---
layout: image
image: /images/hexagon-kafka.svg
backgroundSize: contain
---

<div class="absolute top-4 left-4 text-xs font-mono px-2 py-1 rounded bg-red-600/80 text-white">● REC · cli, kafka, confluent</div>

---

# The annotation is the spec

The platform's driving port, [`Story`](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/platform/src/main/java/dk/mathmagicians/wonderland/eventing/application/Story.java)

```java
@PrimaryPort
public interface Story {

    String name();

    default Set<Class<? extends Payload>> subscribesTo() {
        return Set.of();
    }
}
```

Its twin in the purse, a client's story, [`KnowWhatIsLeft`](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/examples/purse/src/main/java/dk/mathmagicians/wonderland/examples/stories/purse/KnowWhatIsLeft.java)

```java
@PrimaryPort
public final class KnowWhatIsLeft implements Story {

    public static final String NAME = "purse";
```

---

# Express the architecture
purse.textproto · the_purse_has_hexagonal_architecture.feature

<div class="absolute top-4 right-4 text-xs font-mono px-2 py-1 rounded bg-green-600/80 text-white">● LIVE · make arch-gen · make arch-check · make arch-bdd</div>

```text
name: "Purse Story"
primary_port { name: "KnowWhatIsLeft"  implements: "Story" }
subscribes { payload: "Transaction" }
depends_on: ["Application", "Domain"]
wiring { configuration: "PurseStory"  settings { type: "PurseProperties"  prefix: "purse" }
         bean { name: "purse"  builds: "KnowWhatIsLeft"  from: ["PurseProperties"] } }
```

<div class="flex justify-center gap-16 text-lg text-gray-500" style="font-family: Chalkboard, 'Comic Sans MS', cursive">
<span>⇡ the feature specifies</span>
<span>arch-bdd checks ⇣</span>
</div>

```gherkin
@architecture-discovery
Feature: The purse has a hexagonal architecture

  Scenario: KnowWhatIsLeft is the purse's story
    Then its @PrimaryPort is KnowWhatIsLeft, which implements Story
    And KnowWhatIsLeft subscribesTo Transaction
    And KnowWhatIsLeft publishes nothing
```



---

# Three levels
forest · streams · hexagon · `make arch-gen`

<div class="grid grid-cols-3 grid-rows-2 gap-x-6 gap-y-2 text-xs text-center">
<div>
<img src="/generated/forest.svg" class="mx-auto h-36" alt="The forest: Wonderland's platform with its stories around it, the purse beside them">
Level 1 · the forest · the platform and its stories · <code>make arch-forest</code>
</div>
<div class="col-span-2 row-span-2">
<img src="/generated/hexagon.svg" class="mx-auto h-96" alt="The hexagon of Wonderland, generated from the code">
Level 3 · the hexagon · rings, modules, roles, arrows · <code>make arch-hexagon</code>
</div>
<div>
<img src="/generated/streams.svg" class="mx-auto h-36" alt="The streams: the stories and the topics between them, who publishes to which and who subscribes">
Level 2 · the streams · a queue per topic, who publishes, who subscribes · <code>make arch-streams</code>
</div>
</div>

---

# The agent round trip
`/wonderland-generate-story` · examples/white-rabbit

<div class="absolute top-4 right-4 text-xs font-mono px-2 py-1 rounded bg-green-600/80 text-white">● LIVE · make claude STORY=examples/white-rabbit · make check</div>

<pre class="absolute bottom-6 right-10 text-sm font-mono leading-tight text-gray-700">
   (\ /)
   ( . .)   white-rabbit
  c(")(")
</pre>

| The agent gets | Where |
|---|---|
| the features | [the_white_rabbit_has_no_time_to_haggle.feature](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/examples/white-rabbit/src/test/resources/features/the_white_rabbit_has_no_time_to_haggle.feature) · [the_white_rabbits_hexagon.feature](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/examples/white-rabbit/src/test/resources/features/the_white_rabbits_hexagon.feature) |
| the hexagon | [white-rabbit.textproto](https://github.com/Mathmagicians/wonderland-eventing-playground/blob/main/examples/white-rabbit/src/test/resources/architecture/white-rabbit.textproto) |

```text
primary_port { name: "NoTimeToHaggle"  implements: "Story" }
subscribes { payload: "Offer" }
publishes { payload: "Order"  through: "PublishMessage" }
```

---
layout: center
---

# Thanks! Questions?
